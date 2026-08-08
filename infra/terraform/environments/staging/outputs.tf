output "ecr_repository_url" {
  value = module.artifacts.ecr_repository_url
}

output "frontend_bucket_name" {
  value = module.artifacts.frontend_bucket_id
}

output "upload_bucket_name" {
  value = module.artifacts.upload_bucket_id
}

output "cloudfront_distribution_id" {
  value = module.runtime_edge.cloudfront_distribution_id
}

output "cloudfront_domain_name" {
  value = module.runtime_edge.cloudfront_domain_name
}

output "public_application_url" {
  value = "https://${module.runtime_edge.cloudfront_domain_name}"
}

output "ecs_cluster_name" {
  value = module.runtime_edge.cluster_name
}

output "backend_task_definition_arn" {
  value = module.runtime_edge.backend_task_definition_arn
}

output "migration_task_definition_arn" {
  value = module.runtime_edge.migration_task_definition_arn
}

output "rds_endpoint" {
  value     = module.database.endpoint
  sensitive = true
}

output "next_step" {
  value = "AWS-C must push an immutable image, set backend_image_uri and allowed_origins, then enable create_ecs_service."
}
