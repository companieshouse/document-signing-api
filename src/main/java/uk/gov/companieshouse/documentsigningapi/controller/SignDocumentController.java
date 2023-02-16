package uk.gov.companieshouse.documentsigningapi.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.companieshouse.documentsigningapi.dto.SignPdfRequestDTO;

import static org.springframework.http.HttpStatus.OK;

@RestController
public class SignDocumentController {

    public static final String SIGN_PDF_URI =
            "${uk.gov.companieshouse.documentsigningapi.signpdf}";

    @PostMapping(SIGN_PDF_URI)
    public ResponseEntity<Object> signPdf(final @RequestBody SignPdfRequestDTO signPdfRequestDTO) {

        // TODO DCAC-76 Use structured logging
        System.out.println("signPdf(" + signPdfRequestDTO + ")");

        return ResponseEntity.status(OK).build();
    }

}
