package uk.gov.companieshouse.documentsigningapi.interceptor;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;
import static uk.gov.companieshouse.documentsigningapi.util.TestConstants.ERIC_AUTHORIZED_KEY_ROLES;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.documentsigningapi.logging.LoggingUtils;
import uk.gov.companieshouse.logging.Logger;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@ExtendWith(MockitoExtension.class)
public class UserAuthorisationInterceptorTest {
    @InjectMocks
    private UserAuthorisationInterceptor userAuthorisationInterceptor;
    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;
    @Mock
    private Logger logger;
    @Mock
    private LoggingUtils loggingUtils;

    @Test
    @DisplayName("preHandle ERIC-Authorised-Key-Roles is present and CORRECT value")
    void willAuthoriseIfEricHeadersArePresentAndCorrectValue() throws Exception {
        lenient()
            .doReturn("*")
            .when(request)
            .getHeader(ERIC_AUTHORIZED_KEY_ROLES );

        assertTrue(userAuthorisationInterceptor.preHandle(request, response, null));
    }
    @Test
    @DisplayName("preHandle ERIC-Authorised-Key-Roles is MISSING")
    void willNotAuthoriseIfEricHeadersAreMissing() throws Exception {
        when(loggingUtils.getLogger()).thenReturn(logger);  // Required as preHandle failure triggers logging.
        lenient()
            .doReturn(null)
            .when(request)
            .getHeader(ERIC_AUTHORIZED_KEY_ROLES);

        assertFalse(userAuthorisationInterceptor.preHandle(request, response, null));
    }
    @Test
    @DisplayName("preHandle ERIC-Authorised-Key-Roles is present and INCORRECT value")
    void willNotAuthoriseIfEricHeadersPresentAndIncorrectValue() throws Exception {
        when(loggingUtils.getLogger()).thenReturn(logger);
        lenient()
            .doReturn("xxx")
            .when(request)
            .getHeader(ERIC_AUTHORIZED_KEY_ROLES);

        assertFalse(userAuthorisationInterceptor.preHandle(request, response, null));
    }
}
