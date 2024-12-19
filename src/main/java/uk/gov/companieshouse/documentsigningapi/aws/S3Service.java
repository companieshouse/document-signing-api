package uk.gov.companieshouse.documentsigningapi.aws;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.net.URI;
import java.net.URISyntaxException;

@Component
public class S3Service {

    private static final String DIRECTORY_SEPARATOR = "/";

    private final S3Client s3Client;

    private final String signedDocBucketName;

    @Autowired
    public S3Service(S3Client s3Client,
                     @Value("${environment.signed.doc.bucket.name}") String signedDocBucketName) {
        this.s3Client = s3Client;
        this.signedDocBucketName = signedDocBucketName;
    }

    /**
     * Retrieves the document from the location specified in S3.
     * @param documentLocation the document location, assumed to be the location of an (unsigned) document stored in S3,
     *                         specified as an S3 URI string
     * @return {@link ResponseInputStream} of {@link GetObjectResponse} containing a reference to the document
     * @throws URISyntaxException should there be an issue parsing the S3 bucket name or S3 key name (a.k.a file path)
     * from the document location provided
     */
    public ResponseInputStream<GetObjectResponse> retrieveUnsignedDocument(final String documentLocation)
            throws URISyntaxException {
        final String bucketName = getBucketName(documentLocation);
        final String key = getKey(documentLocation);
        final var getObjectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();
        return s3Client.getObject(getObjectRequest);
    }

    /**
     * Writes the (signed) document content provided to a specific location in a specific S3 bucket.
     * The bucket name is obtained from configuration. The location (S3 key) is derived from the prefix
     * (effectively a path) and the key (effectively a filename).
     * @param signedDocument byte array containing the signed document content
     * @param prefix the path of the "folder" within which the document will be stored in the S3 bucket
     * @param key the name given to the object (file) stored in the S3 bucket (i.e., the document filename)
     * @return the location of signed document stored in S3, as an S3 URI string
     */
    public String storeSignedDocument(final byte[] signedDocument, final String prefix, final String key) {
        final var filePath = prefix + DIRECTORY_SEPARATOR + key;
        final var putObjectRequest = PutObjectRequest.builder()
                .bucket(signedDocBucketName)
                .key(filePath)
                .build();
        s3Client.putObject(putObjectRequest, RequestBody.fromBytes(signedDocument));
        // There seems to be no obvious way to infer the S3 URI "officially" through the SDK.
        return "s3://" + signedDocBucketName + "/" + filePath;
    }

    String getBucketName(final String documentLocation) throws URISyntaxException {
        final var uri = new URI(documentLocation);
        if (uri.getScheme() == null || !uri.getScheme().equals("s3")) {
            throw new URISyntaxException(documentLocation, "The document location provided is not a valid S3 URI");
        }
        return uri.getHost();
    }

    String getKey(final String documentLocation) throws URISyntaxException {
        final var uri = new URI(documentLocation);
        final String path = uri.getPath();
        return path.substring(1);
    }
}
