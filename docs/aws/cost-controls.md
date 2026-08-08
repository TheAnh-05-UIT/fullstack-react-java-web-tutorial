# AWS staging cost controls

AWS-B intentionally favors a small, understandable staging footprint. Exact
prices vary by region and date and must be checked with current AWS pricing
before an approved apply.

## Billable components

- One NAT gateway plus processed data and one public IPv4 address
- One Application Load Balancer
- ECS Fargate CPU, memory, logs, and data transfer after service enablement
- Single-AZ RDS instance, gp3 storage, backups, and I/O
- CloudFront requests and data transfer
- Frontend/upload/state S3 storage, versions, and requests
- Secrets Manager secret storage and API calls
- ECR image storage and scanning
- CloudWatch Logs retention

## Controls

- One NAT gateway, documented as a single-AZ staging dependency
- RDS `db.t4g.micro`, 20 GiB gp3, Single-AZ, seven-day backups
- ECS desired count one; service creation disabled until an image exists
- 0.5 vCPU/2 GiB tasks; no autoscaling and Container Insights disabled
- Fourteen-day application and migration log retention
- ECR immutable tags, seven-day untagged cleanup, and 30 `sha-*` images retained
- Frontend noncurrent versions expire after 30 days
- Incomplete upload multipart requests abort after seven days
- Buckets default to `force_destroy=false`
- No ALB/CloudFront access-log buckets or alarm actions in the first foundation

CloudWatch alarms are deferred until the ECS service exists and an approved
notification destination is available. The first deployment must reassess
running-task count, unhealthy hosts, ALB 5xx, RDS CPU, free storage, and
connection-count alarms.

## Shutdown and cleanup

For a temporary pause, set the ECS desired count to zero after deployment. NAT,
ALB, RDS, Secrets Manager, and storage continue to incur charges.

Destruction is intentionally manual and dangerous. Review retained uploads,
frontend versions, RDS snapshots, state versions, and secret recovery windows
before any destroy. Never run destroy from automation and never delete the
remote-state bucket before all managed stacks have been retired and audited.
