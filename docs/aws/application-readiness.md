# AWS application readiness

## Frontend

- `VITE_API_BASE_URL` is optional. Production builds default to same-origin
  `/api/v1`; do not inject an ALB hostname into browser code.
- Local Vite development proxies `/api` and `/uploads` to
  `VITE_DEV_API_TARGET`, defaulting to `http://localhost:8080`.
- Upload links remain `/uploads/images/...`. Under the managed target, route
  `/uploads/*` to the ALB as a non-cached dynamic behavior as well as `/api/*`.
- Upload the contents of `frontend/dist/` to the private frontend bucket, not
  the directory itself.
- Invalidate `index.html` (or use versioned release prefixes) after publishing.

## Backend

- The container runs Java 21 as non-root UID `10001` and listens on `8080`.
- ECS may use a read-only root filesystem with a writable `/tmp`; AWS mode does
  not persist uploads locally. CPU and memory sizing remain task-definition
  concerns rather than source constants.
- AWS profile storage is S3; local and Docker Compose storage remain filesystem
  based.
- The AWS SDK default credential provider chain supplies task-role credentials.
  No access key properties exist in application configuration.
- The existing SEC-4 validator decodes a bounded upload before storage; S3
  request bodies and reads are therefore in memory but capped by
  `app.upload.max-file-size` (5 MB by default). Revisit streaming only if that
  product limit grows materially.
- Requests are gracefully drained during ECS termination. Set the ALB target
  deregistration delay consistently with `APP_SHUTDOWN_TIMEOUT`.
- Logs go to stdout/stderr for the `awslogs` driver and CloudWatch Logs.
- The existing `/api/v1/health` endpoint checks database readiness. Configure the
  ALB health check to use it, allow its successful status code, and give startup
  enough grace time for the application to initialize.

## Database and rollout

1. Inject JDBC URL, application username, and password from the deployment
   environment/Secrets Manager.
2. Run the one-off migration task using the exact backend image tag selected for
   the release.
3. Require a zero exit code from migration.
4. Update the ECS service to the same immutable image digest/tag.
5. Wait for ALB targets to become healthy; retain the previous task definition
   for rollback.

RDS should enforce TLS according to the chosen engine policy. Include the
required JDBC TLS parameters in `SPRING_DATASOURCE_URL`; no certificate bypass
is provided by the application.

## Validation commands

```powershell
cd javabackend
.\mvnw.cmd clean verify

cd ..\frontend
npm run typecheck
npm test -- --run
npm run build

cd ..
docker compose --env-file .env.example config --quiet
docker build -t webtutorial-backend:aws-a .\javabackend
```

Container runtime smoke checks require reachable MySQL and non-secret runtime
values. Use application mode with `SPRING_PROFILES_ACTIVE=aws`; use migration
mode with `SPRING_PROFILES_ACTIVE=aws,migration` and
`APP_RUNTIME_MODE=migration`. AWS-A does not contact a real AWS account.

## Out of scope

- AWS account, VPC, ALB, ECS, RDS, S3, CloudFront, IAM, DNS, or certificates
- Terraform, CloudFormation, CDK, or production task definitions
- Publishing images or frontend artifacts
- Replacing the legacy CD-1 workflow
