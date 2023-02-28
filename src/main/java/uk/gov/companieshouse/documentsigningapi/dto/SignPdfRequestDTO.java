package uk.gov.companieshouse.documentsigningapi.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class SignPdfRequestDTO {

    @JsonProperty("document_location")
    private String documentLocation;

    @JsonProperty("document_type")
    private String documentType;

    @JsonProperty("signature_options")
    private List<String> signatureOptions;

    public String getDocumentLocation() {
        return documentLocation;
    }

    public void setDocumentLocation(String documentLocation) {
        this.documentLocation = documentLocation;
    }

    public String getDocumentType() {
        return documentType;
    }

    public void setDocumentType(String documentType) {
        this.documentType = documentType;
    }

    public List<String> getSignatureOptions() {
        return signatureOptions;
    }

    public void setSignatureOptions(List<String> signatureOptions) {
        this.signatureOptions = signatureOptions;
    }

    @Override
    public String toString() {
        return "SignPdfRequestDTO{" +
                "documentLocation='" + documentLocation + '\'' +
                ", documentType='" + documentType + '\'' +
                ", signatureOptions=" + signatureOptions +
                '}';
    }
}
