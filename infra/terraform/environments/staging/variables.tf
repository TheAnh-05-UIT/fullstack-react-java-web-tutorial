variable "aws_region" {
  type        = string
  description = "AWS region for staging."
  default     = "ap-southeast-1"

  validation {
    condition     = var.aws_region == "ap-southeast-1"
    error_message = "AWS-B staging is restricted to ap-southeast-1."
  }
}

variable "project_name" {
  type        = string
  description = "Lowercase project identifier."
  default     = "web-tutorial"
}

variable "environment" {
  type        = string
  description = "Environment guard."
  default     = "staging"

  validation {
    condition     = var.environment == "staging"
    error_message = "This stack may provision staging only."
  }
}

variable "availability_zones" {
  type        = list(string)
  description = "Exactly two availability zones."
  default     = ["ap-southeast-1a", "ap-southeast-1b"]

  validation {
    condition     = length(var.availability_zones) == 2 && length(distinct(var.availability_zones)) == 2
    error_message = "Provide exactly two distinct availability zones."
  }
}

variable "vpc_cidr" {
  type    = string
  default = "10.0.0.0/16"
  validation {
    condition     = can(cidrnetmask(var.vpc_cidr))
    error_message = "vpc_cidr must be valid IPv4 CIDR."
  }
}

variable "public_subnet_cidrs" {
  type    = list(string)
  default = ["10.0.1.0/24", "10.0.2.0/24"]
}

variable "app_subnet_cidrs" {
  type    = list(string)
  default = ["10.0.11.0/24", "10.0.12.0/24"]
}

variable "db_subnet_cidrs" {
  type    = list(string)
  default = ["10.0.21.0/24", "10.0.22.0/24"]
}

variable "enable_nat_gateway" {
  type        = bool
  description = "One NAT gateway is the initial staging outbound strategy."
  default     = true
}

variable "ecs_cpu" {
  type        = number
  description = "Fargate CPU units; 512 is 0.5 vCPU."
  default     = 512
  validation {
    condition     = contains([512, 1024], var.ecs_cpu)
    error_message = "Staging supports 512 or 1024 CPU units."
  }
}

variable "ecs_memory" {
  type        = number
  description = "Fargate memory in MiB."
  default     = 2048
  validation {
    condition     = contains([1024, 2048, 3072, 4096], var.ecs_memory)
    error_message = "Use a supported staging Fargate memory size."
  }
}

variable "ecs_desired_count" {
  type    = number
  default = 1
  validation {
    condition     = var.ecs_desired_count >= 0 && var.ecs_desired_count <= 2
    error_message = "Staging desired count must be between 0 and 2."
  }
}

variable "create_ecs_service" {
  type        = bool
  description = "Keep false until AWS-C pushes an immutable backend image."
  default     = false
}

variable "backend_image_uri" {
  type        = string
  description = "Immutable ECR image URI supplied by AWS-C. Null during AWS-B."
  default     = null
  nullable    = true
}

variable "allowed_origins" {
  type        = string
  description = "CloudFront HTTPS origin supplied after distribution creation and before service enablement."
  default     = null
  nullable    = true
}

variable "rds_instance_class" {
  type    = string
  default = "db.t4g.micro"
}

variable "rds_allocated_storage" {
  type    = number
  default = 20
  validation {
    condition     = var.rds_allocated_storage >= 20 && var.rds_allocated_storage <= 100
    error_message = "Staging RDS storage must be between 20 and 100 GiB."
  }
}

variable "database_name" {
  type    = string
  default = "webtutorial"
  validation {
    condition     = can(regex("^[A-Za-z][A-Za-z0-9_]{0,63}$", var.database_name))
    error_message = "database_name must be a valid MySQL database name."
  }
}

variable "backup_retention_days" {
  type    = number
  default = 1
}

variable "rds_deletion_protection" {
  type        = bool
  description = "False for initial staging; enable before storing important data."
  default     = false
}

variable "rds_skip_final_snapshot" {
  type        = bool
  description = "True is an accepted disposable-staging trade-off."
  default     = true
}

variable "log_retention_days" {
  type    = number
  default = 14
  validation {
    condition     = contains([7, 14, 30, 60, 90], var.log_retention_days)
    error_message = "Select a supported finite CloudWatch retention."
  }
}

variable "s3_force_destroy" {
  type        = bool
  description = "False protects staged frontend and user uploads from implicit deletion."
  default     = false
}

variable "frontend_bucket_name" {
  type        = string
  description = "Optional globally unique override; otherwise a random suffix is generated."
  default     = null
  nullable    = true
}

variable "upload_bucket_name" {
  type        = string
  description = "Optional globally unique override; otherwise a random suffix is generated."
  default     = null
  nullable    = true
}
