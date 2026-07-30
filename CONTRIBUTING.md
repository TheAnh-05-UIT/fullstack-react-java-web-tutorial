# Contributing

## Local quality checks

Run the same core checks used by CI before opening a pull request.

Backend (Java 21 and Maven Wrapper 3.9.16):

```bash
cd javabackend
./mvnw clean verify
```

Frontend (Node.js 20):

```powershell
cd frontend
npm ci
npm run typecheck
npm run test:run
npm audit --audit-level=high --omit=dev
npm run build
```

Validate the container definitions from the repository root:

```powershell
docker build -t webtutorial-backend:local ./javabackend
docker build -t webtutorial-frontend:local ./frontend
docker compose --env-file .env.example config --quiet
```

## Pull request quality gate

The `CI` workflow runs for pull requests targeting `main`, pushes to `main`, and
manual dispatches. Its blocking quality gate requires:

- backend compilation and tests through `./mvnw clean verify`;
- frontend type-checking, tests, high-severity production dependency audit,
  changed-file linting, and production build;
- Gitleaks scans of new PR/push commits and the complete checked-out tree;
- HIGH and CRITICAL dependency vulnerability scanning;
- successful backend and frontend image builds, followed by HIGH and CRITICAL
  image vulnerability scanning.

Backend Surefire reports are retained as a short-lived artifact when verification
fails. CI builds container images only for validation and never pushes or deploys
them.

The full frontend lint job is explicitly non-blocking while pre-existing lint debt
is reduced. Its current baseline is 44 errors and 3 warnings. ESLint violations
in frontend source files changed by a pull request remain blocking. Once the
baseline reaches zero, make the full lint job blocking. Do not use the
non-blocking legacy job to waive new violations.

The Gitleaks allowlist is restricted to the non-production JWT fixture in the
Spring test profile and generated/dependency directories. Trivy exceptions are
documented in `.trivyignore` and must be removed or renewed by their stated review
date; new HIGH or CRITICAL findings remain blocking.

Never commit `.env`; use the tracked placeholder contract in `.env.example`.
Before changing an action or scanner pin, review its release notes, update the
stable version and immutable image digest together, then rerun actionlint and the
corresponding local scanner.

A local audit cannot claim that hosted GitHub Actions has passed. Confirm the
hosted `Quality gate` result after the workflow is committed and pushed.

## Branch protection

Configure the `main` branch to require the `Quality gate` check before merging.
The workflow defines the check, but repository branch-protection settings must be
enabled separately by a repository administrator after the workflow is merged.
