package uk.gov.companieshouse.documentsigningapi.aws;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Utilities;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.GetUrlRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.net.URISyntaxException;
import java.net.URL;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class S3ServiceTest {

    private static final String VALID_UNSIGNED_DOCUMENT_LOCATION =
            "https://document-api-images-cidev.s3.eu-west-2.amazonaws.com/9616659670.pdf";
    private static final String INVALID_UNSIGNED_DOCUMENT_LOCATION_WITHOUT_HOST =
            "here";
    private static final String INVALID_UNSIGNED_DOCUMENT_LOCATION_SYNTAX =
            "https:// document-api-images-cidev.s3.eu-west-2.amazonaws.com/9616659670.pdf";

    @InjectMocks
    private S3Service s3Service;

    @Mock
    private S3Client s3Client;

    @Mock
    private ResponseInputStream<GetObjectResponse> response;

    @Mock
    private S3Utilities s3Utilities;

    @Mock
    private URL signedDocumentLocationUrl;

    @Test
    @DisplayName("retrieveUnsignedDocument delegates retrieval to GetObject")
    void delegatesRetrievalToGetObject() throws Exception {
        when(s3Client.getObject(any(GetObjectRequest.class))).thenReturn(response);

        final ResponseInputStream<GetObjectResponse> retrieved =
                s3Service.retrieveUnsignedDocument(VALID_UNSIGNED_DOCUMENT_LOCATION);
        assertThat(retrieved, is(response));
        verify(s3Client).getObject(any(GetObjectRequest.class));
    }

    @Test
    @DisplayName("retrieveUnsignedDocument throws URISyntaxException where host is null")
    void throwsURISyntaxExceptionWhereHostIsNull() {
        final URISyntaxException exception = assertThrows(URISyntaxException.class,
                () -> s3Service.retrieveUnsignedDocument(INVALID_UNSIGNED_DOCUMENT_LOCATION_WITHOUT_HOST));
        assertThat(exception.getMessage(),
                is("No bucket name could be extracted from the document location: here"));
    }

    @Test
    @DisplayName("retrieveUnsignedDocument propagates URISyntaxException caused by invalid URI syntax")
    void propagatesURISyntaxExceptionCausedByInvalidURISyntax() {
        final URISyntaxException exception = assertThrows(URISyntaxException.class,
                () -> s3Service.retrieveUnsignedDocument(INVALID_UNSIGNED_DOCUMENT_LOCATION_SYNTAX));
        assertThat(exception.getMessage(),
                is("Illegal character in authority at index 8: " +
                        "https:// document-api-images-cidev.s3.eu-west-2.amazonaws.com/9616659670.pdf"));
    }

    @Test
    @DisplayName("storeSignedDocument stores signed document in named bucket")
    void storesSignedDocumentInNamedBucket() {
        final S3Service serviceUnderTest = new S3Service(s3Client, "bucket");
        when(s3Client.utilities()).thenReturn(s3Utilities);
        when(s3Utilities.getUrl(any(GetUrlRequest.class))).thenReturn(signedDocumentLocationUrl);

        serviceUnderTest.storeSignedDocument(new byte[]{}, "prefix/folder", "file");

        verifySignedDocumentWrittenToBucketAndFilepath("bucket", "prefix/folder/file");
    }

    private void verifySignedDocumentWrittenToBucketAndFilepath(final String expectedBucketName, final String expectedFilePath) {
        final PutObjectRequest expectedPutObjectRequest =
                PutObjectRequest.builder()
                        .bucket(expectedBucketName)
                        .key(expectedFilePath)
                        .build();
        verify(s3Client).putObject(eq(expectedPutObjectRequest), any(RequestBody.class));
    }

}