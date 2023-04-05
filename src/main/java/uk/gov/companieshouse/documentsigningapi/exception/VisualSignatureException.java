package uk.gov.companieshouse.documentsigningapi.exception;


import java.io.IOException;

/**
 * Wraps and propagates exceptions originating in
 * {@link uk.gov.companieshouse.documentsigningapi.coversheet.VisualSignature}.
 */
public class VisualSignatureException extends IOException {
    public VisualSignatureException(String message, Throwable cause) {
        super(message, cause);
    }
}
