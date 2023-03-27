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
import uk.gov.companieshouse.documentsigningapi.coversheet.CoverSheetService;
import uk.gov.companieshouse.documentsigningapi.dto.SignPdfRequestDTO;
import uk.gov.companieshouse.documentsigningapi.dto.SignPdfResponseDTO;
import uk.gov.companieshouse.documentsigningapi.exception.CoverSheetException;
import uk.gov.companieshouse.documentsigningapi.exception.DocumentSigningException;
import uk.gov.companieshouse.documentsigningapi.logging.LoggingUtils;
import uk.gov.companieshouse.documentsigningapi.signing.SigningService;
import uk.gov.companieshouse.documentsigningapi.validation.RequestValidator;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;

@RestController
public class SignDocumentController {

    private static final String SIGN_PDF_ERROR_PREFIX = "signPdf: ";

    private static final String COVER_SHEET_SIGNATURE_OPTION = "cover-sheet";

    public static final String SIGN_PDF_URI =
            "${uk.gov.companieshouse.documentsigningapi.signpdf}";

    private final LoggingUtils logger;
    private final S3Service s3Service;
    private final SigningService signingService;
    private final CoverSheetService coverSheetService;
    private final RequestValidator requestValidator;

    public SignDocumentController(LoggingUtils logger,
                                  S3Service s3Service,
                                  SigningService signingService,
                                  CoverSheetService coverSheetService,
                                  RequestValidator requestValidator) {
        this.logger = logger;
        this.s3Service = s3Service;
        this.signingService = signingService;
        this.coverSheetService = coverSheetService;
        this.requestValidator = requestValidator;
    }

    /**
     * Retrieves an unsigned PDF document from the S3 bucket location specified, signs it and stores it.
     * Adds a cover sheet to the signed PDF document if required.
     * Stores the signed copy of the PDF document in the configured signed document S3 bucket, under a key (file path)
     * derived from information in the request body.
     * @param signPdfRequestDTO {@link SignPdfRequestDTO} specifying the document to be signed and information
     *                          used to derive the storage location of the signed document
     * @return {@link ResponseEntity} of {@link Object} containing a status code and the location of the signed PDF
     * document when successful
     */
    @PostMapping(SIGN_PDF_URI)
    public ResponseEntity<Object> signPdf(final @RequestBody SignPdfRequestDTO signPdfRequestDTO) {

        final var unsignedDocumentLocation = signPdfRequestDTO.getDocumentLocation();
        final var prefix = signPdfRequestDTO.getPrefix();
        final var key = signPdfRequestDTO.getKey();
        final var map = logger.createLogMap();
        map.put(SIGN_PDF_REQUEST, signPdfRequestDTO);

        List<String> errors = requestValidator.validateRequest(signPdfRequestDTO);
        if (!requestValidator.validateRequest(signPdfRequestDTO).isEmpty()) {
            return buildValidationResponse(BAD_REQUEST.value(), errors, map);
        }

        try {
            final var unsignedDoc = s3Service.retrieveUnsignedDocument(unsignedDocumentLocation);
            final var coveredDoc = addCoverSheetIfRequired(unsignedDoc.readAllBytes(), signPdfRequestDTO);
            final var signedPDF = signingService.signPDF(coveredDoc);
            final var signedDocumentLocation =
                    s3Service.storeSignedDocument(signedPDF, prefix, key);
            return buildResponse(signedDocumentLocation, signPdfRequestDTO, map);
        } catch (URISyntaxException use) {
            return buildErrorResponse(BAD_REQUEST.value(), use, map);
        } catch (SdkServiceException sse) {
            return buildErrorResponse(sse.statusCode(), sse, map);
        } catch (SdkException | DocumentSigningException | IOException | CoverSheetException e) {
            return buildErrorResponse(INTERNAL_SERVER_ERROR.value(), e, map);
        }
    }

    private byte[] addCoverSheetIfRequired(final byte[] document,
                                           final SignPdfRequestDTO request) {
        return request.getSignatureOptions() != null &&
                request.getSignatureOptions().contains(COVER_SHEET_SIGNATURE_OPTION) ?
                coverSheetService.addCoverSheet(document, request.getCoverSheetData()) : document;
    }

    private ResponseEntity<Object> buildResponse(final String signedDocumentLocation,
                                                 final SignPdfRequestDTO request,
                                                 final Map<String, Object> map) {
        final var signPdfResponseDTO = new SignPdfResponseDTO();
        signPdfResponseDTO.setSignedDocumentLocation(signedDocumentLocation);
        map.put(SIGN_PDF_RESPONSE, signPdfResponseDTO);
        logger.getLogger().info("signPdf(" + request + ") returning " + signPdfResponseDTO + ")", map);
        return ResponseEntity.status(CREATED).body(signPdfResponseDTO);
    }

    private ResponseEntity<Object> buildValidationResponse(final int statusCode,
                                                           final List<String> errors,
                                                           final Map<String, Object> map){
        final ResponseEntity<Object> response = ResponseEntity.status(statusCode).body(errors);
        map.put(SIGN_PDF_RESPONSE, response);
        logger.getLogger().error(SIGN_PDF_ERROR_PREFIX + errors, map);
        return response;
    }

    private ResponseEntity<Object> buildErrorResponse(final int statusCode,
                                                      final Exception ex,
                                                      final Map<String, Object> map){
        final ResponseEntity<Object> response = ResponseEntity.status(statusCode).body(ex.getMessage());
        map.put(SIGN_PDF_RESPONSE, response);
        logger.getLogger().error(SIGN_PDF_ERROR_PREFIX + ex.getMessage(), map);
        return response;
    }
}
