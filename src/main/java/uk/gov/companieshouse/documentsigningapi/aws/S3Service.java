package uk.gov.companieshouse.documentsigningapi.aws;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.GetUrlRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.net.URI;
import java.net.URISyntaxException;

import static org.apache.commons.lang.StringUtils.isEmpty;

@Component
public class S3Service {

    private static final String DIRECTORY_SEPARATOR = "/";

    private final S3Client s3Client;

    private final String signedDocStoragePrefix;

    public S3Service(S3Client s3Client,
                     @Value("${environment.signed.doc.storage.prefix}") String signedDocStoragePrefix) {
        this.s3Client = s3Client;
        this.signedDocStoragePrefix = signedDocStoragePrefix;
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

    public String storeSignedDocument(final byte[] signedDocument, final String folderName, final String fileName) {
        final var bucketName = "document-signing-api";
        final var filePath = isEmpty(signedDocStoragePrefix) ?
                folderName + DIRECTORY_SEPARATOR + fileName :
                signedDocStoragePrefix + DIRECTORY_SEPARATOR + folderName + DIRECTORY_SEPARATOR +  fileName;
        final var putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(filePath)
                .build();
        s3Client.putObject(putObjectRequest, RequestBody.fromBytes(signedDocument));
        final var s3Utilities = s3Client.utilities();
        final var getUrlRequest = GetUrlRequest.builder()
                .bucket(bucketName)
                .key(filePath)
                .build();
        return s3Utilities.getUrl(getUrlRequest).toString();
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
