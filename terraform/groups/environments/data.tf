data "aws_iam_policy_document" "image_bucket" {
  statement {
    sid = "AllowListImages"

    effect = "Allow"

    actions = [
      "s3:ListBucket"
    ]

    resources = [
      local.images_bucket_arn
    ]
  }

  statement {
    sid = "AllowGetObjectImages"

    effect = "Allow"

    actions = [
      "s3:GetObject"
    ]

    resources = [
      "${local.images_bucket_arn}/*"
    ]
  }
}

data "aws_iam_policy_document" "signed_bucket" {
  statement {
    sid = "AllowListSigned"

    effect = "Allow"

    actions = [
      "s3:ListBucket"
    ]

    resources = [
      local.signed_bucket_arn
    ]
  }

  statement {
    sid = "AllowPutSigned"

    effect = "Allow"

    actions = [
      "s3:PutObject",
      "s3:PutObjectAcl"
    ]

    resources = [
      "${local.signed_bucket_arn}/${var.environment}/*"
    ]
  }
}

data "aws_iam_policy_document" "user" {
  source_policy_documents = [
    data.aws_iam_policy_document.image_bucket.json,
    data.aws_iam_policy_document.signed_bucket.json
  ]
}

data "aws_s3_bucket" "image_bucket" {
  bucket = "${var.image_bucket_name_prefix}-${var.environment}"
}

data "aws_s3_bucket" "signed_bucket" {
  bucket = var.signed_bucket_name
}
