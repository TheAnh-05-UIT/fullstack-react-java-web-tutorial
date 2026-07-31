#!/usr/bin/env bash
set -euo pipefail

deploy_dir="${DEPLOY_DIR:?DEPLOY_DIR is required}"
backend_image="${BACKEND_IMAGE:?BACKEND_IMAGE is required}"
frontend_image="${FRONTEND_IMAGE:?FRONTEND_IMAGE is required}"
deploy_sha="${DEPLOY_SHA:?DEPLOY_SHA is required}"
staging_url="${STAGING_BASE_URL:?STAGING_BASE_URL is required}"
v7_backup_confirmed="${V7_BACKUP_CONFIRMED:-false}"

if [[ ! "$deploy_sha" =~ ^[0-9a-f]{40}$ ]]; then
  echo "DEPLOY_SHA must be a full commit SHA." >&2
  exit 2
fi
if [[ ! "$backend_image" =~ :${deploy_sha}$ ]] || [[ ! "$frontend_image" =~ :${deploy_sha}$ ]]; then
  echo "Both image references must use DEPLOY_SHA as their immutable tag." >&2
  exit 2
fi

cd "$deploy_dir"
if [[ ! -f .env.staging ]]; then
  echo "${deploy_dir}/.env.staging must be provisioned outside Git with mode 600." >&2
  exit 1
fi
if [[ "$(stat -c '%a' .env.staging)" != "600" ]] ||
   [[ "$(stat -c '%u' .env.staging)" != "$(id -u)" ]]; then
  echo "${deploy_dir}/.env.staging must be owned by the deployment user with mode 600." >&2
  exit 1
fi

compose=(docker compose --env-file .env.staging -f docker-compose.staging.yml)
state_file=".deployment-state"
previous_backend=""
previous_frontend=""
previous_sha=""
migration_applied=false
deployment_started=false
state_tmp=""

if [[ -f "$state_file" ]]; then
  while IFS='=' read -r key value; do
    case "$key" in
      BACKEND_IMAGE) previous_backend="$value" ;;
      FRONTEND_IMAGE) previous_frontend="$value" ;;
      DEPLOY_SHA) previous_sha="$value" ;;
    esac
  done < "$state_file"
fi

export BACKEND_IMAGE="$backend_image"
export FRONTEND_IMAGE="$frontend_image"

rollback() {
  local exit_code=$?
  if [[ -n "$state_tmp" ]] && [[ -f "$state_tmp" ]]; then
    rm -f "$state_tmp"
  fi
  if [[ "$deployment_started" != "true" ]] || [[ -z "$previous_backend" ]] ||
     [[ -z "$previous_frontend" ]] || [[ "$migration_applied" == "true" ]]; then
    echo "Automatic application rollback is unavailable or unsafe; operator intervention is required." >&2
    exit "$exit_code"
  fi

  echo "Deployment failed; restoring previous application commit ${previous_sha:-unknown}."
  export BACKEND_IMAGE="$previous_backend"
  export FRONTEND_IMAGE="$previous_frontend"
  "${compose[@]}" up -d --no-deps backend frontend
  for attempt in {1..24}; do
    if ./scripts/smoke-staging.sh "$staging_url"; then
      echo "Previous application version restored."
      exit "$exit_code"
    fi
    echo "Waiting for rollback health (${attempt}/24)."
    sleep 5
  done
  echo "Rollback health verification failed; operator intervention is required." >&2
  exit "$exit_code"
}
trap rollback ERR

echo "Pulling immutable images for commit ${deploy_sha}."
"${compose[@]}" pull backend frontend
"${compose[@]}" up -d db

for attempt in {1..24}; do
  # Variables in this command expand inside the database container.
  # shellcheck disable=SC2016
  if "${compose[@]}" exec -T db sh -c \
      'MYSQL_PWD="$MYSQL_ROOT_PASSWORD" mysqladmin ping -h 127.0.0.1 -u root --silent' \
      >/dev/null 2>&1; then
    break
  fi
  if [[ "$attempt" == "24" ]]; then
    echo "Staging database did not become healthy." >&2
    exit 1
  fi
  echo "Waiting for staging database (${attempt}/24)."
  sleep 5
done

# Variables in these commands expand inside the database container.
# shellcheck disable=SC2016
has_schema="$("${compose[@]}" exec -T db sh -c \
  'MYSQL_PWD="$MYSQL_PASSWORD" mysql -N -B -u "$MYSQL_USER" "$MYSQL_DATABASE" \
    -e "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = '\''users'\'';"')"
if [[ "$has_schema" == "1" ]]; then
  # shellcheck disable=SC2016
  v7_installed="$("${compose[@]}" exec -T db sh -c \
    'MYSQL_PWD="$MYSQL_PASSWORD" mysql -N -B -u "$MYSQL_USER" "$MYSQL_DATABASE" \
      -e "SELECT COUNT(*) FROM flyway_schema_history WHERE version = '\''7'\'' AND success = 1;"' \
      2>/dev/null || echo 0)"
  if [[ "$v7_installed" != "1" ]]; then
    if [[ "$v7_backup_confirmed" != "true" ]]; then
      echo "V7 is not installed. A verified staging backup and explicit confirmation are required." >&2
      exit 1
    fi
    # shellcheck disable=SC2016
    violation_count="$("${compose[@]}" exec -T db sh -c \
      'MYSQL_PWD="$MYSQL_PASSWORD" mysql -N -B -u "$MYSQL_USER" "$MYSQL_DATABASE"' \
      < scripts/db2-preflight.sql)"
    if [[ "$violation_count" != "0" ]]; then
      echo "DB-2 preflight found ${violation_count} aggregate violation group(s); no data was changed." >&2
      exit 1
    fi
    migration_applied=true
  fi
fi

deployment_started=true
"${compose[@]}" up -d --no-deps backend
"${compose[@]}" up -d --no-deps frontend

for attempt in {1..24}; do
  if "${compose[@]}" ps --status running backend | grep -q backend &&
     "${compose[@]}" ps --status running frontend | grep -q frontend; then
    if ./scripts/smoke-staging.sh "$staging_url"; then
      break
    fi
  fi
  if [[ "$attempt" == "24" ]]; then
    echo "Staging health/smoke checks timed out." >&2
    exit 1
  fi
  echo "Waiting for staging services (${attempt}/24)."
  sleep 5
done

state_tmp="$(mktemp "${deploy_dir}/.deployment-state.XXXXXX")"
printf 'BACKEND_IMAGE=%s\nFRONTEND_IMAGE=%s\nDEPLOY_SHA=%s\n' \
  "$backend_image" "$frontend_image" "$deploy_sha" > "$state_tmp"
chmod 600 "$state_tmp"
mv "$state_tmp" "$state_file"
state_tmp=""
trap - ERR
echo "Staging deployment ${deploy_sha} completed successfully."
