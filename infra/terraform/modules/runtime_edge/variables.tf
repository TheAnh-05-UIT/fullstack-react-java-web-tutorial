variable "name_prefix" { type = string }
variable "aws_region" { type = string }
variable "vpc_id" { type = string }
variable "public_subnet_ids" { type = list(string) }
variable "app_subnet_ids" { type = list(string) }
variable "alb_security_group_id" { type = string }
variable "ecs_security_group_id" { type = string }
variable "ecr_repository_arn" { type = string }
variable "ecr_repository_url" { type = string }
variable "frontend_bucket_arn" { type = string }
variable "frontend_bucket_regional_domain_name" { type = string }
variable "upload_bucket_name" { type = string }
variable "upload_bucket_arn" { type = string }
variable "upload_prefix" { type = string }
variable "db_endpoint" { type = string }
variable "db_port" { type = number }
variable "db_name" { type = string }
variable "db_secret_arn" { type = string }
variable "jwt_secret_arn" { type = string }
variable "backend_log_group_name" { type = string }
variable "migration_log_group_name" { type = string }
variable "origin_verify_header" { type = string }
variable "origin_verify_value" {
  type      = string
  sensitive = true
}
variable "backend_image_uri" {
  type     = string
  default  = null
  nullable = true
}
variable "allowed_origins" {
  type     = string
  default  = null
  nullable = true
}
variable "ecs_cpu" { type = number }
variable "ecs_memory" { type = number }
variable "desired_count" { type = number }
variable "create_ecs_service" { type = bool }

locals {
  image_uri = coalesce(var.backend_image_uri, "${var.ecr_repository_url}:bootstrap-required")
}
