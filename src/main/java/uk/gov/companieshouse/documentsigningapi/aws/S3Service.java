package uk.gov.companieshouse.documentsigningapi.aws;

import org.springframework.stereotype.Component;
import software.amazon.awssdk.auth.credentials.EnvironmentVariableCredentialsProvider;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

import java.net.URI;
import java.net.URISyntaxException;

@Component
public class S3Service {
    public ResponseInputStream<GetObjectResponse> retrieveUnsignedDocument(final String documentLocation)
            throws URISyntaxException {
        final String bucketName = getBucketName(documentLocation);
        final String fileName = getFileName(documentLocation);

        // TODO DCAC-76: Can we replace session credentials with configured non-expiring credentials?
        final String region = System.getenv("ENV_REGION_AWS");
        final S3Client client = S3Client.builder().
                region(Region.of(region)).
                credentialsProvider(EnvironmentVariableCredentialsProvider.create()).build();
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
