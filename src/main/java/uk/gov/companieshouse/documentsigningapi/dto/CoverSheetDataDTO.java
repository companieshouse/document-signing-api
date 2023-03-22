package uk.gov.companieshouse.documentsigningapi.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CoverSheetDataDTO {

    @JsonProperty("company_name")
    private String companyName;

    @JsonProperty("company_number")
    private String companyNumber;

    @JsonProperty("filing_history_description")
    private String filingHistoryDescription;

    @JsonProperty("filing_history_type")
    private String filingHistoryType;

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getCompanyNumber() {
        return companyNumber;
    }

    public void setCompanyNumber(String companyNumber) {
        this.companyNumber = companyNumber;
    }

    public String getFilingHistoryDescription() {
        return filingHistoryDescription;
    }

    public void setFilingHistoryDescription(String filingHistoryDescription) {
        this.filingHistoryDescription = filingHistoryDescription;
    }

    public String getFilingHistoryType() {
        return filingHistoryType;
    }

    public void setFilingHistoryType(String filingHistoryType) {
        this.filingHistoryType = filingHistoryType;
    }

    @Override
    public String toString() {
        return "CoverSheetDataDTO{" +
                "companyName='" + companyName + '\'' +
                ", companyNumber='" + companyNumber + '\'' +
                ", filingHistoryDescription='" + filingHistoryDescription + '\'' +
                ", filingHistoryType='" + filingHistoryType + '\'' +
                '}';
    }
}
