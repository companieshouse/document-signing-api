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
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.net.URISyntaxException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class S3ServiceTest {

    /**
     * Unsigned documents are typically found nested in "directories" such as
     * "docs/--5p23aItPJhX1GtWC3FPX0pnAo-AsEMejG9aNCvVRA/"
     */
    private static final String NESTED_UNSIGNED_DOCUMENT_LOCATION_S3_URI =
            "s3://document-api-images-cidev/docs/--5p23aItPJhX1GtWC3FPX0pnAo-AsEMejG9aNCvVRA/application-pdf";

    /**
     * NOT a typical location for an unsigned document.
     */
    private static final String UNNESTED_UNSIGNED_DOCUMENT_LOCATION_S3_URI =
            "s3://document-api-images-cidev/9616659670.pdf";
    private static final String UNSIGNED_DOCUMENT_LOCATION_OBJECT_URL =
            "https://document-api-images-cidev.s3.eu-west-2.amazonaws.com/9616659670.pdf";
    private static final String INVALID_UNSIGNED_DOCUMENT_LOCATION_SYNTAX =
            "s3:// document-api-images-cidev/9616659670.pdf";

    private static final GetObjectRequest EXPECTED_NESTED_DOCUMENT_GET_OBJECT_REQUEST = GetObjectRequest.builder()
            .bucket("document-api-images-cidev")
            .key("docs/--5p23aItPJhX1GtWC3FPX0pnAo-AsEMejG9aNCvVRA/application-pdf")
            .build();

    private static final GetObjectRequest EXPECTED_UNNESTED_DOCUMENT_GET_OBJECT_REQUEST = GetObjectRequest.builder()
            .bucket("document-api-images-cidev")
            .key("9616659670.pdf")
            .build();

    @InjectMocks
    private S3Service s3Service;

    @Mock
    private S3Client s3Client;

    @Mock
    private ResponseInputStream<GetObjectResponse> response;

    @Test
    @DisplayName("retrieveUnsignedDocument delegates nested document retrieval to GetObject correctly")
    void delegatesNestedDocumentRetrievalToGetObject() throws Exception {
        when(s3Client.getObject(any(GetObjectRequest.class))).thenReturn(response);

        final ResponseInputStream<GetObjectResponse> retrieved =
                s3Service.retrieveUnsignedDocument(NESTED_UNSIGNED_DOCUMENT_LOCATION_S3_URI);
        assertThat(retrieved, is(response));
        verify(s3Client).getObject(EXPECTED_NESTED_DOCUMENT_GET_OBJECT_REQUEST);
    }

    @Test
    @DisplayName("retrieveUnsignedDocument delegates un-nested document retrieval to GetObject correctly")
    void delegatesUnnestedDocumentRetrievalToGetObject() throws Exception {
        when(s3Client.getObject(any(GetObjectRequest.class))).thenReturn(response);

        final ResponseInputStream<GetObjectResponse> retrieved =
                s3Service.retrieveUnsignedDocument(UNNESTED_UNSIGNED_DOCUMENT_LOCATION_S3_URI);
        assertThat(retrieved, is(response));
        verify(s3Client).getObject(EXPECTED_UNNESTED_DOCUMENT_GET_OBJECT_REQUEST);
    }

    @Test
    @DisplayName("retrieveUnsignedDocument throws URISyntaxException where scheme is not s3")
    void throwsURISyntaxExceptionWhereSchemeIsNotS3() {
        final URISyntaxException exception = assertThrows(URISyntaxException.class,
                () -> s3Service.retrieveUnsignedDocument(UNSIGNED_DOCUMENT_LOCATION_OBJECT_URL));
        assertThat(exception.getMessage(),
                is("The document location provided is not a valid S3 URI: " +
                        "https://document-api-images-cidev.s3.eu-west-2.amazonaws.com/9616659670.pdf"));
    }

    @Test
    @DisplayName("retrieveUnsignedDocument propagates URISyntaxException caused by invalid URI syntax")
    void propagatesURISyntaxExceptionCausedByInvalidURISyntax() {
        final URISyntaxException exception = assertThrows(URISyntaxException.class,
                () -> s3Service.retrieveUnsignedDocument(INVALID_UNSIGNED_DOCUMENT_LOCATION_SYNTAX));
        assertThat(exception.getMessage(),
                is("Illegal character in authority at index 5: s3:// document-api-images-cidev/9616659670.pdf"));
    }

    @Test
    @DisplayName("storeSignedDocument stores signed document in named bucket")
    void storesSignedDocumentInNamedBucket() {
        final S3Service serviceUnderTest = new S3Service(s3Client, "bucket");

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