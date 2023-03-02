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

    private final String signedDocBucketName;

    private final String signedDocStoragePrefix;

    public S3Service(S3Client s3Client,
                     @Value("${environment.signed.doc.bucket.name}") String signedDocBucketName,
                     @Value("${environment.signed.doc.storage.prefix}") String signedDocStoragePrefix) {
        this.s3Client = s3Client;
        this.signedDocBucketName = signedDocBucketName;
        this.signedDocStoragePrefix = signedDocStoragePrefix;
    }

    /**
     * Retrieves the document from the location specified in S3.
     * @param documentLocation the document location, assumed to be the location of an (unsigned) document stored in S3,
     *                         specified as an object URL string
     * @return {@link ResponseInputStream} of {@link GetObjectResponse} containing a reference to the document
     * @throws URISyntaxException should there be an issue parsing the S3 bucket name or S3 key name (a.k.a file path)
     * from the document location provided
     */
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

    /**
     * Writes the (signed) document content provided to a specific location in a specific S3 bucket.
     * The bucket name is obtained from configuration. The location (S3 key) is derived from configuration, the folder
     * name and the filename.
     * @param signedDocument byte array containing the signed document content
     * @param folderName the name of the folder within which the document will be stored in the S3 bucket
     * @param fileName the name given to the file stored in the S3 bucket
     * @return the location of signed document is stored in S3, as an object URL string
     */
    public String storeSignedDocument(final byte[] signedDocument, final String folderName, final String fileName) {
        final var filePath = isEmpty(signedDocStoragePrefix) ?
                folderName + DIRECTORY_SEPARATOR + fileName :
                signedDocStoragePrefix + DIRECTORY_SEPARATOR + folderName + DIRECTORY_SEPARATOR +  fileName;
        final var putObjectRequest = PutObjectRequest.builder()
                .bucket(signedDocBucketName)
                .key(filePath)
                .build();
        s3Client.putObject(putObjectRequest, RequestBody.fromBytes(signedDocument));
        final var s3Utilities = s3Client.utilities();
        final var getUrlRequest = GetUrlRequest.builder()
                .bucket(signedDocBucketName)
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
