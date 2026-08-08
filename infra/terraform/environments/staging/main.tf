resource "random_id" "bucket_suffix" {
  byte_length = 4
}

resource "random_password" "jwt_material" {
  length  = 86
  special = false
}

resource "random_password" "origin_verify" {
  length  = 48
  special = false
}

resource "aws_secretsmanager_secret" "jwt" {
  name                    = "${local.name_prefix}/jwt"
  recovery_window_in_days = 7
}

resource "aws_secretsmanager_secret_version" "jwt" {
  secret_id     = aws_secretsmanager_secret.jwt.id
  secret_string = base64encode(random_password.jwt_material.result)
}

module "networking" {
  source = "../../modules/networking"

  name_prefix         = local.name_prefix
  vpc_cidr            = var.vpc_cidr
  availability_zones  = var.availability_zones
  public_subnet_cidrs = var.public_subnet_cidrs
  app_subnet_cidrs    = var.app_subnet_cidrs
  db_subnet_cidrs     = var.db_subnet_cidrs
  enable_nat_gateway  = var.enable_nat_gateway
}

module "security" {
  source = "../../modules/security"

  name_prefix = local.name_prefix
  vpc_id      = module.networking.vpc_id
  vpc_cidr    = module.networking.vpc_cidr
}

module "artifacts" {
  source = "../../modules/artifacts"

  name_prefix          = local.name_prefix
  ecr_repository_name  = "${var.project_name}-backend"
  frontend_bucket_name = local.frontend_bucket_name
  upload_bucket_name   = local.upload_bucket_name
  force_destroy        = var.s3_force_destroy
  log_retention_days   = var.log_retention_days
}

module "database" {
  source = "../../modules/database"

  name_prefix           = local.name_prefix
  db_name               = var.database_name
  db_subnet_group_name  = module.networking.db_subnet_group_name
  security_group_id     = module.security.rds_security_group_id
  instance_class        = var.rds_instance_class
  allocated_storage     = var.rds_allocated_storage
  backup_retention_days = var.backup_retention_days
  deletion_protection   = var.rds_deletion_protection
  skip_final_snapshot   = var.rds_skip_final_snapshot
}

module "runtime_edge" {
  source = "../../modules/runtime_edge"

  name_prefix                          = local.name_prefix
  aws_region                           = var.aws_region
  vpc_id                               = module.networking.vpc_id
  public_subnet_ids                    = module.networking.public_subnet_ids
  app_subnet_ids                       = module.networking.app_subnet_ids
  alb_security_group_id                = module.security.alb_security_group_id
  ecs_security_group_id                = module.security.ecs_security_group_id
  ecr_repository_arn                   = module.artifacts.ecr_repository_arn
  ecr_repository_url                   = module.artifacts.ecr_repository_url
  frontend_bucket_arn                  = module.artifacts.frontend_bucket_arn
  frontend_bucket_regional_domain_name = module.artifacts.frontend_bucket_regional_domain_name
  upload_bucket_name                   = module.artifacts.upload_bucket_id
  upload_bucket_arn                    = module.artifacts.upload_bucket_arn
  upload_prefix                        = "uploads/images"
  db_endpoint                          = module.database.endpoint
  db_port                              = module.database.port
  db_name                              = module.database.database_name
  db_secret_arn                        = module.database.master_secret_arn
  jwt_secret_arn                       = aws_secretsmanager_secret.jwt.arn
  backend_log_group_name               = module.artifacts.backend_log_group_name
  migration_log_group_name             = module.artifacts.migration_log_group_name
  origin_verify_header                 = "X-Origin-Verify"
  origin_verify_value                  = random_password.origin_verify.result
  backend_image_uri                    = var.backend_image_uri
  allowed_origins                      = var.allowed_origins
  ecs_cpu                              = var.ecs_cpu
  ecs_memory                           = var.ecs_memory
  desired_count                        = var.ecs_desired_count
  create_ecs_service                   = var.create_ecs_service
}

data "aws_iam_policy_document" "frontend_cloudfront" {
  statement {
    actions   = ["s3:GetObject"]
    resources = ["${module.artifacts.frontend_bucket_arn}/*"]
    principals {
      type        = "Service"
      identifiers = ["cloudfront.amazonaws.com"]
    }
    condition {
      test     = "StringEquals"
      variable = "AWS:SourceArn"
      values   = [module.runtime_edge.cloudfront_distribution_arn]
    }
  }
}

resource "aws_s3_bucket_policy" "frontend_cloudfront" {
  bucket = module.artifacts.frontend_bucket_id
  policy = data.aws_iam_policy_document.frontend_cloudfront.json
}
