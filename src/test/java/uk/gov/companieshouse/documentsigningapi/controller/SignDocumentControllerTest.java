package uk.gov.companieshouse.documentsigningapi.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.NoSuchBucketException;
import uk.gov.companieshouse.documentsigningapi.aws.S3Service;
import uk.gov.companieshouse.documentsigningapi.coversheet.CoverSheetService;
import uk.gov.companieshouse.documentsigningapi.dto.CoverSheetDataDTO;
import uk.gov.companieshouse.documentsigningapi.dto.SignPdfRequestDTO;
import uk.gov.companieshouse.documentsigningapi.logging.LoggingUtils;
import uk.gov.companieshouse.documentsigningapi.signing.SigningService;
import uk.gov.companieshouse.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import java.net.URISyntaxException;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;
import static uk.gov.companieshouse.documentsigningapi.util.TestConstants.*;

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

    @Mock
    private CoverSheetService coverSheetService;

    @Mock
    private ResponseInputStream<GetObjectResponse> unsignedDocument;

    @Mock
    private SigningService signingService;

    @Mock
    private HttpServletRequest request;

    @Test
    @DisplayName("signPdf reports URISyntaxException as a bad request (400)")
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
    @DisplayName("signPdf reports SdkServiceException with its own status code")
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
    @DisplayName("signPdf reports SdkException as an internal server error (500)")
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

    @Test
    @DisplayName("signPdf adds cover sheet if required")
    void addsCoverSheetIfRequired() throws Exception {
        when(s3Service.retrieveUnsignedDocument(anyString())).thenReturn(unsignedDocument);
        when(coverSheetService.addCoverSheet(any(byte[].class), any(CoverSheetDataDTO.class))).thenReturn(new byte[]{});
        when(unsignedDocument.readAllBytes()).thenReturn(new byte[]{});
        when(loggingUtils.getLogger()).thenReturn(logger);
        //
        // ERIC headers for auth-auth
        //
        lenient().doReturn(ERIC_IDENTITY_HEADER_VALUE).when(request).getHeader(ERIC_IDENTITY_HEADER_NAME);
        lenient().doReturn(ERIC_IDENTITY_HEADER_VALUE).when(request).getHeader(ERIC_IDENTITY_TYPE_HEADER_NAME);
        lenient().doReturn(ERIC_AUTHORIZED_KEY_ROLES_VALUE).when(request).getHeader(ERIC_AUTHORIZED_KEY_ROLES );

        final SignPdfRequestDTO signPdfRequestDTO = new SignPdfRequestDTO();
        signPdfRequestDTO.setDocumentLocation(TOKEN_UNSIGNED_DOCUMENT_LOCATION);
        signPdfRequestDTO.setDocumentType("certified-copy");
        signPdfRequestDTO.setSignatureOptions(List.of("cover-sheet"));
        signPdfRequestDTO.setCoverSheetData(new CoverSheetDataDTO());

        controller.signPdf(signPdfRequestDTO);

        verify(coverSheetService).addCoverSheet(any(byte[].class), any(CoverSheetDataDTO.class));

    }

    @Test
    @DisplayName("signPdf does not add cover sheet if not required")
    void doesNotAddCoverSheetIfNotRequired() throws Exception {
        when(s3Service.retrieveUnsignedDocument(anyString())).thenReturn(unsignedDocument);
        when(loggingUtils.getLogger()).thenReturn(logger);
        //
        // ERIC headers for auth-auth
        //
        lenient().doReturn(ERIC_IDENTITY_HEADER_VALUE).when(request).getHeader(ERIC_IDENTITY_HEADER_NAME);
        lenient().doReturn(ERIC_IDENTITY_HEADER_VALUE).when(request).getHeader(ERIC_IDENTITY_TYPE_HEADER_NAME);
        lenient().doReturn(ERIC_AUTHORIZED_KEY_ROLES_VALUE).when(request).getHeader(ERIC_AUTHORIZED_KEY_ROLES );

        final SignPdfRequestDTO signPdfRequestDTO = new SignPdfRequestDTO();
        signPdfRequestDTO.setDocumentLocation(TOKEN_UNSIGNED_DOCUMENT_LOCATION);
        signPdfRequestDTO.setDocumentType("certified-copy");

        controller.signPdf(signPdfRequestDTO);

        verify(coverSheetService, times(0))
                .addCoverSheet(any(byte[].class), any(CoverSheetDataDTO.class));
    }

}