provider "aws" {
  region = var.aws_region

  default_tags {
    tags = {
      Project     = var.project_name
      Environment = "shared"
      ManagedBy   = "terraform"
      Repository  = "Fullstack-React-Java-Web-Tutorial"
    }
  }
}
