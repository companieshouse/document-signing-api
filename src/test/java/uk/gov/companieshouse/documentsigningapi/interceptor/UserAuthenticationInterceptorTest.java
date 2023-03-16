package uk.gov.companieshouse.documentsigningapi.interceptor;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.lenient;
import static uk.gov.companieshouse.documentsigningapi.util.TestConstants.*;

@ExtendWith(MockitoExtension.class)
class UserAuthenticationInterceptorTest {
    @InjectMocks
    private UserAuthenticationInterceptor userAuthenticationInterceptor;
    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;

    @Test
    @DisplayName("Authentication : ERIC-Identity-Type ERIC-Identity present")
    void bothEricHeadersPresent() {
        lenient().doReturn(ERIC_IDENTITY_HEADER_VALUE).when(request).getHeader(ERIC_IDENTITY_HEADER_NAME);
        lenient().doReturn(ERIC_IDENTITY_HEADER_VALUE).when(request).getHeader(ERIC_IDENTITY_TYPE_HEADER_NAME);

        assertTrue(userAuthenticationInterceptor.preHandle(request, response, null));
    }
    @Test
    @DisplayName("Authentication : ERIC-Identity header missing")
    void missingEricIdentityHeader() {
        lenient().doReturn("").when(request).getHeader(ERIC_IDENTITY_HEADER_NAME);
        lenient().doReturn(ERIC_IDENTITY_HEADER_VALUE).when(request).getHeader(ERIC_IDENTITY_TYPE_HEADER_NAME);

        assertFalse(userAuthenticationInterceptor.preHandle(request, response, null));
    }
    @Test
    @DisplayName("Authentication : ERIC-Identity-Type header missing")
    void missingEricIdentityTypeHeader() {
        lenient().doReturn(ERIC_IDENTITY_HEADER_VALUE).when(request).getHeader(ERIC_IDENTITY_HEADER_NAME);
        lenient().doReturn("").when(request).getHeader(ERIC_IDENTITY_TYPE_HEADER_NAME);

        assertFalse(userAuthenticationInterceptor.preHandle(request, response, null));
    }
    @Test
    @DisplayName("Authentication : ERIC headers missing")
    void missingBothEricHeaders() {
        lenient().doReturn("").when(request).getHeader(ERIC_IDENTITY_HEADER_NAME);
        lenient().doReturn("").when(request).getHeader(ERIC_IDENTITY_TYPE_HEADER_NAME);

        assertFalse(userAuthenticationInterceptor.preHandle(request, response, null));
    }
}
