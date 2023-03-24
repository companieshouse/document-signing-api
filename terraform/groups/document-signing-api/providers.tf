provider "aws" {
  region = var.region

  default_tags {
    tags = {
      "Terraform" = var.repository_name
    }
  }
}
