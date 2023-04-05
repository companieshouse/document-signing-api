package uk.gov.companieshouse.documentsigningapi.exception;

public class ImageUnavailableException extends RuntimeException {
    public ImageUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
