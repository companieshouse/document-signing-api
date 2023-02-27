package uk.gov.companieshouse.documentsigningapi.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.services.s3.model.NoSuchBucketException;
import uk.gov.companieshouse.documentsigningapi.aws.S3Service;
import uk.gov.companieshouse.documentsigningapi.dto.SignPdfRequestDTO;
import uk.gov.companieshouse.documentsigningapi.logging.LoggingUtils;
import uk.gov.companieshouse.logging.Logger;

import java.net.URISyntaxException;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Partially unit tests the {@link SignDocumentController} class.
 */
@ExtendWith(MockitoExtension.class)
class SignDocumentControllerTest {

    private static final String TOKEN_UNSIGNED_DOCUMENT_LOCATION =
            "Token unsigned document location";

    @InjectMocks
    private SignDocumentController controller;

    @Mock
    private S3Service s3Service;

    @Mock
    private LoggingUtils loggingUtils;

    @Mock
    private Logger logger;

    @Test
    @DisplayName("Reports URISyntaxException as a bad request (400)")
    void reportsURISyntaxExceptionAsABadRequest() throws Exception {
        when(s3Service.retrieveUnsignedDocument(anyString())).
                thenThrow(new URISyntaxException("Test exception", "Reason"));
        when(loggingUtils.getLogger()).thenReturn(logger);

        final SignPdfRequestDTO signPdfRequestDTO = new SignPdfRequestDTO();
        signPdfRequestDTO.setDocumentLocation(TOKEN_UNSIGNED_DOCUMENT_LOCATION);
        signPdfRequestDTO.setDocumentType("certified-copy");
        signPdfRequestDTO.setSignatureOptions(List.of("cover-sheet"));

        final ResponseEntity<Object> response = controller.signPdf(signPdfRequestDTO);
        assertThat(response.getStatusCode(), is(HttpStatus.BAD_REQUEST));
        assertThat(response.getBody(), is("Reason: Test exception"));
    }

    @Test
    @DisplayName("Reports SdkServiceException with its own status code")
    void reportsSdkServiceExceptionWithItsOwnStatusCode() throws Exception {
        // NoSuchBucketException is a kind of SdkServiceException.
        final NoSuchBucketException exception =
                NoSuchBucketException.builder()
                        .message("Test exception")
                        .statusCode(HttpStatus.NOT_FOUND.value())
                        .build();
        when(s3Service.retrieveUnsignedDocument(anyString())).
                thenThrow(exception);
        when(loggingUtils.getLogger()).thenReturn(logger);

        final SignPdfRequestDTO signPdfRequestDTO = new SignPdfRequestDTO();
        signPdfRequestDTO.setDocumentLocation(TOKEN_UNSIGNED_DOCUMENT_LOCATION);
        signPdfRequestDTO.setDocumentType("certified-copy");
        signPdfRequestDTO.setSignatureOptions(List.of("cover-sheet"));

        final ResponseEntity<Object> response = controller.signPdf(signPdfRequestDTO);
        assertThat(response.getStatusCode(), is(HttpStatus.NOT_FOUND));
        assertThat(response.getBody(), is("Test exception"));
    }

    @Test
    @DisplayName("Reports SdkException as an internal server error (500)")
    void reportsSdkExceptionAsAnInternalServerError() throws Exception {
        when(s3Service.retrieveUnsignedDocument(anyString())).
                thenThrow(SdkClientException.create("Test exception"));
        when(loggingUtils.getLogger()).thenReturn(logger);

        final SignPdfRequestDTO signPdfRequestDTO = new SignPdfRequestDTO();
        signPdfRequestDTO.setDocumentLocation(TOKEN_UNSIGNED_DOCUMENT_LOCATION);
        signPdfRequestDTO.setDocumentType("certified-copy");
        signPdfRequestDTO.setSignatureOptions(List.of("cover-sheet"));

        final ResponseEntity<Object> response = controller.signPdf(signPdfRequestDTO);
        assertThat(response.getStatusCode(), is(HttpStatus.INTERNAL_SERVER_ERROR));
        assertThat(response.getBody(), is("Test exception"));
    }

}