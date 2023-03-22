resource "aws_s3_bucket" "signed" {
  bucket = var.signed_bucket_name
}

resource "aws_s3_bucket_acl" "signed" {
  bucket = aws_s3_bucket.signed.id
  acl    = "private"
}
