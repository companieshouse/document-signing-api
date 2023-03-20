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
import uk.gov.companieshouse.documentsigningapi.util.ImagesBean;
import uk.gov.companieshouse.documentsigningapi.util.OrdinalDateTimeFormatter;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class SigningServiceTest {

    @Mock
    private LoggingUtils logger;

    @Mock
    private ImagesBean images;

    @Mock
    private OrdinalDateTimeFormatter formatter;

    @InjectMocks
    private SigningService signingService =
            new SigningService(
                    "jks",
                    "src/test/resources/keystore.jks",
                    "testkey",
                    "alias",
                    logger,
                    images,
                    formatter);

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
                new SigningService(
                        "unknown",
                        "src/test/resources/keystore.jks",
                        "testkey",
                        "alias",
                        logger,
                        images,
                        formatter);
        final DocumentSigningException exception = assertThrows(DocumentSigningException.class,
            () -> incorrectKeystoreTypeInitialised.signPDF(new byte[]{}));
        assertThat(exception.getMessage(),
            is("Failed to obtain proper KeyStore or Certificate"));
    }
}
