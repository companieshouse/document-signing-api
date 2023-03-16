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
import static uk.gov.companieshouse.documentsigningapi.util.TestConstants.ERIC_AUTHORIZED_KEY_ROLES;

@ExtendWith(MockitoExtension.class)
public class UserAuthorisationInterceptorTest {
    @InjectMocks
    private UserAuthorisationInterceptor userAuthorisationInterceptor;
    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;

    @Test
    @DisplayName("preHandle ERIC-Authorised-Key-Roles is present")
    void willAuthorise_IfEricHeadersArePresent() throws Exception {
        lenient()
            .doReturn("*")
            .when(request)
            .getHeader(ERIC_AUTHORIZED_KEY_ROLES );

        assertTrue(userAuthorisationInterceptor.preHandle(request, response, null));
    }
    @Test
    @DisplayName("preHandle ERIC-Authorised-Key-Roles is NOT present")
    void willNotAuthorise_IfEricHeadersAreNotPresent() throws Exception {
        lenient()
            .doReturn(null)
            .when(request)
            .getHeader(ERIC_AUTHORIZED_KEY_ROLES);

        assertFalse(userAuthorisationInterceptor.preHandle(request, response, null));
    }
}
