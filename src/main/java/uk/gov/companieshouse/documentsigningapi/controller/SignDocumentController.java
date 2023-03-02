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
import uk.gov.companieshouse.documentsigningapi.exception.DocumentSigningException;
import uk.gov.companieshouse.documentsigningapi.exception.DocumentUnavailableException;
import uk.gov.companieshouse.documentsigningapi.logging.LoggingUtils;
import uk.gov.companieshouse.documentsigningapi.signing.SigningService;

import java.net.URISyntaxException;

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
    private final SigningService signingService;

    public SignDocumentController(LoggingUtils logger, S3Service s3Service, SigningService signingService) {
        this.logger = logger;
        this.s3Service = s3Service;
        this.signingService = signingService;
    }

    /**
     * Retrieves an unsigned PDF document from the S3 bucket location specified, signs it and stores it.
     * Stores the signed copy of the PDF document in the configured signed document S3 bucket, under a key (file path)
     * derived both from configuration and from information in the request body.
     * @param signPdfRequestDTO {@link SignPdfRequestDTO} specifying the document to be signed and information
     *                          used to derive the storage location of the signed document
     * @return {@link ResponseEntity} of {@link Object} containing a status code and the location of the signed PDF
     * document when successful
     */
    @PostMapping(SIGN_PDF_URI)
    public ResponseEntity<Object> signPdf(final @RequestBody SignPdfRequestDTO signPdfRequestDTO) {

        final var unsignedDocumentLocation = signPdfRequestDTO.getDocumentLocation();
        final var folderName = signPdfRequestDTO.getFolderName();
        final var filename = signPdfRequestDTO.getFilename();
        final var map = logger.createLogMap();
        map.put(SIGN_PDF_REQUEST, signPdfRequestDTO);
        try {
            final var unsignedDoc = s3Service.retrieveUnsignedDocument(unsignedDocumentLocation);
            final var signedPDF = signingService.signPDF(unsignedDoc);
            final var signedDocumentLocation = s3Service.storeSignedDocument(signedPDF, folderName, filename);

            final var signPdfResponseDTO = new SignPdfResponseDTO();
            signPdfResponseDTO.setSignedDocumentLocation(signedDocumentLocation);
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
        } catch (SdkException | DocumentSigningException | DocumentUnavailableException e) {
            final ResponseEntity<Object> response = ResponseEntity.status(INTERNAL_SERVER_ERROR).body(e.getMessage());
            map.put(SIGN_PDF_RESPONSE, response);
            logger.getLogger().error(SIGN_PDF_ERROR_PREFIX + e.getMessage() , map);
            return response;
        }
    }

}
