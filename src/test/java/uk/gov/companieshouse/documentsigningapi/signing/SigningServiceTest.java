package uk.gov.companieshouse.documentsigningapi.signing;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.documentsigningapi.exception.DocumentSigningException;
import uk.gov.companieshouse.documentsigningapi.exception.DocumentUnavailableException;
import uk.gov.companieshouse.documentsigningapi.logging.LoggingUtils;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class SigningServiceTest {

    private static class TestSigningService extends SigningService {
        public TestSigningService(String keystoreType,
                                  String keystorePath,
                                  String keystorePassword,
                                  String certificateAlias,
                                  LoggingUtils logger) {
            super(keystoreType, keystorePath, keystorePassword, certificateAlias, logger);
        }

        @Override
        protected void logError(Exception exception) {
            // DOES NOTHING HERE
        }
    }

    @Mock
    private LoggingUtils logger;

    @InjectMocks
    private SigningService signingService =
            new TestSigningService("jks", "src/test/resources/keystore.jks", "testkey", "alias", logger);

    @Test
    @DisplayName("Throws DocumentUnavailableException when unable to load keystore")
    void throwsDocumentUnavailableExceptionExceptionWhenUnableToLoadKeystore() {
        final DocumentUnavailableException exception = assertThrows(DocumentUnavailableException.class,
            () -> signingService.signPDF(new byte[]{}));
        assertThat(exception.getMessage(),
            is("Unable to load Keystore or Certificate"));
    }

    @Test
    @DisplayName("Throws DocumentSigningException when failing to obtain keystore")
    void throwsDocumentSigningExceptionExceptionWhenFailingToObtainKeystore() {
        SigningService incorrectKeystoreTypeInitialised =
                new TestSigningService("unknown", "src/test/resources/keystore.jks", "testkey", "alias", logger);
        final DocumentSigningException exception = assertThrows(DocumentSigningException.class,
            () -> incorrectKeystoreTypeInitialised.signPDF(new byte[]{}));
        assertThat(exception.getMessage(),
            is("Failed to obtain proper KeyStore or Certificate"));
    }
}
