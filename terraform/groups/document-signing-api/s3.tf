resource "aws_s3_bucket" "signed" {
  bucket = "${var.service}.${var.aws_account}.ch.gov.uk"
}

resource "aws_s3_bucket_public_access_block" "signed" {
  bucket = aws_s3_bucket.signed.id

  block_public_acls       = true
  block_public_policy     = true
  ignore_public_acls      = true
  restrict_public_buckets = true
}

resource "aws_s3_bucket_ownership_controls" "data" {
  bucket = aws_s3_bucket.signed.id

  rule {
    object_ownership = "BucketOwnerEnforced"
  }
}
