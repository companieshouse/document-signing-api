package uk.gov.companieshouse.documentsigningapi.interceptor;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import uk.gov.companieshouse.documentsigningapi.logging.LoggingUtils;
import uk.gov.companieshouse.documentsigningapi.logging.LoggingUtilsConfiguration;
import uk.gov.companieshouse.documentsigningapi.util.EricHeaderHelper;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

import static org.springframework.http.HttpStatus.UNAUTHORIZED;
import static uk.gov.companieshouse.documentsigningapi.logging.LoggingUtilsConfiguration.*;

@Component
public class UserAuthenticationInterceptor implements HandlerInterceptor {
    private final LoggingUtils logger;

    public UserAuthenticationInterceptor(LoggingUtils logger) {
        this.logger = logger;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        Map<String, Object> logMap = new HashMap<>();
        logMap.put(REQUEST_ID_LOG_KEY, request.getHeader(REQUEST_ID_HEADER_NAME));
        //
        // Any value will do for ERIC-Identity-Type so long as it is NOT empty.
        //
        String identityType = EricHeaderHelper.getIdentityType(request);
        if(identityType == null) {
            logMap.put(IDENTITY_TYPE_LOG_KEY, MISSING_REQUIRED_INFO);
            logMap.put(STATUS_LOG_KEY, UNAUTHORIZED);
            logger.getLogger().error("UserAuthenticationInterceptor error: no ERIC-Identity-Type", logMap);
            response.setStatus(UNAUTHORIZED.value());   // 401
            return(false);  // NOT Authenticated.
        }
        //
        // Any value will do for ERIC-Identity so long as it is NOT empty.
        //
        String identity = EricHeaderHelper.getIdentity(request);
        if(identity == null) {
            logMap.put(IDENTITY_LOG_KEY, MISSING_REQUIRED_INFO);
            logMap.put(STATUS_LOG_KEY, UNAUTHORIZED);
            logger.getLogger().error("UserAuthenticationInterceptor error: no ERIC-Identity", logMap);
            response.setStatus(UNAUTHORIZED.value());   // 401
            return(false);  // NOT Authenticated.
        }

        return(true);   // Authenticated OK.
    }
}
