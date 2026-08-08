variable "name_prefix" { type = string }
variable "ecr_repository_name" { type = string }
variable "frontend_bucket_name" { type = string }
variable "upload_bucket_name" { type = string }
variable "force_destroy" { type = bool }
variable "log_retention_days" { type = number }
