resource "aws_s3_bucket" "signed" {
  bucket = "${var.service}.${var.aws_account}.ch.gov.uk"
}

resource "aws_s3_bucket_acl" "signed" {
  bucket = aws_s3_bucket.signed.id
  acl    = "private"
}

resource "aws_s3_bucket_public_access_block" "signed" {
  bucket = aws_s3_bucket.signed.id

  block_public_acls       = true
  block_public_policy     = true
  ignore_public_acls      = true
  restrict_public_buckets = true
}
