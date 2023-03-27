package uk.gov.companieshouse.documentsigningapi.validation;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import uk.gov.companieshouse.documentsigningapi.dto.CoverSheetDataDTO;
import uk.gov.companieshouse.documentsigningapi.dto.SignPdfRequestDTO;

import java.util.Arrays;
import java.util.List;

class RequestValidatorTest {

    private static final String DOCUMENT_LOCATION =  "document/to/be/signed";
    private static final String DOCUMENT_TYPE =  "certified-copy";
    private static final List<String> SIGNATURE_OPTIONS = Arrays.asList("cover-sheet");
    private static final String PREFIX =  "test/certified-copy";
    private static final String KEY =  "testDoc.pdf";
    private static final String COMPANY_NAME = "test company";
    private static final String COMPANY_NUMBER = "00000000";
    private static final String FILING_HISTORY_TYPE = "ad01";
    private static final String FILING_HISTORY_DESCRIPTION = "this is an ad01 description";
    private static final CoverSheetDataDTO COVER_SHEET_DATA= new CoverSheetDataDTO(COMPANY_NAME, COMPANY_NUMBER,
        FILING_HISTORY_DESCRIPTION, FILING_HISTORY_TYPE);
    private static final CoverSheetDataDTO COVER_SHEET_DATA_MISSING_FIELD= new CoverSheetDataDTO(COMPANY_NAME, null,
        FILING_HISTORY_DESCRIPTION, FILING_HISTORY_TYPE);

    private static final String PREFIX_MISSING_VALIDATION_MESSAGE = "prefix: is a mandatory field and is not present";
    private static final String KEY_MISSING_VALIDATION_MESSAGE = "key: is a mandatory field and is not present";
    private static final String DOCUMENT_TYPE_MISSING_VALIDATION_MESSAGE = "document_type: is a mandatory field and is not present";
    private static final String DOCUMENT_LOCATION_MISSING_VALIDATION_MESSAGE = "document_location: is a mandatory field and is not present";
    private static final String COVER_SHEET_DATA_MISSING_VALIDATION_MESSAGE= "cover_sheet_data: must be present when signature_options contains 'cover-sheet' value";
    private static final String COVER_SHEET_DATA_FIELDS_MISSING_VALIDATION_MESSAGE = "cover_sheet_data: there are missing coversheet data fields, " +
        "please check that company_name, company_number, filing_history_type and filing_history_description are present in request";

    @Test
    @DisplayName("validate request returns no errors")
    void validateRequestReturnsNoErrors() {
        final SignPdfRequestDTO dto = new SignPdfRequestDTO(DOCUMENT_LOCATION, DOCUMENT_TYPE,
            SIGNATURE_OPTIONS, PREFIX, KEY, COVER_SHEET_DATA);

        RequestValidator requestValidator = new RequestValidator();
        List<String> errors = requestValidator.validateRequest(dto);

        Assertions.assertEquals(0, errors.size());
    }

    @Test
    @DisplayName("validate request returns prefix missing error")
    void validateRequestReturnsPrefixMissingError() {
        final SignPdfRequestDTO dto = new SignPdfRequestDTO(DOCUMENT_LOCATION, DOCUMENT_TYPE,
            SIGNATURE_OPTIONS, null, KEY, COVER_SHEET_DATA);

        RequestValidator requestValidator = new RequestValidator();
        List<String> errors = requestValidator.validateRequest(dto);

        Assertions.assertEquals(1, errors.size());
        Assertions.assertEquals(PREFIX_MISSING_VALIDATION_MESSAGE, errors.get(0));
    }

    @Test
    @DisplayName("validate request returns key missing error")
    void validateRequestReturnsKeyMissingError() {
        final SignPdfRequestDTO dto = new SignPdfRequestDTO(DOCUMENT_LOCATION, DOCUMENT_TYPE,
            SIGNATURE_OPTIONS, PREFIX, null, COVER_SHEET_DATA);

        RequestValidator requestValidator = new RequestValidator();
        List<String> errors = requestValidator.validateRequest(dto);

        Assertions.assertEquals(1, errors.size());
        Assertions.assertEquals(KEY_MISSING_VALIDATION_MESSAGE, errors.get(0));
    }

    @Test
    @DisplayName("validate request returns document type missing error")
    void validateRequestReturnsDocumentTypeMissingError() {
        final SignPdfRequestDTO dto = new SignPdfRequestDTO(DOCUMENT_LOCATION, null,
            SIGNATURE_OPTIONS, PREFIX, KEY, COVER_SHEET_DATA);

        RequestValidator requestValidator = new RequestValidator();
        List<String> errors = requestValidator.validateRequest(dto);

        Assertions.assertEquals(1, errors.size());
        Assertions.assertEquals(DOCUMENT_TYPE_MISSING_VALIDATION_MESSAGE, errors.get(0));
    }

    @Test
    @DisplayName("validate request returns document location missing error")
    void validateRequestReturnsDocumentLocationMissingError() {
        final SignPdfRequestDTO dto = new SignPdfRequestDTO(null, DOCUMENT_TYPE,
            SIGNATURE_OPTIONS, PREFIX, KEY, COVER_SHEET_DATA);

        RequestValidator requestValidator = new RequestValidator();
        List<String> errors = requestValidator.validateRequest(dto);

        Assertions.assertEquals(1, errors.size());
        Assertions.assertEquals(DOCUMENT_LOCATION_MISSING_VALIDATION_MESSAGE, errors.get(0));
    }

    @Test
    @DisplayName("validate request returns cover sheet data missing error")
    void validateRequestReturnsCoverSheetDataMissingError() {
        final SignPdfRequestDTO dto = new SignPdfRequestDTO(DOCUMENT_LOCATION, DOCUMENT_TYPE,
            SIGNATURE_OPTIONS, PREFIX, KEY, null);

        RequestValidator requestValidator = new RequestValidator();
        List<String> errors = requestValidator.validateRequest(dto);

        Assertions.assertEquals(1, errors.size());
        Assertions.assertEquals(COVER_SHEET_DATA_MISSING_VALIDATION_MESSAGE, errors.get(0));
    }

    @Test
    @DisplayName("validate request returns cover sheet data fields missing error")
    void validateRequestReturnsCoverSheetDataFieldsMissingError() {
        final SignPdfRequestDTO dto = new SignPdfRequestDTO(DOCUMENT_LOCATION, DOCUMENT_TYPE,
            SIGNATURE_OPTIONS, PREFIX, KEY, COVER_SHEET_DATA_MISSING_FIELD);

        RequestValidator requestValidator = new RequestValidator();
        List<String> errors = requestValidator.validateRequest(dto);

        Assertions.assertEquals(1, errors.size());
        Assertions.assertEquals(COVER_SHEET_DATA_FIELDS_MISSING_VALIDATION_MESSAGE, errors.get(0));
    }
}
