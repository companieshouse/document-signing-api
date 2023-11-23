package uk.gov.companieshouse.documentsigningapi.validation;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import uk.gov.companieshouse.documentsigningapi.dto.CoverSheetDataDTO;
import uk.gov.companieshouse.documentsigningapi.dto.SignPdfRequestDTO;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

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

    private static final Map<String, String> FILING_HISTORY_DESCRIPTION_VALUES = Map.of("date", "2023-01-01",
            "director", "Test Director");

    private static final CoverSheetDataDTO COVER_SHEET_DATA= new CoverSheetDataDTO(COMPANY_NAME, COMPANY_NUMBER,
        FILING_HISTORY_DESCRIPTION, FILING_HISTORY_TYPE);
    private static final CoverSheetDataDTO COVER_SHEET_DATA_MISSING_FIELD= new CoverSheetDataDTO(COMPANY_NAME, COMPANY_NUMBER,
        FILING_HISTORY_DESCRIPTION, null);
    private static final CoverSheetDataDTO COVER_SHEET_DATA_MISSING_COMPANY_NAME= new CoverSheetDataDTO(null, COMPANY_NUMBER,
        FILING_HISTORY_DESCRIPTION, FILING_HISTORY_TYPE);
    private static final CoverSheetDataDTO COVER_SHEET_DATA_MISSING_COMPANY_NUMBER= new CoverSheetDataDTO(COMPANY_NAME, null,
        FILING_HISTORY_DESCRIPTION, FILING_HISTORY_TYPE);

    private static final String PREFIX_MISSING_VALIDATION_MESSAGE = "prefix: is a mandatory field and is not present";
    private static final String KEY_MISSING_VALIDATION_MESSAGE = "key: is a mandatory field and is not present";
    private static final String DOCUMENT_TYPE_MISSING_VALIDATION_MESSAGE = "document_type: is a mandatory field and is not present";
    private static final String DOCUMENT_LOCATION_MISSING_VALIDATION_MESSAGE = "document_location: is a mandatory field and is not present";
    private static final String COVER_SHEET_DATA_MISSING_VALIDATION_MESSAGE= "cover_sheet_data: must be present when signature_options contains 'cover-sheet' value";
    private static final String COVER_SHEET_DATA_FIELDS_MISSING_VALIDATION_MESSAGE = "cover_sheet_data: there are missing coversheet data fields, " +
        "please check that filing_history_type and filing_history_description are present in request";
    private static final String COMPANY_NAME_MISSING_MESSAGE = "cover_sheet_data: missing company_name";
    private static final String COMPANY_NUMBER_MISSING_MESSAGE = "cover_sheet_data: missing company_number";

    @Test
    @DisplayName("validate request returns no errors")
    void validateRequestReturnsNoErrors() {
        final SignPdfRequestDTO dto = new SignPdfRequestDTO(DOCUMENT_LOCATION, DOCUMENT_TYPE,
            SIGNATURE_OPTIONS, PREFIX, KEY, COVER_SHEET_DATA, FILING_HISTORY_DESCRIPTION_VALUES);

        RequestValidator requestValidator = new RequestValidator();
        List<String> errors = requestValidator.validateRequest(dto);

        Assertions.assertEquals(0, errors.size());
    }

    @Test
    @DisplayName("validate request returns no errors without optional fields")
    void validateRequestReturnsNoErrorsWithoutOptionals() {
        final SignPdfRequestDTO dto = new SignPdfRequestDTO(DOCUMENT_LOCATION, DOCUMENT_TYPE,
            null, PREFIX, KEY, null, FILING_HISTORY_DESCRIPTION_VALUES);

        RequestValidator requestValidator = new RequestValidator();
        List<String> errors = requestValidator.validateRequest(dto);

        Assertions.assertEquals(0, errors.size());
    }

    @Test
    @DisplayName("validate request returns prefix missing error")
    void validateRequestReturnsPrefixMissingError() {
        final SignPdfRequestDTO dto = new SignPdfRequestDTO(DOCUMENT_LOCATION, DOCUMENT_TYPE,
            SIGNATURE_OPTIONS, null, KEY, COVER_SHEET_DATA, FILING_HISTORY_DESCRIPTION_VALUES);

        RequestValidator requestValidator = new RequestValidator();
        List<String> errors = requestValidator.validateRequest(dto);

        Assertions.assertEquals(1, errors.size());
        Assertions.assertEquals(PREFIX_MISSING_VALIDATION_MESSAGE, errors.get(0));
    }

    @Test
    @DisplayName("validate request returns key missing error")
    void validateRequestReturnsKeyMissingError() {
        final SignPdfRequestDTO dto = new SignPdfRequestDTO(DOCUMENT_LOCATION, DOCUMENT_TYPE,
            SIGNATURE_OPTIONS, PREFIX, null, COVER_SHEET_DATA, FILING_HISTORY_DESCRIPTION_VALUES);

        RequestValidator requestValidator = new RequestValidator();
        List<String> errors = requestValidator.validateRequest(dto);

        Assertions.assertEquals(1, errors.size());
        Assertions.assertEquals(KEY_MISSING_VALIDATION_MESSAGE, errors.get(0));
    }

    @Test
    @DisplayName("validate request returns document type missing error")
    void validateRequestReturnsDocumentTypeMissingError() {
        final SignPdfRequestDTO dto = new SignPdfRequestDTO(DOCUMENT_LOCATION, null,
            SIGNATURE_OPTIONS, PREFIX, KEY, COVER_SHEET_DATA, FILING_HISTORY_DESCRIPTION_VALUES);

        RequestValidator requestValidator = new RequestValidator();
        List<String> errors = requestValidator.validateRequest(dto);

        Assertions.assertEquals(1, errors.size());
        Assertions.assertEquals(DOCUMENT_TYPE_MISSING_VALIDATION_MESSAGE, errors.get(0));
    }

    @Test
    @DisplayName("validate request returns document location missing error")
    void validateRequestReturnsDocumentLocationMissingError() {
        final SignPdfRequestDTO dto = new SignPdfRequestDTO(null, DOCUMENT_TYPE,
            SIGNATURE_OPTIONS, PREFIX, KEY, COVER_SHEET_DATA, FILING_HISTORY_DESCRIPTION_VALUES);

        RequestValidator requestValidator = new RequestValidator();
        List<String> errors = requestValidator.validateRequest(dto);

        Assertions.assertEquals(1, errors.size());
        Assertions.assertEquals(DOCUMENT_LOCATION_MISSING_VALIDATION_MESSAGE, errors.get(0));
    }

    @Test
    @DisplayName("validate request returns cover sheet data missing error")
    void validateRequestReturnsCoverSheetDataMissingError() {
        final SignPdfRequestDTO dto = new SignPdfRequestDTO(DOCUMENT_LOCATION, DOCUMENT_TYPE,
            SIGNATURE_OPTIONS, PREFIX, KEY, null, FILING_HISTORY_DESCRIPTION_VALUES);

        RequestValidator requestValidator = new RequestValidator();
        List<String> errors = requestValidator.validateRequest(dto);

        Assertions.assertEquals(1, errors.size());
        Assertions.assertEquals(COVER_SHEET_DATA_MISSING_VALIDATION_MESSAGE, errors.get(0));
    }

    @Test
    @DisplayName("validate request returns cover sheet filing_history_type and/or filing_history_description missing error")
    void validateRequestReturnsCoverSheetDataFieldsMissingError() {
        final SignPdfRequestDTO dto = new SignPdfRequestDTO(DOCUMENT_LOCATION, DOCUMENT_TYPE,
            SIGNATURE_OPTIONS, PREFIX, KEY, COVER_SHEET_DATA_MISSING_FIELD, FILING_HISTORY_DESCRIPTION_VALUES);

        RequestValidator requestValidator = new RequestValidator();
        List<String> errors = requestValidator.validateRequest(dto);

        Assertions.assertEquals(1, errors.size());
        Assertions.assertEquals(COVER_SHEET_DATA_FIELDS_MISSING_VALIDATION_MESSAGE, errors.get(0));
    }

    @Test
    @DisplayName("validate request returns cover sheet company NAME missing error")
    void validateRequestReturnsCompanyNameMissingError() {
        final SignPdfRequestDTO dto = new SignPdfRequestDTO(
            DOCUMENT_LOCATION,
            DOCUMENT_TYPE,
            SIGNATURE_OPTIONS,
            PREFIX,
            KEY,
            COVER_SHEET_DATA_MISSING_COMPANY_NAME,
                FILING_HISTORY_DESCRIPTION_VALUES);

        RequestValidator requestValidator = new RequestValidator();
        List<String> errors = requestValidator.validateRequest(dto);

        Assertions.assertEquals(1, errors.size());
        Assertions.assertEquals(COMPANY_NAME_MISSING_MESSAGE, errors.get(0));
    }

    @Test
    @DisplayName("validate request returns cover sheet company NUMBER missing error")
    void validateRequestReturnsCompanyNumberMissingError() {
        final SignPdfRequestDTO dto = new SignPdfRequestDTO(
            DOCUMENT_LOCATION,
            DOCUMENT_TYPE,
            SIGNATURE_OPTIONS,
            PREFIX,
            KEY,
            COVER_SHEET_DATA_MISSING_COMPANY_NUMBER,
                FILING_HISTORY_DESCRIPTION_VALUES);

        RequestValidator requestValidator = new RequestValidator();
        List<String> errors = requestValidator.validateRequest(dto);

        Assertions.assertEquals(1, errors.size());
        Assertions.assertEquals(COMPANY_NUMBER_MISSING_MESSAGE, errors.get(0));
    }
}
