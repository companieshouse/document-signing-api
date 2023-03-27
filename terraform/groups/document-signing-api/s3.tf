resource "aws_s3_bucket" "signed" {
  bucket = "${var.service}.${var.aws_account}.ch.gov.uk"
}

resource "aws_s3_bucket_acl" "signed" {
  bucket = aws_s3_bucket.signed.id
  acl    = "private"
}
