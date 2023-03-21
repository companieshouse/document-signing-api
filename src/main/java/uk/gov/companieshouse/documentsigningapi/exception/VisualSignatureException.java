package uk.gov.companieshouse.documentsigningapi.exception;


/**
 * Wraps and propagates exceptions originating in
 * {@link uk.gov.companieshouse.documentsigningapi.coversheet.VisualSignature}.
 */
public class VisualSignatureException extends RuntimeException {
    public VisualSignatureException(String message, Throwable cause) {
        super(message, cause);
    }
}
