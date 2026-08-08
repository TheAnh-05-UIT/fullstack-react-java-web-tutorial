# AWS staging infrastructure design

AWS-B describes and provisions only the staging foundation. It does not deploy
application artifacts and is not production-ready until AWS-C/AWS-D deploy and
smoke-test an immutable release.

```text
CloudFront generated HTTPS domain
├── default/*  -> private frontend S3 bucket through OAC
├── /api/*     -> public ALB origin (secret-header restricted)
└── /uploads/* -> public ALB origin (no cache)
                         |
                         v
                 private ECS Fargate task
                   ├── private RDS MySQL
                   └── private upload S3 bucket
```

## Decisions

- CloudFront and private S3 provide a low-operations React delivery path without
  public bucket hosting. OAC is the only frontend object reader.
- ALB and ECS Fargate provide managed HTTP routing and container scheduling
  without EC2 hosts, SSH, EKS control-plane complexity, or Lightsail coupling.
- RDS replaces a MySQL container so database lifecycle, backups, encryption, and
  private networking are independent from the application task.
- S3 replaces local upload volumes because Fargate filesystems are ephemeral.
  The backend remains the authorization and serving boundary; the upload bucket
  is never a browser origin.
- No Route 53 or ACM resource is created. The generated CloudFront domain is
  sufficient for initial staging. Viewer traffic is HTTPS; CloudFront-to-ALB is
  HTTP until a usable origin certificate/domain exists, so this is not
  end-to-end encrypted or production-ready.

## Network

| Layer | CIDRs/placement |
|---|---|
| VPC | `10.0.0.0/16` |
| Public | `10.0.1.0/24`, `10.0.2.0/24`; ALB and one NAT gateway |
| Private application | `10.0.11.0/24`, `10.0.12.0/24`; ECS without public IP |
| Private database | `10.0.21.0/24`, `10.0.22.0/24`; RDS without internet route |

One NAT gateway is selected for initial staging simplicity. It is a fixed-cost,
single-AZ outbound dependency. Production should evaluate one NAT per AZ or the
ECR API/Docker, S3, Logs, and Secrets Manager endpoint set.

## Security groups

| Security group | Direction | Port | Peer | Purpose |
|---|---|---:|---|---|
| ALB | In | 80 | Internet | CloudFront origin; listener rejects missing secret header |
| ALB | Out | 8080 | ECS SG | Backend forwarding |
| ECS | In | 8080 | ALB SG | No direct public backend |
| ECS | Out | 3306 | RDS SG | Database |
| ECS | Out | 443 | NAT | ECR, logs, secrets, S3 and AWS APIs |
| ECS | Out | 53 TCP/UDP | VPC resolver | DNS |
| RDS | In | 3306 | ECS SG | Database access only |

The random CloudFront custom header is origin restriction, not a WAF substitute.
Direct ALB requests receive 403. Its value is sensitive Terraform state data and
must never be logged or committed.

## Runtime and rollout

The initial task size is 0.5 vCPU and 2 GiB. Compared with 1 GiB, this leaves
more room for Java 21 startup, Hibernate/Flyway, the Hikari pool, and bounded
in-memory upload decoding. One vCPU/2 GiB is a later option if startup latency
or sustained CPU warrants its additional cost. Desired count starts at one and
autoscaling is disabled.

AWS-B registers application and migration task definitions using the ECR
repository plus a non-runnable bootstrap tag, but creates no ECS service.
AWS-C must push a real immutable `sha-*` image. The one-off migration task runs
first; only exit code zero permits the application service rollout.

The initial RDS credential is the RDS-managed master secret. It is not MySQL
`root`, but it is more privileged than a least-privilege runtime account.
Splitting migration and runtime database principals requires an approved
database bootstrap process and remains a hardening item.

## CloudFront behavior

- Default behavior reads private frontend S3 through OAC.
- A viewer-request function rewrites only extensionless/default frontend routes
  to `/index.html`. `/api/*` and `/uploads/*` are separate behaviors, so API
  errors never become React HTML. Static paths with extensions are not rewritten.
- API and uploads caching is disabled. Cookies, query strings, Authorization,
  Origin, preflight headers, and `X-XSRF-TOKEN` are forwarded deliberately.
- The frontend bucket is empty until AWS-D publishes a build, so initial
  CloudFront 403/404 responses are expected.

The CloudFront domain creates a two-step CORS dependency: first create the
distribution with no service, then set `allowed_origins` to its HTTPS URL when
AWS-C enables the ECS service. Credentialed CORS must never use `*`.
