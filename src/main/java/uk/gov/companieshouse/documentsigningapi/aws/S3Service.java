package uk.gov.companieshouse.documentsigningapi.aws;

import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

import java.net.URI;
import java.net.URISyntaxException;

@Component
public class S3Service {

    private final S3Client s3Client;

    public S3Service(S3Client s3Client) {
        this.s3Client = s3Client;
    }

    public ResponseInputStream<GetObjectResponse> retrieveUnsignedDocument(final String documentLocation)
            throws URISyntaxException {
        final String bucketName = getBucketName(documentLocation);
        final String fileName = getFileName(documentLocation);
        final var getObjectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(fileName)
                .build();
        return s3Client.getObject(getObjectRequest);
    }

    String getBucketName(final String documentLocation) throws URISyntaxException {
        final String host = new URI(documentLocation).getHost();
        if (host == null) {
            throw new URISyntaxException(documentLocation,
                                         "No bucket name could be extracted from the document location");
        }
        return host.substring(0, host.indexOf('.'));
    }

    String getFileName(final String documentLocation) throws URISyntaxException {
        final var uri = new URI(documentLocation);
        final String path = uri.getPath();
        return path.substring(path.lastIndexOf('/') + 1);
    }
}
