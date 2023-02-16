package uk.gov.companieshouse.documentsigningapi.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.companieshouse.documentsigningapi.dto.SignPdfRequestDTO;
import uk.gov.companieshouse.documentsigningapi.dto.SignPdfResponseDTO;
import uk.gov.companieshouse.documentsigningapi.logging.LoggingUtils;

import java.util.Map;

import static org.springframework.http.HttpStatus.CREATED;
import static uk.gov.companieshouse.documentsigningapi.logging.LoggingUtils.SIGN_PDF_REQUEST;
import static uk.gov.companieshouse.documentsigningapi.logging.LoggingUtils.SIGN_PDF_RESPONSE;

@RestController
public class SignDocumentController {

    public static final String SIGN_PDF_URI =
            "${uk.gov.companieshouse.documentsigningapi.signpdf}";

    private final LoggingUtils logger;

    public SignDocumentController(LoggingUtils logger) {
        this.logger = logger;
    }

    @PostMapping(SIGN_PDF_URI)
    public ResponseEntity<Object> signPdf(final @RequestBody SignPdfRequestDTO signPdfRequestDTO) {

        final SignPdfResponseDTO signPdfResponseDTO = new SignPdfResponseDTO();
        signPdfResponseDTO.setSignedDocumentLocation("there");

        final Map<String, Object> map = logger.createLogMap();
        map.put(SIGN_PDF_REQUEST, signPdfRequestDTO);
        map.put(SIGN_PDF_RESPONSE, signPdfResponseDTO);
        logger.getLogger().debug("signPdf(" + signPdfRequestDTO + ") returning " + signPdfResponseDTO + ")", map);

        return ResponseEntity.status(CREATED).body(signPdfResponseDTO);
    }

}
