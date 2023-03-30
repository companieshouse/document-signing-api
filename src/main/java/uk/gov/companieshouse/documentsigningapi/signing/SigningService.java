package uk.gov.companieshouse.documentsigningapi.signing;

import org.apache.commons.io.FileUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.PDSignature;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.SignatureInterface;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.SignatureOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;
import uk.gov.companieshouse.documentsigningapi.coversheet.VisualSignature;
import uk.gov.companieshouse.documentsigningapi.exception.DocumentSigningException;
import uk.gov.companieshouse.documentsigningapi.exception.DocumentUnavailableException;
import uk.gov.companieshouse.documentsigningapi.exception.SigningException;
import uk.gov.companieshouse.documentsigningapi.exception.VisualSignatureException;
import uk.gov.companieshouse.documentsigningapi.logging.LoggingUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.Calendar;

@Service
public class SigningService {

    private static final String SIGNING_AUTHORITY_NAME = "Companies House";

    private final String keystoreType;
    private final String keystorePath;
    private final String keystorePassword;
    private final String certificateAlias;
    private final LoggingUtils logger;

    private final VisualSignature visualSignature;

    public SigningService(@Value("${environment.keystore.type}") String keystoreType,
                          @Value("${environment.keystore.path}") String keystorePath,
                          @Value("${environment.keystore.password}") String keystorePassword,
                          @Value("${environment.certificate.alias}") String certificateAlias,
                          LoggingUtils logger, VisualSignature visualSignature) {
        this.keystoreType = keystoreType;
        this.keystorePath = keystorePath;
        this.keystorePassword = keystorePassword;
        this.certificateAlias = certificateAlias;
        this.logger = logger;
        this.visualSignature = visualSignature;
    }

    @SuppressWarnings("squid:S1130") // exceptions are thrown actually
    public byte[] signPDF(byte[] pdfToSign, Calendar signingDate)
            throws DocumentSigningException, DocumentUnavailableException, VisualSignatureException, SigningException {
        try {
            var keyStore = getKeyStore();
            var signature = new Signature(keyStore, this.keystorePassword.toCharArray(), certificateAlias);

            // Create new PDF file
            var pdfFile = File.createTempFile("pdf", "");
            // Write the content to the new PDF
            FileUtils.writeByteArrayToFile(pdfFile, pdfToSign);

            // Create the file to be signed
            var signedPdf = File.createTempFile("signedPdf", "");

            // Sign the document
            this.signDetached(signature, pdfFile, signedPdf, signingDate);

            byte[] signedPdfBytes = Files.readAllBytes(signedPdf.toPath());

            //remove temporary files
            pdfFile.deleteOnExit();
            signedPdf.deleteOnExit();

            return signedPdfBytes;


        } catch (NoSuchAlgorithmException | CertificateException | UnrecoverableKeyException | KeyStoreException e) {
            logError(e);
            throw new DocumentSigningException("Failed to obtain proper KeyStore or Certificate", e);
        } catch (VisualSignatureException | SigningException se) {
            // Already logged, caught and thrown to prevent being handled as an IOException.
            throw se;
        } catch (IOException e) {
            logError(e);
            throw new DocumentUnavailableException("Unable to load Keystore or Certificate", e);
        }
    }

    private KeyStore getKeyStore() throws KeyStoreException, IOException, CertificateException, NoSuchAlgorithmException {
        var keyStore = KeyStore.getInstance(keystoreType);
        var key = ResourceUtils.getFile(keystorePath);
        keyStore.load(new FileInputStream(key), keystorePassword.toCharArray());
        return keyStore;
    }

    private void signDetached(SignatureInterface signature,
                              File inputFile,
                              File outputFile,
                              Calendar signingDate) throws IOException {
        if (inputFile == null || !inputFile.exists()) {
            throw new FileNotFoundException("Document for signing does not exist");
        }

        try (var fos = new FileOutputStream(outputFile);
             var doc = PDDocument.load(inputFile)) {
            signDetached(signature, doc, fos, signingDate);
        }
    }

    private void signDetached(SignatureInterface signature,
                              PDDocument document,
                              OutputStream output,
                              Calendar signingDate) throws IOException {
        var pdSignature = new PDSignature();
        pdSignature.setName(SIGNING_AUTHORITY_NAME);
        pdSignature.setFilter(PDSignature.FILTER_ADOBE_PPKLITE);
        pdSignature.setSubFilter(PDSignature.SUBFILTER_ADBE_PKCS7_DETACHED);

        // the signing date, needed for valid signature
        pdSignature.setSignDate(signingDate);

        try (final var signatureOptions = new SignatureOptions()) {

            visualSignature.renderSignatureLink(signatureOptions, document);

            // register signature dictionary, signature interface and options
            document.addSignature(pdSignature, signature, signatureOptions);

            // write incremental (only for signing purpose)
            // use saveIncremental to add signature, using plain save method may break up a document
            document.saveIncremental(output);
        } catch (SigningException se) {
            // Already logged, caught and thrown to prevent being handled as an IOException.
            throw se;
        } catch (IOException ioe) {
            logError(ioe);
            throw new VisualSignatureException("Failed to add visual signature to document", ioe);
        }

    }

    protected void logError(final Exception exception) {
        logger.getLogger().error(exception.getMessage(), exception);
    }

}
