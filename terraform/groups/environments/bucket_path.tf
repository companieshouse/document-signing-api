resource "aws_s3_object" "environment" {
  bucket     = local.signed_bucket_id
  content_type = "application/x-directory"
  key        = "${var.environment}/"
}
