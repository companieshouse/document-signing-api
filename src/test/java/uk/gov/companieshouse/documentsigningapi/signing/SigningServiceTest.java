package uk.gov.companieshouse.documentsigningapi.signing;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import uk.gov.companieshouse.documentsigningapi.exception.DocumentSigningException;
import uk.gov.companieshouse.documentsigningapi.exception.DocumentUnavailableException;
import uk.gov.companieshouse.documentsigningapi.logging.LoggingUtils;

@ExtendWith(MockitoExtension.class)
public class SigningServiceTest {

    @Mock
    private LoggingUtils logger;

    @Mock
    private ResponseInputStream<GetObjectResponse> response;

    @InjectMocks
    private SigningService signingService = new SigningService("jks", "src/test/resources/keystore.jks", "testkey", "alias", logger);

//    @Test
//    @DisplayName("Test signPDF returns a signed pdf")
//    void successfulSigningOfPDF() throws Exception {
//
//        KeyStoreSpi keyStoreSpiMock = mock(KeyStoreSpi.class);
//        KeyStore keyStoreMock = new KeyStore(keyStoreSpiMock, null, "jks"){ };
//        keyStoreMock.load(any());
//
//
//
//        byte[] signedPDF = signingService.signPDF(response);
//    }

    @Test
    @DisplayName("Throws DocumentUnavailableException when unable to load keystore")
    void throwsDocumentUnavailableExceptionExceptionWhereHostIsNull() {
        final DocumentUnavailableException exception = assertThrows(DocumentUnavailableException.class,
            () -> signingService.signPDF(response));
        assertThat(exception.getMessage(),
            is("Unable to load Keystore or Certificate"));
    }

    @Test
    @DisplayName("Throws DocumentSigningException when failing to obtain keystore")
    void throwsDocumentSigningExceptionExceptionWhereHostIsNull() {
        SigningService incorrectKeystoreTypeInitialised = new SigningService("unknown", "src/test/resources/keystore.jks", "testkey", "alias", logger);
        final DocumentSigningException exception = assertThrows(DocumentSigningException.class,
            () -> incorrectKeystoreTypeInitialised.signPDF(response));
        assertThat(exception.getMessage(),
            is("Failed to obtain proper KeyStore or Certificate"));
    }
}
