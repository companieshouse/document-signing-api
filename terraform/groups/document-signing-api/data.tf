data "aws_iam_policy_document" "image_bucket" {
  statement {
    sid = "AllowListImages"

    effect = "Allow"

    actions = [
      "s3:ListBucket"
    ]

    resources = [
      "arn:aws:s3:::${var.image_bucket_name_prefix}-*"
    ]
  }

  statement {
    sid = "AllowGetObjectImages"

    effect = "Allow"

    actions = [
      "s3:GetObject"
    ]

    resources = [
      "arn:aws:s3:::${var.image_bucket_name_prefix}-*/*"
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
      "${local.signed_bucket_arn}/*/*"
    ]
  }
}

data "aws_iam_policy_document" "user" {
  source_policy_documents = [
    data.aws_iam_policy_document.image_bucket.json,
    data.aws_iam_policy_document.signed_bucket.json
  ]
}
