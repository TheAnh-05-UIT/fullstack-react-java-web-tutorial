locals {
  name_prefix = "${var.project_name}-${var.environment}"
  tags = {
    Project     = var.project_name
    Environment = var.environment
    ManagedBy   = "terraform"
    Repository  = "Fullstack-React-Java-Web-Tutorial"
  }

  frontend_bucket_name = coalesce(
    var.frontend_bucket_name,
    "${local.name_prefix}-frontend-${random_id.bucket_suffix.hex}"
  )
  upload_bucket_name = coalesce(
    var.upload_bucket_name,
    "${local.name_prefix}-uploads-${random_id.bucket_suffix.hex}"
  )
}
