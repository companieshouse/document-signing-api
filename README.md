# document-signing-api
API to sign documents digitally.

## MVP

* The MVP version of this API will be limited to digitally signing certified copies.

## Requirements
In order to build document-generator locally you will need the following:
- Java 21
- Maven
- Docker
- Git

## Environment Variables

The supported environmental variables have been categorised by use case and are as follows.

### Deployment Variables
| Name                   | Description                                                    | Mandatory | Default | Example             |
|------------------------|----------------------------------------------------------------|-----------|---------|---------------------|
| AWS_REGION             | AWS region                                                     | √         | N/A     | `eu-west-2`         |
| AWS_ACCESS_KEY_ID      | Part of AWS credentials.                                       | √         | N/A     | `ASIA...`           |
| AWS_SECRET_ACCESS_KEY  | Part of AWS credentials.                                       | √         | N/A     | `UgO8...`           |
| KEYSTORE_TYPE          | The type of keystore used to sign a document.                  | √         | N/A     | `pkcs12`            |
| KEYSTORE_PATH          | The path to the keystore used to sign a document.              | √         | N/A     | `src/test/resources/keystore.p12`     |
| KEYSTORE_PASSWORD      | The password to the keystore used to sign a document.          | √         | N/A     | `password`          |
| CERTIFICATE_ALIAS      | The unique string to identify the keystore.                    | √         | N/A     | `dockerkeystore`        |
| SIGNED_DOC_BUCKET_NAME | The name of the S3 bucket used for storing signed documents.   | √         | N/A     | `document-signing-api` |
| COVERSHEET_IMAGES_PATH | The path to the directory containing images for a cover sheet. | X         | ""      | `src/main/resources/coversheet` |

## Endpoints
| Path                           | Method | Description                                                         |
|--------------------------------|--------|---------------------------------------------------------------------|
| *`/document-signing/sign-pdf`* | POST   | Signs an existing PDF document.                                     |
| *`/healthcheck`*               | GET    | Returns HTTP OK (`200`) to indicate a healthy application instance. |

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



## Terraform ECS

### What does this code do?

The code present in this repository is used to define and deploy a dockerised container in AWS ECS.
This is done by calling a [module](https://github.com/companieshouse/terraform-modules/tree/main/aws/ecs) from terraform-modules. Application specific attributes are injected and the service is then deployed using Terraform via the CICD platform 'Concourse'.


Application specific attributes | Value                                | Description
:---------|:-----------------------------------------------------------------------------|:-----------
**ECS Cluster**        |order-service                                     | ECS cluster (stack) the service belongs to
**Load balancer**      |{env}-chs-apichgovuk <br> {env}-chs-apichgovuk-private                                       | The load balancer that sits in front of the service
**Concourse pipeline**     |[Pipeline link](https://ci-platform.companieshouse.gov.uk/teams/team-development/pipelines/document-signing-api ) <br> [Pipeline code](https://github.com/companieshouse/ci-pipelines/blob/master/pipelines/ssplatform/team-development/document-signing-api )                                  | Concourse pipeline link in shared services


### Contributing
- Please refer to the [ECS Development and Infrastructure Documentation](https://companieshouse.atlassian.net/wiki/spaces/DEVOPS/pages/4390649858/Copy+of+ECS+Development+and+Infrastructure+Documentation+Updated) for detailed information on the infrastructure being deployed.

### Testing
- Ensure the terraform runner local plan executes without issues. For information on terraform runners please see the [Terraform Runner Quickstart guide](https://companieshouse.atlassian.net/wiki/spaces/DEVOPS/pages/1694236886/Terraform+Runner+Quickstart).
- If you encounter any issues or have questions, reach out to the team on the **#platform** slack channel.

### Vault Configuration Updates
- Any secrets required for this service will be stored in Vault. For any updates to the Vault configuration, please consult with the **#platform** team and submit a workflow request.

### Useful Links
- [ECS service config dev repository](https://github.com/companieshouse/ecs-service-configs-dev)
- [ECS service config production repository](https://github.com/companieshouse/ecs-service-configs-production)