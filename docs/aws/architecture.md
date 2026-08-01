# AWS managed target architecture

AWS-A prepares the application for the following target. It does not provision
or deploy any AWS resource.

```text
Browser
  |
  v
CloudFront
  |-- default behavior --> private S3 frontend origin (OAC)
  `-- /api/* -----------> ALB --> ECS Fargate Spring Boot
                                      |-- private RDS MySQL
                                      `-- private S3 upload bucket
```

## Request routing

- The browser uses relative `/api/v1` URLs, including credentialed requests and
  the existing CSRF cookie/header contract.
- CloudFront's default behavior serves the built SPA from a private S3 bucket.
  Object access should use Origin Access Control; the bucket must not be public.
- `/api/*` is a separate, non-cached behavior targeting the ALB. Forward all
  required cookies and headers, including `Authorization`, `Origin`,
  `X-XSRF-TOKEN`, query strings, and the host/protocol forwarding headers.
  Allow `GET`, `HEAD`, `OPTIONS`, `POST`, `PUT`, `PATCH`, and `DELETE`, and
  require HTTPS from viewers.
- Never apply the SPA fallback to `/api/*`. API 401, 403, 404, and 5xx responses
  must remain API responses.
- The SPA behavior may map S3 403/404 responses to `/index.html` with HTTP 200
  for client-side routes. Hashed assets should use long immutable caching;
  `index.html` should use short or no caching.

## Network boundary

- CloudFront and the ALB are the public request path. Only the ALB security group
  may reach the ECS application port `8080`.
- The ALB spans two public subnets, uses IP targets for Fargate, checks
  `/api/v1/health` with matcher `200`, and applies a deregistration delay aligned
  with graceful shutdown. ECS tasks have no public IP.
- ECS tasks run in private subnets. RDS is private and permits MySQL only from
  the ECS task security group.
- The upload bucket is private. There is no public ACL or public object URL.
  The existing backend `/uploads/images/{folder}/{filename}` endpoint authorizes
  and streams objects, so application URLs and access policy stay stable.
- NAT or VPC endpoints are an infrastructure decision for AWS-C. S3 and Secrets
  Manager endpoints reduce the need for public egress.

The RDS contract is MySQL 8 compatible, encrypted, backed up, not publicly
accessible, and placed in private DB subnets. Single-AZ is acceptable for a
small staging environment; production should evaluate Multi-AZ. Use a dedicated
application principal. The migration identity requires DDL rights; splitting it
from a least-privilege runtime identity is a later infrastructure hardening step.

The upload bucket contract enables Block Public Access, server-side encryption,
bucket-owner-enforced object ownership, and no public ACLs. Versioning is
recommended. A future lifecycle/reconciliation policy may remove confirmed
orphans; it must not delete active objects by age alone.

## Runtime responsibilities

The same backend image has two modes:

- Application service: `SPRING_PROFILES_ACTIVE=aws`, `APP_RUNTIME_MODE=application`.
  It starts HTTP, validates Hibernate mappings, and does not run Flyway.
- One-off migration task: `SPRING_PROFILES_ACTIVE=aws,migration`,
  `APP_RUNTIME_MODE=migration`. It starts no HTTP server, runs and validates
  Flyway, then exits. A failed migration exits unsuccessfully and must prevent
  rollout of the application service.

Run the migration task once per release before updating the ECS service. Do not
let every application task race to migrate the schema.

## IAM contract

The ECS execution role is for platform operations such as pulling the image,
writing logs, and resolving injected Secrets Manager values. The task role is
the application identity.

The application task role needs only object operations on the configured upload
prefix: `s3:PutObject`, `s3:GetObject`, `s3:DeleteObject`, and optionally
`s3:ListBucket` constrained to that prefix if operational tooling needs it. The
current application uses `HeadObject`, which is authorized by `s3:GetObject`.
It does not need ACL permissions.

The migration task needs database connectivity and secret injection, but does
not need S3 object permissions. Neither role should embed credentials in the
image, task definition source, or repository.

## Data consistency limitation

S3 object writes and relational database changes cannot share one ACID
transaction. Callers that save an object and later fail to save related database
metadata must perform compensating deletion. Orphan discovery and cleanup is an
operational concern; AWS-A does not introduce a queue or distributed transaction.

The existing `.github/workflows/cd-staging.yml` remains the legacy SSH/Docker
Compose staging path. It is not an implementation of this managed architecture.
Building AWS infrastructure and an AWS deployment workflow belongs to AWS-C.

## Object key contract

| Field | Allowed in key | Reason |
| --- | --- | --- |
| Server-selected resource folder | Yes | Keeps the current route grouping |
| UUID and validated extension | Yes | Collision resistant and content-safe |
| Configured non-secret prefix | Yes | Scopes IAM and lifecycle rules |
| Original filename, email, username | No | Avoids disclosure and client control |
| Filesystem path, token, credential | No | Prevents traversal and secret leakage |
