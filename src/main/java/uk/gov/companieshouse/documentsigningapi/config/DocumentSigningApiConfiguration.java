package uk.gov.companieshouse.documentsigningapi.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.EnvironmentVariableCredentialsProvider;
import software.amazon.awssdk.core.SdkSystemSetting;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration
public class DocumentSigningApiConfiguration {

    @Bean
    public S3Client s3Client() {
        // TODO We will need to replace the approach used here with one based on something like the
        // StsGetSessionTokenCredentialsProvider using only the access key and secret key of an IAM user
        // or role. We cannot do that until we have used terraform in the Concourse pipeline to
        // set up that user or role for development and higher environments.
        return S3Client.builder().
                region(Region.of(getRegion())).
                credentialsProvider(EnvironmentVariableCredentialsProvider.create())
                .build();
    }

    protected String getRegion() {
        return System.getenv(SdkSystemSetting.AWS_REGION.environmentVariable());
    }

}
