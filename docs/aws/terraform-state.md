# Terraform state design

The bootstrap stack creates a dedicated private S3 state bucket before the
staging stack uses it. The bucket has Block Public Access, bucket-owner-enforced
ownership, AES-256 encryption, versioning, and `prevent_destroy`.

Terraform 1.10 or newer supports native S3 lockfiles. Staging uses:

```hcl
terraform {
  backend "s3" {}
}
```

Initialize it with explicit non-secret values:

```powershell
terraform init `
  -backend-config="bucket=<state-bucket>" `
  -backend-config="key=staging/terraform.tfstate" `
  -backend-config="region=ap-southeast-1" `
  -backend-config="encrypt=true" `
  -backend-config="use_lockfile=true"
```

The bootstrap stack cannot use the bucket it is creating as its own backend.
Keep its initial local state in an approved protected operator location or
migrate it to a separate pre-existing backend after Gate B1.

Terraform state is sensitive. RDS managed-password metadata, generated JWT
material, and the CloudFront origin-header value can be represented in state
even when outputs are marked sensitive. Restrict bucket access, enable version
retention, audit access, and never publish state or binary plan artifacts.

The repository ignores `.terraform`, `*.tfstate*`, real `*.tfvars`, override
files, crash logs, and `*.tfplan`. Provider lockfiles are expected to be
committed after successful initialization.
