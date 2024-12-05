package uk.gov.companieshouse.documentsigningapi.util;

import org.apache.commons.lang.StringUtils;
import jakarta.servlet.http.HttpServletRequest;

public class EricHeaderHelper {
    public static String ERIC_IDENTITY             = "ERIC-Identity";
    public static String ERIC_IDENTITY_TYPE        = "ERIC-Identity-Type";
    public static String ERIC_AUTHORISED_KEY_ROLES = "ERIC-Authorised-Key-Roles";

    private EricHeaderHelper() { }

    public static String getIdentity(HttpServletRequest request) {
        return getHeader(request, ERIC_IDENTITY);
    }

    public static String getIdentityType(HttpServletRequest request) {
        return getHeader(request, ERIC_IDENTITY_TYPE);
    }

    private static String getHeader(HttpServletRequest request, String headerName) {
        String headerValue = request.getHeader(headerName);
        if (StringUtils.isNotBlank(headerValue)) {
            return headerValue;
        } else {
            return null;
        }
    }
}
