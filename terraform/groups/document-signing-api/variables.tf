variable "aws_account" {
  description = "The name of the AWS account"
  type        = string
}

variable "image_bucket_name_prefix" {
  default     = "document-api-images"
  description = "The name prefix of the image bucket"
  type        = string
}

variable "repository_name" {
  default     = "document-signing-api"
  description = "The name of the repository in which we're operating"
  type        = string
}

variable "region" {
  description = "The AWS region in which resources will be administered"
  type        = string
}

variable "service" {
  default     = "document-signing-api"
  description = "The service name to be used when creating AWS resources"
  type        = string
}
