# Staging deployment

## Architecture

```text
CI Quality gate on main
→ checkout the exact tested SHA
→ build and push immutable backend/frontend images to GHCR
→ deploy through verified SSH to one Docker Compose staging host
→ Flyway V1–V7 and Hibernate validation during backend startup
→ bounded health checks and public API smoke tests
→ application rollback when the database boundary is compatible
```

The workflow is `.github/workflows/cd-staging.yml`. It never deploys pull
requests or feature branches and it does not define a production deployment.
The hosted workflow has not been run as part of this local implementation and
audit; a real staging deployment still requires the GitHub Environment and host
configuration documented below.
The staging Compose model runs one backend replica so Flyway is not started
concurrently.

## Required GitHub configuration

Create a GitHub Environment named `staging`. Configure these environment
variables:

| Variable | Purpose |
|---|---|
| `STAGING_HOST` | DNS name of the staging SSH host |
| `STAGING_PORT` | SSH port |
| `STAGING_USER` | Non-root, Docker-enabled deployment account |
| `STAGING_DEPLOY_PATH` | Absolute directory owned by the deployment account |
| `STAGING_BASE_URL` | HTTPS origin exposed by the staging reverse proxy |
| `STAGING_V7_READY` | `true` only after the first V7 backup/preflight rollout is complete |

Configure these environment secrets:

| Secret | Purpose |
|---|---|
| `STAGING_SSH_PRIVATE_KEY` | Dedicated key for the least-privilege deployment account |
| `STAGING_SSH_KNOWN_HOSTS` | Pre-verified `known_hosts` entry, obtained out of band |

GHCR uses the workflow `GITHUB_TOKEN`; no registry password is required. Keep
`main` protected by pull requests and the required `Quality gate` check.

The staging server must provide `${STAGING_DEPLOY_PATH}/.env.staging` with mode
`600`. It is managed outside Git and must define:

```dotenv
MYSQL_DATABASE=
MYSQL_USER=
MYSQL_PASSWORD=
MYSQL_ROOT_PASSWORD=
JWT_SECRET_BASE64=
CORS_ALLOWED_ORIGINS=https://staging.example.invalid
REFRESH_COOKIE_SAME_SITE=Lax
REFRESH_COOKIE_DOMAIN=
TRUST_FORWARDED_HEADERS=true
TRUSTED_PROXY_HOPS=1
STAGING_HTTP_PORT=8088
```

Replace the example origin before provisioning the file. Never reuse production
credentials. The reverse proxy must terminate TLS and forward only to
`127.0.0.1:STAGING_HTTP_PORT`, including trusted `Host`,
`X-Forwarded-For`, and `X-Forwarded-Proto` headers. HTTPS is required for the
Secure refresh cookie.

## Server prerequisites and persistence

- Docker Engine with Compose v2.
- A non-root deployment user with access only to the deployment directory and
  the Docker service required for this host.
- A TLS reverse proxy configured outside this phase.
- Enough storage for images, database backups, `webtutorial_staging_mysql_data`,
  and `webtutorial_staging_upload_data`.

The MySQL service is private to the Compose network. Deployments never run
`docker compose down -v`; database and uploads survive container replacement.

## Initial V7 rollout

V7 converts roadmap difficulty ordinals to strings and adds strict constraints.
Before its first run against an existing staging database:

1. Stop writes or use an application maintenance window.
2. Run and verify a timestamped database backup/snapshot into protected
   staging storage. Do not upload it to GitHub Actions.
3. Review the read-only checks in `scripts/db2-preflight.sql`.
4. Resolve reported violations manually; the script never changes data or emits
   email values.
5. Manually dispatch `CD - Staging` with the full tested main SHA and
   `v7_backup_confirmed=true`.
6. After successful V7 deployment, set `STAGING_V7_READY=true` to allow normal
   automatic deployments.

An empty new staging database does not contain legacy rows and is initialized by
Flyway. Do not set the confirmation merely to bypass a failed preflight.

## Normal deployment and redeployment

A successful `CI` workflow on `main` triggers staging automatically. Manual
redeployment accepts only a full SHA that is an ancestor of `main` and has a
successful `Quality gate` check. Backend and frontend images use:

```text
ghcr.io/<owner>/<repository>-backend:<full-sha>
ghcr.io/<owner>/<repository>-frontend:<full-sha>
```

The server pulls both images before replacing containers. The smoke script
checks health, frontend HTML, public content APIs, CSRF cookie behavior,
anonymous admin denial, managed-image 404 behavior, validation, request IDs,
and core security headers. It does not log cookies or credentials.

## Rollback limitation

The server records only the previous immutable image references and SHA in
`.deployment-state`; it stores no secrets. When startup or smoke tests fail,
the script restores those images only if no new schema boundary was crossed.

Flyway is forward-only. V7 can make a pre-DB-2 binary unable to read roadmap
difficulty, so automatic rollback to a pre-V7 application is deliberately
blocked after V7 migration begins. Restore the verified database backup and
select a compatible binary through the documented operator procedure instead.
No workflow runs Flyway undo.

Inspect GitHub Actions and filtered service logs for the deployed SHA, bounded
health attempts, smoke result, and rollback result. Do not publish `.env`,
`docker inspect`, cookie jars, database backups, or full environment output as
artifacts.
