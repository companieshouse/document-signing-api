package uk.gov.companieshouse.documentsigningapi.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.core.SdkSystemSetting;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.sts.StsClient;
import software.amazon.awssdk.services.sts.auth.StsGetSessionTokenCredentialsProvider;
import uk.gov.companieshouse.documentsigningapi.coversheet.ImagesBean;

@Configuration
public class DocumentSigningApiConfiguration {

    @Value("${environment.coversheet.images.path}")
    private String imagesPath;

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

    @Bean
    public ImagesBean imagesBean() {
        return new ImagesBean(imagesPath);
    }

    protected String getRegion() {
        return System.getenv(SdkSystemSetting.AWS_REGION.environmentVariable());
    }

}
