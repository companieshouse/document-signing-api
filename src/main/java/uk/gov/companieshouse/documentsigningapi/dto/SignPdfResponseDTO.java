package uk.gov.companieshouse.documentsigningapi.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SignPdfResponseDTO {

    @JsonProperty("signed_document_location")
    private String signedDocumentLocation;

    public String getSignedDocumentLocation() {
        return signedDocumentLocation;
    }

    public void setSignedDocumentLocation(String signedDocumentLocation) {
        this.signedDocumentLocation = signedDocumentLocation;
    }

    @Override
    public String toString() {
        return "SignPdfResponseDTO{" +
                "signedDocumentLocation='" + signedDocumentLocation + '\'' +
                '}';
    }
}
