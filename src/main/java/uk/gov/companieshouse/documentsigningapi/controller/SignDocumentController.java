package uk.gov.companieshouse.documentsigningapi.controller;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.companieshouse.documentsigningapi.aws.S3Client;
import uk.gov.companieshouse.documentsigningapi.dto.SignPdfRequestDTO;
import uk.gov.companieshouse.documentsigningapi.dto.SignPdfResponseDTO;
import uk.gov.companieshouse.documentsigningapi.logging.LoggingUtils;

import java.net.URISyntaxException;
import java.util.Map;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CREATED;
import static uk.gov.companieshouse.documentsigningapi.logging.LoggingUtils.SIGN_PDF_REQUEST;
import static uk.gov.companieshouse.documentsigningapi.logging.LoggingUtils.SIGN_PDF_RESPONSE;

@RestController
public class SignDocumentController {

    public static final String SIGN_PDF_URI =
            "${uk.gov.companieshouse.documentsigningapi.signpdf}";

    private final LoggingUtils logger;
    private final S3Client s3Client;

    public SignDocumentController(LoggingUtils logger, S3Client s3Client) {
        this.logger = logger;
        this.s3Client = s3Client;
    }

    @PostMapping(SIGN_PDF_URI)
    public ResponseEntity<Object> signPdf(final @RequestBody SignPdfRequestDTO signPdfRequestDTO) {

        final String unsignedDocumentLocation = signPdfRequestDTO.getDocumentLocation();
        final Map<String, Object> map = logger.createLogMap();
        map.put(SIGN_PDF_REQUEST, signPdfRequestDTO);
        try {
            final S3ObjectInputStream s3UnsignedDocument = s3Client.retrieveUnsignedDocument(unsignedDocumentLocation);

            // Note this is just returning the location of the unsigned document as reported by S3 for now.
            final String s3ConfirmedUnsignedDocumentLocation = s3UnsignedDocument.getHttpRequest().getURI().toString();
            final SignPdfResponseDTO signPdfResponseDTO = new SignPdfResponseDTO();
            signPdfResponseDTO.setSignedDocumentLocation(s3ConfirmedUnsignedDocumentLocation);
            map.put(SIGN_PDF_RESPONSE, signPdfResponseDTO);
            logger.getLogger().info("signPdf(" + signPdfRequestDTO + ") returning " + signPdfResponseDTO + ")", map);
            return ResponseEntity.status(CREATED).body(signPdfResponseDTO);
        } catch (URISyntaxException use) {
            final ResponseEntity<Object> response = ResponseEntity.status(BAD_REQUEST).body(use.getMessage());
            map.put(SIGN_PDF_RESPONSE, response);
            logger.getLogger().error("signPdf: " + use.getMessage(), map);
            return response;
        } catch (AmazonServiceException ase) {
            final ResponseEntity<Object> response = ResponseEntity.status(ase.getStatusCode()).body(ase.getMessage());
            map.put(SIGN_PDF_RESPONSE, response);
            logger.getLogger().error("signPdf: " + ase.getMessage() , map);
            return response;
        }
    }

}
