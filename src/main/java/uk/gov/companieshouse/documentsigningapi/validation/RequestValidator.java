package uk.gov.companieshouse.documentsigningapi.validation;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.documentsigningapi.dto.SignPdfRequestDTO;
import uk.gov.companieshouse.logging.util.DataMap;

import java.util.ArrayList;
import java.util.List;

@Component
public class RequestValidator {
    private static final String PREFIX_MISSING_VALIDATION_MESSAGE = "prefix: is a mandatory field and is not present";
    private static final String KEY_MISSING_VALIDATION_MESSAGE = "key: is a mandatory field and is not present";
    private static final String DOCUMENT_TYPE_MISSING_VALIDATION_MESSAGE = "document_type: is a mandatory field and is not present";
    private static final String DOCUMENT_LOCATION_MISSING_VALIDATION_MESSAGE = "document_location: is a mandatory field and is not present";
    private static final String COVER_SHEET_DATA_MISSING_VALIDATION_MESSAGE= "cover_sheet_data: must be present when signature_options contains 'cover-sheet' value";
    private static final String COVER_SHEET_DATA_FIELDS_MISSING_VALIDATION_MESSAGE = "cover_sheet_data: there are missing coversheet data fields, " +
        "please check that filing_history_type and filing_history_description are present in request";
    private static final String COMPANY_NAME_KEY = "company_name_includes";
    private static final String COMPANY_NUMBER_KEY = "company_number";
    private static final String COMPANY_NAME_MISSING_MESSAGE = "cover_sheet_data: missing company_name";
    private static final String COMPANY_NUMBER_MISSING_MESSAGE = "cover_sheet_data: missing company_number";

    public List<String> validateRequest(SignPdfRequestDTO dto) {
        final List<String> errors = new ArrayList<>();
        
        validateMandatoryFields(dto, errors);
        validateCoverSheet(dto, errors);
        
        return errors;
    }

    private void validateMandatoryFields(SignPdfRequestDTO dto, List<String> errors) {
        if (StringUtils.isBlank(dto.getPrefix())) errors.add(PREFIX_MISSING_VALIDATION_MESSAGE);
        if (StringUtils.isBlank(dto.getKey())) errors.add(KEY_MISSING_VALIDATION_MESSAGE);
        if (StringUtils.isBlank(dto.getDocumentType())) errors.add(DOCUMENT_TYPE_MISSING_VALIDATION_MESSAGE);
        if (StringUtils.isBlank(dto.getDocumentLocation())) errors.add(DOCUMENT_LOCATION_MISSING_VALIDATION_MESSAGE);
    }

    private void validateCoverSheet(SignPdfRequestDTO dto, List<String> errors) {
        if (dto.getSignatureOptions() != null && !dto.getSignatureOptions().isEmpty() && dto.getSignatureOptions().contains(("cover-sheet"))) {

            if (dto.getCoverSheetData() == null) {
                errors.add(COVER_SHEET_DATA_MISSING_VALIDATION_MESSAGE);
                return;
            }

            validateCompanyName(dto, errors);
            validateCompanyNumber(dto, errors);

            if (StringUtils.isBlank(dto.getCoverSheetData().getFilingHistoryType()) ||
                StringUtils.isBlank(dto.getCoverSheetData().getFilingHistoryDescription())) {
                errors.add(COVER_SHEET_DATA_FIELDS_MISSING_VALIDATION_MESSAGE);
            }
        }
    }

    /**
     * Company NAME missing check.
     * @param dto to validate.
     * @param errors to append to.
     */
    private void validateCompanyName(SignPdfRequestDTO dto, List<String> errors) {
        // Use COMPANY_NAME_KEY for the single KV pair we are interested in.
        if(StringUtils.isBlank(dto.getCoverSheetData().getCompanyName())) {
            DataMap dataMap = new DataMap.Builder()
                    .companyName(COMPANY_NAME_MISSING_MESSAGE)
                .build();
            errors.add((String) dataMap.getLogMap().get(COMPANY_NAME_KEY));
        }
    }

    /**
     * Company NUMBER missing check.
     * @param dto to validate.
     * @param errors to append to.
     */
    private void validateCompanyNumber(SignPdfRequestDTO dto, List<String> errors) {
        // Company name required for builder, which can be ignored, so use COMPANY_NUMBER_KEY for the single KV pair we are interested in.
        if(StringUtils.isBlank(dto.getCoverSheetData().getCompanyNumber())) {
            DataMap dataMap = new DataMap.Builder()
                    .companyName(dto.getCoverSheetData().getCompanyNumber())
                .companyNumber(COMPANY_NUMBER_MISSING_MESSAGE)
                .build();
            errors.add((String) dataMap.getLogMap().get(COMPANY_NUMBER_KEY));
        }
    }
}
