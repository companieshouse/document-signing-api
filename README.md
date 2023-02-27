# document-signing-api
API to sign documents digitally.

## Docker
To create a certificate to sign the document the following can be done on the docker image:
```
1. $ docker exec -it document-signing-api bash
2. $ cd app/
3. $ keytool -genkey -alias <CERTIFICATE_ALIAS> -keyalg RSA -sigalg SHA256withRSA -keysize 2048 -validity 3650 -keystore keystore.jk
```

Once that has been done, update the docker compose file to contain the correct values for the following:
```
      - KEYSTORE_PATH=/app/keystore.jks
      - KEYSTORE_PASSWORD=<PASSWORD>
      - CERTIFICATE_ALIAS=<CERTIFICATE_ALIAS>
```

The application should now be able to sign documents.

## Integration tests warning

* If you find any `testcontainers` based tests are hanging shortly after start up, this could be caused by start up 
checks. These can be disabled by setting this value in `~/.testcontainers.properties`:

```
checks.disable=true
```

Alternatively, when running such tests, set the corresponding environment variable:

```
export TESTCONTAINERS_CHECKS_DISABLE=true
```

Other, more manageable approaches, such as setting the same value in  `src/test/resources` do not seem to work.

The issue _may_ ultimately be caused by not having the `Use gRPC FUSE for file sharing` options checked in 
Docker Desktop General Preferences, but it may also not be convenient to have this checked either.

See the following for more information:

* [Localstack integration fails to start container unless checks are disabled ](https://github.com/testcontainers/testcontainers-java/issues/3790)
* [LocalStack fails to start container with docker v20](https://github.com/localstack/localstack/issues/3446)
* [Checks not disabled by .testcontainers.properties?](https://github.com/testcontainers/testcontainers-java/issues/2312)


