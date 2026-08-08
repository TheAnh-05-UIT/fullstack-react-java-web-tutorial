resource "aws_db_instance" "this" {
  identifier = var.name_prefix

  engine         = "mysql"
  engine_version = "8.0"
  instance_class = var.instance_class

  db_name                     = var.db_name
  username                    = "webtutorial_admin"
  manage_master_user_password = true
  port                        = 3306

  allocated_storage     = var.allocated_storage
  max_allocated_storage = max(var.allocated_storage, 100)
  storage_type          = "gp3"
  storage_encrypted     = true

  db_subnet_group_name   = var.db_subnet_group_name
  vpc_security_group_ids = [var.security_group_id]
  publicly_accessible    = false
  multi_az               = false

  backup_retention_period = var.backup_retention_days
  backup_window           = "18:00-19:00"
  maintenance_window      = "sun:19:00-sun:20:00"

  auto_minor_version_upgrade = true
  deletion_protection        = var.deletion_protection
  skip_final_snapshot        = var.skip_final_snapshot
  final_snapshot_identifier  = var.skip_final_snapshot ? null : "${var.name_prefix}-final"
  copy_tags_to_snapshot      = true

  performance_insights_enabled = false

}
