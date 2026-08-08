output "state_bucket_name" {
  description = "S3 bucket to configure in the staging backend."
  value       = aws_s3_bucket.state.id
}

output "backend_configuration" {
  description = "Non-secret backend settings. Native S3 lockfiles avoid a separate DynamoDB table."
  value = {
    bucket       = aws_s3_bucket.state.id
    key          = "staging/terraform.tfstate"
    region       = var.aws_region
    encrypt      = true
    use_lockfile = true
  }
}
