#!/bin/bash
#
# Start script for document-signing-api

PORT=8080
exec java -jar -Dserver.port="${PORT}" "document-signing-api.jar"