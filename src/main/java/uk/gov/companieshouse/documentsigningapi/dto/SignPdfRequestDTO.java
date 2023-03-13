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

    @JsonProperty("folder_name")
    private String folderName;

    @JsonProperty("filename")
    private String filename;

    @JsonProperty("cover_sheet_data")
    private CoverSheetDataDTO coverSheetData;

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

    public String getFolderName() {
        return folderName;
    }

    public void setFolderName(String folderName) {
        this.folderName = folderName;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public CoverSheetDataDTO getCoverSheetData() {
        return coverSheetData;
    }

    public void setCoverSheetData(CoverSheetDataDTO coverSheetData) {
        this.coverSheetData = coverSheetData;
    }

    @Override
    public String toString() {
        return "SignPdfRequestDTO{" +
                "documentLocation='" + documentLocation + '\'' +
                ", documentType='" + documentType + '\'' +
                ", signatureOptions=" + signatureOptions +
                ", folderName='" + folderName + '\'' +
                ", filename='" + filename + '\'' +
                ", coverSheetData=" + coverSheetData +
                '}';
    }
}
