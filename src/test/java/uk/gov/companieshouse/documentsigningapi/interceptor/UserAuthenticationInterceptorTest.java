package uk.gov.companieshouse.documentsigningapi.interceptor;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.documentsigningapi.logging.LoggingUtils;
import uk.gov.companieshouse.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;
import static uk.gov.companieshouse.documentsigningapi.util.TestConstants.*;

@ExtendWith(MockitoExtension.class)
class UserAuthenticationInterceptorTest {
    @InjectMocks
    private UserAuthenticationInterceptor userAuthenticationInterceptor;
    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;
    @Mock
    private Logger logger;
    @Mock
    private LoggingUtils loggingUtils;

    @Test
    @DisplayName("Authentication : ERIC-Identity-Type ERIC-Identity present")
    void bothEricHeadersPresent() {
        lenient().doReturn(ERIC_IDENTITY_HEADER_VALUE).when(request).getHeader(ERIC_IDENTITY_HEADER_NAME);
        lenient().doReturn(ERIC_IDENTITY_HEADER_VALUE).when(request).getHeader(ERIC_IDENTITY_TYPE_HEADER_NAME);

        assertTrue(userAuthenticationInterceptor.preHandle(request, response, null));
    }
    @Test
    @DisplayName("Authentication : ERIC-Identity header EMPTY")
    void emptyEricIdentityHeader() {
        when(loggingUtils.getLogger()).thenReturn(logger);  // Required as preHandle failure triggers logging.
        lenient().doReturn("").when(request).getHeader(ERIC_IDENTITY_HEADER_NAME);
        lenient().doReturn(ERIC_IDENTITY_HEADER_VALUE).when(request).getHeader(ERIC_IDENTITY_TYPE_HEADER_NAME);

        assertFalse(userAuthenticationInterceptor.preHandle(request, response, null));
    }
    @Test
    @DisplayName("Authentication : ERIC-Identity-Type header EMPTY")
    void emptyEricIdentityTypeHeader() {
        when(loggingUtils.getLogger()).thenReturn(logger);
        lenient().doReturn(ERIC_IDENTITY_HEADER_VALUE).when(request).getHeader(ERIC_IDENTITY_HEADER_NAME);
        lenient().doReturn("").when(request).getHeader(ERIC_IDENTITY_TYPE_HEADER_NAME);

        assertFalse(userAuthenticationInterceptor.preHandle(request, response, null));
    }
    @Test
    @DisplayName("Authentication : ERIC headers EMPTY")
    void emptyBothEricHeaders() {
        when(loggingUtils.getLogger()).thenReturn(logger);
        lenient().doReturn("").when(request).getHeader(ERIC_IDENTITY_HEADER_NAME);
        lenient().doReturn("").when(request).getHeader(ERIC_IDENTITY_TYPE_HEADER_NAME);

        assertFalse(userAuthenticationInterceptor.preHandle(request, response, null));
    }
    @Test
    @DisplayName("Authentication : ERIC-Identity header MISSING")
    void missingEricIdentityHeader() {
        when(loggingUtils.getLogger()).thenReturn(logger);
        lenient().doReturn(null).when(request).getHeader(ERIC_IDENTITY_HEADER_NAME);
        lenient().doReturn(ERIC_IDENTITY_HEADER_VALUE).when(request).getHeader(ERIC_IDENTITY_TYPE_HEADER_NAME);

        assertFalse(userAuthenticationInterceptor.preHandle(request, response, null));
    }
    @Test
    @DisplayName("Authentication : ERIC-Identity-Type header MISSING")
    void missingEricIdentityTypeHeader() {
        when(loggingUtils.getLogger()).thenReturn(logger);
        lenient().doReturn(ERIC_IDENTITY_HEADER_VALUE).when(request).getHeader(ERIC_IDENTITY_HEADER_NAME);
        lenient().doReturn(null).when(request).getHeader(ERIC_IDENTITY_TYPE_HEADER_NAME);

        assertFalse(userAuthenticationInterceptor.preHandle(request, response, null));
    }
    @Test
    @DisplayName("Authentication : ERIC headers MISSING")
    void missingBothEricHeaders() {
        when(loggingUtils.getLogger()).thenReturn(logger);
        lenient().doReturn(null).when(request).getHeader(ERIC_IDENTITY_HEADER_NAME);
        lenient().doReturn(null).when(request).getHeader(ERIC_IDENTITY_TYPE_HEADER_NAME);

        assertFalse(userAuthenticationInterceptor.preHandle(request, response, null));
    }
}
