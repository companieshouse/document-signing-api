resource "aws_iam_user_policy" "user_policy" {
  name   = "${var.service}-${var.environment}"
  policy = data.aws_iam_policy_document.user.json
  user   = aws_iam_user.user.name
}
