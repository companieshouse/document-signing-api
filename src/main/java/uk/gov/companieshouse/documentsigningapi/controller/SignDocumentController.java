package uk.gov.companieshouse.documentsigningapi.controller;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static uk.gov.companieshouse.documentsigningapi.logging.LoggingUtils.SIGN_PDF_REQUEST;
import static uk.gov.companieshouse.documentsigningapi.logging.LoggingUtils.SIGN_PDF_RESPONSE;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.core.exception.SdkServiceException;
import uk.gov.companieshouse.documentsigningapi.aws.S3Service;
import uk.gov.companieshouse.documentsigningapi.dto.SignPdfRequestDTO;
import uk.gov.companieshouse.documentsigningapi.dto.SignPdfResponseDTO;
import uk.gov.companieshouse.documentsigningapi.exception.DocumentSigningException;
import uk.gov.companieshouse.documentsigningapi.logging.LoggingUtils;
import uk.gov.companieshouse.documentsigningapi.signing.SigningService;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Map;

@RestController
public class SignDocumentController {

    private static final String SIGN_PDF_ERROR_PREFIX = "signPdf: ";

    public static final String SIGN_PDF_URI =
            "${uk.gov.companieshouse.documentsigningapi.signpdf}";

    private final LoggingUtils logger;
    private final S3Service s3Service;
    private final SigningService signingService;

    public SignDocumentController(LoggingUtils logger, S3Service s3Service, SigningService signingService) {
        this.logger = logger;
        this.s3Service = s3Service;
        this.signingService = signingService;
    }

    @PostMapping(SIGN_PDF_URI)
    public ResponseEntity<Object> signPdf(final @RequestBody SignPdfRequestDTO signPdfRequestDTO) {

        final String unsignedDocumentLocation = signPdfRequestDTO.getDocumentLocation();
        final Map<String, Object> map = logger.createLogMap();
        map.put(SIGN_PDF_REQUEST, signPdfRequestDTO);
        try {

            byte[] unsignedDoc = s3Service.retrieveUnsignedDocument(unsignedDocumentLocation).readAllBytes();

            final byte[] signedPDF = signingService.signPDF(unsignedDoc);

            // Note this is just returning the location of the unsigned document for now.
            final SignPdfResponseDTO signPdfResponseDTO = new SignPdfResponseDTO();
            signPdfResponseDTO.setSignedDocumentLocation(unsignedDocumentLocation);
            map.put(SIGN_PDF_RESPONSE, signPdfResponseDTO);
            logger.getLogger().info("signPdf(" + signPdfRequestDTO + ") returning " + signPdfResponseDTO + ")", map);

            // Return the bytes as a response to enable viewing on signed pdf
            return ResponseEntity.status(CREATED).body(signedPDF);
//            TODO S3 changes not required for the signing yet, removed for ease of testing
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
        } catch (SdkException | DocumentSigningException | IOException e) {
            final ResponseEntity<Object> response = ResponseEntity.status(INTERNAL_SERVER_ERROR).body(e.getMessage());
            map.put(SIGN_PDF_RESPONSE, response);
            logger.getLogger().error(SIGN_PDF_ERROR_PREFIX + e.getMessage() , map);
            return response;
        }
    }

}
