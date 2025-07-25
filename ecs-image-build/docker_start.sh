#!/bin/bash
#
# Start script for document-signing-api

PORT=8080

# Ensure KEYSTORE_PATH, KEYSTORE_PASSWORD, and KEYSTORE_P12_B64 are set
if [ -z "$KEYSTORE_PATH" ]; then
  echo "KEYSTORE_PATH is not set in the environment"
  exit 1
fi

if [ -z "$KEYSTORE_PASSWORD" ]; then
  echo "KEYSTORE_PASSWORD is not set in the environment"
  exit 1
fi

if [ ! -f "$KEYSTORE_PATH" ] && [ -z "$KEYSTORE_P12_B64" ]; then
  echo "KEYSTORE not available and KEYSTORE_B64 is not set in the environment"
  exit 1
elif [ ! -f "$KEYSTORE_PATH" ] && [ -n "$KEYSTORE_P12_B64" ]; then
    # Decode the keystore from the environment variable and save it to the path defined by KEYSTORE_PATH
    echo "$KEYSTORE_P12_B64" | base64 -d > "$KEYSTORE_PATH"
fi

if [ $? -ne 0 ]; then
  echo "Failed to decode the keystore"
  exit 1
fi

if [ ! -s "$KEYSTORE_PATH" ]; then
  echo "Decoded keystore file is empty or invalid"
  exit 1
fi

echo "Keystore fetched and saved to ${KEYSTORE_PATH}"

# Start the Java application
exec java -jar -Dserver.port="${PORT}" -Djavax.net.ssl.keyStore="${KEYSTORE_PATH}" -Djavax.net.ssl.keyStorePassword="$KEYSTORE_PASSWORD" -XX:MaxRAMPercentage=80 "document-signing-api.jar"
