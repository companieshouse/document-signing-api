#!/bin/bash
#
# Start script for document-signing-api

APP_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

if [[ -z "${MESOS_SLAVE_PID}" ]]; then
    source ~/.chs_env/private_env
    source ~/.chs_env/global_env
    source ~/.chs_env/document-signing-api/env

    PORT="${DOCUMENT_SIGNING_API_PORT}"
else
    PORT="$1"
    CONFIG_URL="$2"
    ENVIRONMENT="$3"
    APP_NAME="$4"

    echo "Downloading environment from: ${CONFIG_URL}/${ENVIRONMENT}/${APP_NAME}"
    wget -O "${APP_DIR}/private_env" "${CONFIG_URL}/${ENVIRONMENT}/private_env"
    wget -O "${APP_DIR}/global_env" "${CONFIG_URL}/${ENVIRONMENT}/global_env"
    wget -O "${APP_DIR}/app_env" "${CONFIG_URL}/${ENVIRONMENT}/${APP_NAME}/env"
    wget -O "${APP_DIR}/keystore.p12" "${CONFIG_URL}/${ENVIRONMENT}/${APP_NAME}/keystore.p12"
    source "${APP_DIR}/private_env"
    source "${APP_DIR}/global_env"
    source "${APP_DIR}/app_env"
fi

exec java ${JAVA_MEM_ARGS} -jar -Dserver.port="${PORT}" "${APP_DIR}/document-signing-api.jar"