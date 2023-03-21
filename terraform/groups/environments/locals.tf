locals {

  images_bucket_arn = data.aws_s3_bucket.image_bucket.arn
  images_bucket_id = data.aws_s3_bucket.image_bucket.id

  signed_bucket_arn = data.aws_s3_bucket.signed_bucket.arn
  signed_bucket_id = data.aws_s3_bucket.signed_bucket.id

}
