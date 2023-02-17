package uk.gov.companieshouse.documentsigningapi.aws;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicSessionCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.URISyntaxException;

@Component
public class S3Client {
    public S3ObjectInputStream retrieveUnsignedDocument(final String documentLocation) throws URISyntaxException {
        final String bucketName = getBucketName(documentLocation);
        final String fileName = getFileName(documentLocation);

        // TODO DCAC-76: Can we replace session credentials with configured non-expiring credentials?
        final String region = System.getenv("ENV_REGION_AWS");
        final String accessKeyId = System.getenv("AWS_ACCESS_KEY_ID");
        final String secretAccessKey = System.getenv("AWS_SECRET_ACCESS_KEY");
        final String sessionKey = System.getenv("AWS_SESSION_TOKEN");
        final AWSCredentials credentials = new BasicSessionCredentials(
                accessKeyId,
                secretAccessKey,
                sessionKey);
        final AmazonS3 s3 = AmazonS3ClientBuilder.standard().
                withRegion(region).
                withCredentials(new AWSStaticCredentialsProvider(credentials)).
                build();
        final S3Object o = s3.getObject(bucketName, fileName);
        return o.getObjectContent();
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
