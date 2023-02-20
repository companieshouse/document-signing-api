package uk.gov.companieshouse.documentsigningapi.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.core.exception.SdkServiceException;
import uk.gov.companieshouse.documentsigningapi.aws.S3Service;
import uk.gov.companieshouse.documentsigningapi.dto.SignPdfRequestDTO;
import uk.gov.companieshouse.documentsigningapi.dto.SignPdfResponseDTO;
import uk.gov.companieshouse.documentsigningapi.logging.LoggingUtils;

import java.net.URISyntaxException;
import java.util.Map;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static uk.gov.companieshouse.documentsigningapi.logging.LoggingUtils.SIGN_PDF_REQUEST;
import static uk.gov.companieshouse.documentsigningapi.logging.LoggingUtils.SIGN_PDF_RESPONSE;

@RestController
public class SignDocumentController {

    private static final String SIGN_PDF_ERROR_PREFIX = "signPdf: ";

    public static final String SIGN_PDF_URI =
            "${uk.gov.companieshouse.documentsigningapi.signpdf}";

    private final LoggingUtils logger;
    private final S3Service s3Service;

    public SignDocumentController(LoggingUtils logger, S3Service s3Service) {
        this.logger = logger;
        this.s3Service = s3Service;
    }

    @PostMapping(SIGN_PDF_URI)
    public ResponseEntity<Object> signPdf(final @RequestBody SignPdfRequestDTO signPdfRequestDTO) {

        final String unsignedDocumentLocation = signPdfRequestDTO.getDocumentLocation();
        final Map<String, Object> map = logger.createLogMap();
        map.put(SIGN_PDF_REQUEST, signPdfRequestDTO);
        try {

            s3Service.retrieveUnsignedDocument(unsignedDocumentLocation);

            // Note this is just returning the location of the unsigned document for now.
            final SignPdfResponseDTO signPdfResponseDTO = new SignPdfResponseDTO();
            signPdfResponseDTO.setSignedDocumentLocation(unsignedDocumentLocation);
            map.put(SIGN_PDF_RESPONSE, signPdfResponseDTO);
            logger.getLogger().info("signPdf(" + signPdfRequestDTO + ") returning " + signPdfResponseDTO + ")", map);
            return ResponseEntity.status(CREATED).body(signPdfResponseDTO);
        } catch (URISyntaxException use) {
            final ResponseEntity<Object> response = ResponseEntity.status(BAD_REQUEST).body(use.getMessage());
            map.put(SIGN_PDF_RESPONSE, response);
            logger.getLogger().error(SIGN_PDF_ERROR_PREFIX + use.getMessage(), map);
            return response;
        } catch (SdkServiceException sse) {
            final ResponseEntity<Object> response = ResponseEntity.status(sse.statusCode()).body(sse.getMessage());
            map.put(SIGN_PDF_RESPONSE, response);
            logger.getLogger().error(SIGN_PDF_ERROR_PREFIX + sse.getMessage() , map);
            return response;
        } catch (SdkException se) {
            final ResponseEntity<Object> response = ResponseEntity.status(INTERNAL_SERVER_ERROR).body(se.getMessage());
            map.put(SIGN_PDF_RESPONSE, response);
            logger.getLogger().error(SIGN_PDF_ERROR_PREFIX + se.getMessage() , map);
            return response;
        }
    }

}
