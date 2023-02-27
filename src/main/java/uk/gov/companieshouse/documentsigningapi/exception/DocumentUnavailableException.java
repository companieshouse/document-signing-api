package uk.gov.companieshouse.documentsigningapi.exception;

import java.io.IOException;

/**
 * DocumentUnavailableException is a wrapper exception that hides
 * lower level exceptions from the caller and prevents them
 * from being propagated up the call stack.
 */
public class DocumentUnavailableException extends IOException {

    /**
     * Constructs a new DocumentUnavailableException with a custom message.
     *
     * @param message a custom message
     */
    public DocumentUnavailableException(String message) {
        super(message);
    }

    /**
     * Constructs a new DocumentUnavailableException with a custom message and the specified
     * cause.
     *
     * @param message a custom message
     * @param cause the cause
     */
    public DocumentUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
