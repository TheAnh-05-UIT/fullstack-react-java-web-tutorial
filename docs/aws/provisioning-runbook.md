# AWS-B staging provisioning runbook

This runbook stops at reviewed Terraform plans unless an operator explicitly
approves each apply gate. It must not deploy application images or frontend
artifacts.

## Prerequisites

- Terraform 1.10+ and AWS CLI v2
- An identified non-root, non-production AWS principal
- Default region `ap-southeast-1`
- Permission to create the scoped networking, ECS, ECR, RDS, S3, IAM,
  CloudFront, Secrets Manager, and CloudWatch resources
- Current cost approval for NAT Gateway, ALB, RDS, and other billable services

Before every plan:

```powershell
aws sts get-caller-identity
aws configure get region
git status --short
```

Mask account/principal details in reports. Never print access keys or secret
values.

## Gate B1 — remote state

```powershell
cd infra/terraform/bootstrap
Copy-Item terraform.tfvars.example terraform.tfvars
# Replace only the globally unique state bucket placeholder.
terraform init -backend=false
terraform fmt -check
terraform validate
terraform plan
```

Review the state bucket, public block, ownership, encryption, and versioning.
Apply only after explicit B1 approval.

## Initialize staging state

After B1 succeeds, use the bootstrap output:

```powershell
cd ../environments/staging
terraform init `
  -backend-config="bucket=<state-bucket>" `
  -backend-config="key=staging/terraform.tfstate" `
  -backend-config="region=ap-southeast-1" `
  -backend-config="encrypt=true" `
  -backend-config="use_lockfile=true"
```

For offline validation before B1, use `terraform init -backend=false`.

## Gates B2–B4

The code is one dependency-aware staging stack, while approval is operationally
split:

1. **B2 foundation:** network, routes, NAT, security groups, log groups.
2. **B3 data/artifacts:** ECR, private buckets, Secrets Manager, RDS.
3. **B4 runtime edge:** IAM, ALB, ECS cluster/task templates, CloudFront.

Before each approved apply, use targeted plans only as an operator-controlled
bootstrap mechanism, save no binary plan in Git, and confirm zero destroys.
After the initial bootstrap, prefer normal full-stack plans to avoid permanent
targeted-apply drift.

```powershell
terraform fmt -check -recursive ../..
terraform validate
terraform plan
```

Report only add/change/destroy counts and resource types. Do not paste sensitive
plan metadata. Stop immediately if a plan includes an unexpected change or any
destroy.

## Secrets

Terraform requests an RDS-managed master password and generates a high-entropy
base64 JWT secret. ECS receives them through Secrets Manager references, never
plaintext task environment variables. State remains sensitive despite these
controls.

Do not create a bootstrap administrator password. Do not output or manually
copy secret values into GitHub, logs, tickets, or Terraform variables.

## Expected pre-deployment state

`create_ecs_service=false` is mandatory in AWS-B. The frontend bucket remains
empty and the backend service does not run. CloudFront may return 403/404.

AWS-C must:

1. Push an immutable backend `sha-*` image to ECR.
2. Set `backend_image_uri` to that exact image.
3. Set `allowed_origins` to `https://<cloudfront-domain>`.
4. Run the migration task and require exit zero.
5. Enable the ECS service and smoke-test health through CloudFront.

## Rollback and destruction

Terraform rollback does not roll back database migrations or user objects.
Retain the previous task definition and frontend release for application
rollback. Database rollback requires a migration-specific recovery plan.

Never run `terraform destroy` without a separate written approval and data
retention review. State, RDS snapshots, versioned S3 objects, and recoverable
Secrets Manager secrets can outlive a stack and continue to incur cost.
