package uk.gov.companieshouse.documentsigningapi.exception;

/**
 * Wraps and propagates exceptions originating in the
 * {@link uk.gov.companieshouse.documentsigningapi.coversheet.CoverSheetService}.
 */
public class CoverSheetException extends RuntimeException {
    public CoverSheetException(String message, Throwable cause) {
        super(message, cause);
    }
}
