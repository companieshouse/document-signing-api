package uk.gov.companieshouse.documentsigningapi.interceptor;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.AsyncHandlerInterceptor;
import org.springframework.web.servlet.HandlerInterceptor;
import uk.gov.companieshouse.api.util.security.AuthorisationUtil;
import uk.gov.companieshouse.documentsigningapi.logging.LoggingUtils;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

import static org.springframework.http.HttpStatus.UNAUTHORIZED;
import static uk.gov.companieshouse.documentsigningapi.logging.LoggingUtilsConfiguration.*;
import static uk.gov.companieshouse.documentsigningapi.util.EricHeaderHelper.ERIC_AUTHORISED_KEY_ROLES;

@Component
public class UserAuthorisationInterceptor implements AsyncHandlerInterceptor {
    private final LoggingUtils logger;

    public UserAuthorisationInterceptor(LoggingUtils logger) {
        this.logger = logger;
    }
    /**
     * Requires ERIC-Authorised-Key-Roles header with value "*" to be Authorised.
     * @return true if ERIC-Authorised-Key-Roles="*" or false and sets response status to UNAUTHORIZED
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if(hasInternalUserRole(request))
            return(true);

        response.setStatus(UNAUTHORIZED.value());
        return(false);
    }
    /**
     * @param request HttpServletRequest
     * @return true if ERIC-Authorised-Key-Roles="*" or false
     */
    private boolean hasInternalUserRole(HttpServletRequest request) {
        Map<String, Object> logMap = new HashMap<>();
        logMap.put(REQUEST_ID_LOG_KEY, request.getHeader(REQUEST_ID_HEADER_NAME));

        if(!AuthorisationUtil.hasInternalUserRole(request)) {
            logMap.put(STATUS_LOG_KEY, UNAUTHORIZED);
            logger.getLogger().error("UserAuthorisationInterceptor error: missing " + ERIC_AUTHORISED_KEY_ROLES, logMap);
            return(false);  // NOT Authorised.
        }

        return(true);   // Authorised OK.
    }
}
