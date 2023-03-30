package uk.gov.companieshouse.documentsigningapi.exception;

import java.io.IOException;

/**
 * Wraps and propagates exceptions originating in
 * {@link uk.gov.companieshouse.documentsigningapi.signing.Signature}.
 */
public class SigningException  extends IOException {
    public SigningException(String message, Throwable cause) {
        super(message, cause);
    }
}
