package uk.gov.companieshouse.documentsigningapi.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.companieshouse.documentsigningapi.dto.SignPdfRequestDTO;
import uk.gov.companieshouse.documentsigningapi.dto.SignPdfResponseDTO;

import static org.springframework.http.HttpStatus.CREATED;

@RestController
public class SignDocumentController {

    public static final String SIGN_PDF_URI =
            "${uk.gov.companieshouse.documentsigningapi.signpdf}";

    @PostMapping(SIGN_PDF_URI)
    public ResponseEntity<Object> signPdf(final @RequestBody SignPdfRequestDTO signPdfRequestDTO) {

        final SignPdfResponseDTO response = new SignPdfResponseDTO();
        response.setSignedDocumentLocation("there");

        // TODO DCAC-76 Use structured logging
        System.out.println("signPdf(" + signPdfRequestDTO + ") returning " + response + ")");

        return ResponseEntity.status(CREATED).body(response);
    }

}
