package uk.gov.companieshouse.documentsigningapi.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.HttpStatus.OK;

@RestController
public class SignDocumentController {

    public static final String SIGN_PDF_URI =
            "${uk.gov.companieshouse.documentsigningapi.signpdf}";

    @PostMapping(SIGN_PDF_URI)
    public ResponseEntity<Object> signPdf() {
        return ResponseEntity.status(OK).build();
    }

}
