#!/usr/bin/env bash
set -euo pipefail

usage() {
  echo "Usage: $0 <https-staging-base-url>"
}

if [[ "${1:-}" == "--help" ]]; then
  usage
  exit 0
fi

base_url="${1:-}"
if [[ ! "$base_url" =~ ^https://[A-Za-z0-9.-]+(:[0-9]+)?$ ]]; then
  echo "Staging base URL must be an HTTPS origin without a path." >&2
  exit 2
fi

work_dir="$(mktemp -d)"
trap 'rm -rf "$work_dir"' EXIT
headers="$work_dir/headers"
body="$work_dir/body"
cookies="$work_dir/cookies"

request() {
  local expected="$1"
  local path="$2"
  local require_request_id="${3:-true}"
  shift 3 || true
  local status
  status="$(curl --silent --show-error --connect-timeout 5 --max-time 15 \
    --output "$body" --dump-header "$headers" --write-out '%{http_code}' \
    "$@" "${base_url}${path}")"
  if [[ "$status" != "$expected" ]]; then
    echo "Smoke check failed for ${path}: expected ${expected}, received ${status}." >&2
    return 1
  fi
  if [[ "$require_request_id" == "true" ]] && ! grep -qi '^X-Request-ID:' "$headers"; then
    echo "Smoke check failed for ${path}: X-Request-ID is missing." >&2
    return 1
  fi
}

request 200 "/api/v1/health" true
grep -q '"status":"UP"' "$body"
grep -qi '^X-Content-Type-Options: nosniff' "$headers"
grep -qi '^X-Frame-Options: DENY' "$headers"

request 200 "/" false
grep -qi '<!doctype html' "$body"

request 200 "/api/v1/tutorials?page=0&size=1" true
request 200 "/api/v1/projects?page=0&size=1" true
request 200 "/api/v1/roadmaps?page=0&size=1" true
request 401 "/api/v1/tutorials/admin" true
request 404 "/uploads/images/general/00000000-0000-0000-0000-000000000000.png" true
request 400 "/api/v1/tutorials?page=-1&size=1" true

request 204 "/api/v1/csrf" true --cookie-jar "$cookies"
if ! grep -q 'XSRF-TOKEN' "$cookies"; then
  echo "Smoke check failed for /api/v1/csrf: XSRF-TOKEN cookie is missing." >&2
  exit 1
fi

echo "Public staging smoke tests passed."
