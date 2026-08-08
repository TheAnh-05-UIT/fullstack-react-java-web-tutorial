variable "aws_region" {
  description = "AWS region that owns the Terraform state bucket."
  type        = string
  default     = "ap-southeast-1"
}

variable "project_name" {
  description = "Project identifier used in state resource names and tags."
  type        = string
  default     = "web-tutorial"

  validation {
    condition     = can(regex("^[a-z][a-z0-9-]{2,30}$", var.project_name))
    error_message = "project_name must be a lowercase DNS-compatible name."
  }
}

variable "state_bucket_name" {
  description = "Globally unique private S3 bucket name for Terraform state."
  type        = string

  validation {
    condition     = can(regex("^[a-z0-9][a-z0-9.-]{1,61}[a-z0-9]$", var.state_bucket_name))
    error_message = "state_bucket_name must be a valid S3 bucket name."
  }
}
