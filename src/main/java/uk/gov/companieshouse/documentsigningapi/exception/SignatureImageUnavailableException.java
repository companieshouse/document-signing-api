package uk.gov.companieshouse.documentsigningapi.exception;

public class SignatureImageUnavailableException extends RuntimeException {
    public SignatureImageUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
