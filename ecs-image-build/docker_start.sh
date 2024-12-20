#!/bin/bash
#
# Start script for document-signing-api

PORT=8080
SECRET_NAME="ecs/document-signing-api-cidev/keystore.p12"

# Ensure KEYSTORE_PATH and KEYSTORE_PASSWORD are set
if [ -z "$KEYSTORE_PATH" ]; then
  echo "KEYSTORE_PATH is not set in the environment"
  exit 1
fi

if [ -z "$KEYSTORE_PASSWORD" ]; then
  echo "KEYSTORE_PASSWORD is not set in the environment"
  exit 1
fi

# Fetch the keystore binary from AWS Secrets Manager
echo "Fetching keystore from AWS Secrets Manager..."
aws secretsmanager get-secret-value --secret-id "${SECRET_NAME}" --query 'SecretBinary' --output text | base64 --decode > "${KEYSTORE_PATH}"

if [ $? -ne 0 ]; then
  echo "Failed to fetch keystore from AWS Secrets Manager"
  exit 1
fi

echo "Keystore fetched and saved to ${KEYSTORE_PATH}"

# Start the application
exec java -jar -Dserver.port="${PORT}" -Djavax.net.ssl.keyStore="${KEYSTORE_PATH}" -Djavax.net.ssl.keyStorePassword="${KEYSTORE_PASSWORD}" "document-signing-api.jar"
