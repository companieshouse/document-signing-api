package uk.gov.companieshouse.documentsigningapi.config;

import static uk.gov.companieshouse.documentsigningapi.environment.EnvironmentVariablesChecker.RequiredEnvironmentVariables.AWS_REGION;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.sts.StsClient;
import software.amazon.awssdk.services.sts.auth.StsGetSessionTokenCredentialsProvider;
import uk.gov.companieshouse.environment.EnvironmentReader;

@Configuration
public class S3ClientConfig {

    private final EnvironmentReader environmentReader;

    @Autowired
    public S3ClientConfig(EnvironmentReader environmentReader) {
        this.environmentReader = environmentReader;
    }

    @Bean
    public S3Client s3Client() {
        final var stsClient = StsClient.builder().region(Region.of(getRegion())).build();
        final var stsGetSessionTokenCredentialsProvider =
                StsGetSessionTokenCredentialsProvider.builder()
                .stsClient(stsClient).build();
        return S3Client.builder().region(Region.of(getRegion()))
                .credentialsProvider(stsGetSessionTokenCredentialsProvider).build();
    }

    protected String getRegion() {
        return environmentReader.getMandatoryString(AWS_REGION.getName());
    }
}
