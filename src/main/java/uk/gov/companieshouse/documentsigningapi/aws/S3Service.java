package uk.gov.companieshouse.documentsigningapi.aws;

import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.SdkSystemSetting;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.sts.StsClient;
import software.amazon.awssdk.services.sts.auth.StsGetSessionTokenCredentialsProvider;

import java.net.URI;
import java.net.URISyntaxException;

@Component
public class S3Service {
    public ResponseInputStream<GetObjectResponse> retrieveUnsignedDocument(final String documentLocation)
            throws URISyntaxException {
        final String bucketName = getBucketName(documentLocation);
        final String fileName = getFileName(documentLocation);

        // TODO DCAC-76: Can we replace session credentials with configured non-expiring credentials?
        final String region = System.getenv(SdkSystemSetting.AWS_REGION.environmentVariable());

        final StsClient stsClient = StsClient.builder()
                .region(Region.of(region))
                .build();
        final StsGetSessionTokenCredentialsProvider stsGetSessionTokenCredentialsProvider =
                StsGetSessionTokenCredentialsProvider.builder().
                        stsClient(stsClient)
                        .build();

        // TODO DCAC-76: Remove this!
        // Examining caller identity to troubleshoot GetSessionToken failures.
        System.out.println("Caller identity = " + stsClient.getCallerIdentity());

        final S3Client client = S3Client.builder().
                region(Region.of(region)).
                credentialsProvider(stsGetSessionTokenCredentialsProvider)
                .build();
        final GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(fileName)
                .build();
        return client.getObject(getObjectRequest);
    }

    String getBucketName(final String documentLocation) throws URISyntaxException {
        final String host = new URI(documentLocation).getHost();
        if (host == null) {
            throw new URISyntaxException(documentLocation,
                                         "No bucket name could be extracted from the document location.");
        }
        return host.substring(0, host.indexOf('.'));
    }

    String getFileName(final String documentLocation) throws URISyntaxException {
        final URI uri = new URI(documentLocation);
        final String path = uri.getPath();
        return path.substring(path.lastIndexOf('/') + 1);
    }
}
