package uk.gov.companieshouse.documentsigningapi.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.core.SdkSystemSetting;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.sts.StsClient;
import software.amazon.awssdk.services.sts.auth.StsGetSessionTokenCredentialsProvider;

@Configuration
public class DocumentSigningApiConfiguration {

    @Bean
    public S3Client s3Client() {
        final var stsClient = StsClient.builder()
                .region(Region.of(getRegion()))
                .build();
        final var stsGetSessionTokenCredentialsProvider =
                StsGetSessionTokenCredentialsProvider.builder().
                        stsClient(stsClient)
                        .build();
        return S3Client.builder().
                region(Region.of(getRegion())).
                credentialsProvider(stsGetSessionTokenCredentialsProvider)
                .build();
    }

    protected String getRegion() {
        return System.getenv(SdkSystemSetting.AWS_REGION.environmentVariable());
    }

}
