package uk.gov.companieshouse.documentsigningapi.validation;

import org.springframework.stereotype.Component;
import uk.gov.companieshouse.documentsigningapi.dto.SignPdfRequestDTO;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

@Component
public class RequestValidator {

    public List<String> validateRequest(SignPdfRequestDTO dto) {
        final List<String> errors = new ArrayList<>();

        if (!dto.getSignatureOptions().isEmpty() && dto.getSignatureOptions().contains(("cover-sheet"))) {

            if(dto.getCoverSheetData() == null) {
                errors.add("cover_sheet_data: must be present when signature_options contains cover-sheet value");
                return errors;
            }

           boolean allFieldsPresent = Stream.of(dto.getCoverSheetData().getCompanyName(),
                   dto.getCoverSheetData().getCompanyNumber(),
                   dto.getCoverSheetData().getFilingHistoryType(),
                   dto.getCoverSheetData().getFilingHistoryDescription()).allMatch(Objects::isNull);

            if (!allFieldsPresent) {
                errors.add("cover_sheet_data: there are missing fields in the coversheet data");
            }

        }
        return errors;
    }
}
