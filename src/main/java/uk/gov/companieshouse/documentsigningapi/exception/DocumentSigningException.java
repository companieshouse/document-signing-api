package uk.gov.companieshouse.documentsigningapi.exception;

import java.security.GeneralSecurityException;

/**
 * DocumentSigningException is a wrapper exception that hides
 * lower level exceptions from the caller and prevents them
 * from being propagated up the call stack.
 */
public class DocumentSigningException extends GeneralSecurityException {

    /**
     * Constructs a new DocumentSigningException with a custom message.
     *
     * @param message a custom message
     */
    public DocumentSigningException(String message) {
        super(message);
    }

    /**
     * Constructs a new DocumentSigningException with a custom message and the specified
     * cause.
     *
     * @param message a custom message
     * @param cause the cause
     */
    public DocumentSigningException(String message, Throwable cause) {
        super(message, cause);
    }
}
