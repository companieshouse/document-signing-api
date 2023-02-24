package uk.gov.companieshouse.documentsigningapi.signing;

import static uk.gov.companieshouse.documentsigningapi.logging.LoggingUtils.SIGN_PDF_REQUEST;

import org.apache.commons.io.FileUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.PDSignature;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.SignatureInterface;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;
import uk.gov.companieshouse.documentsigningapi.dto.SignPdfRequestDTO;
import uk.gov.companieshouse.documentsigningapi.logging.LoggingUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.Calendar;
import java.util.Map;

@Service
public class SigningService {

    private final String keystoreType;
    private final String keystorePath;
    private final String keystorePassword;
    private final String certificateAlias;
    private final LoggingUtils logger;

    public SigningService(@Value("${environment.keystore.type}") String keystoreType,
                          @Value("${environment.keystore.path}") String keystorePath,
                          @Value("${environment.keystore.password}") String keystorePassword,
                          @Value("${environment.certificate.alias}") String certificateAlias,
                          LoggingUtils logger) {
        this.keystoreType = keystoreType;
        this.keystorePath = keystorePath;
        this.keystorePassword = keystorePassword;
        this.certificateAlias = certificateAlias;
        this.logger = logger;
    }

    public byte[] signPDF(SignPdfRequestDTO signPdfRequestDTO) {
        final Map<String, Object> map = logger.createLogMap();
        map.put(SIGN_PDF_REQUEST, signPdfRequestDTO);

        try {
            KeyStore keyStore = getKeyStore();
            Signature signature = new Signature(keyStore, this.keystorePassword.toCharArray(), certificateAlias);

            // Get signature to be signed location
            Path pdfPath = Paths.get(signPdfRequestDTO.getDocumentLocation());
            // Turn pdf into byte array
            byte[] pdfToSign = Files.readAllBytes(pdfPath);

            // Create new PDF file
            File pdfFile = File.createTempFile("pdf", "");
            // Write the content to the new PDF
            FileUtils.writeByteArrayToFile(pdfFile, pdfToSign);

            // Create the file to be signed
            File signedPdf = File.createTempFile("signedPdf", "");

            // Sign the document
            this.signDetached(signature, pdfFile, signedPdf);

            byte[] signedPdfBytes = Files.readAllBytes(signedPdf.toPath());

            //remove temporary files
            pdfFile.deleteOnExit();
            signedPdf.deleteOnExit();

            return signedPdfBytes;


        } catch (NoSuchAlgorithmException | CertificateException | UnrecoverableKeyException | KeyStoreException e) {
            System.out.println("Cannot obtain proper KeyStore or Certificate");
        } catch (IOException e) {
            System.out.println("Cannot obtain proper file");
        }
    }

    private KeyStore getKeyStore() throws KeyStoreException, IOException, CertificateException, NoSuchAlgorithmException {
        KeyStore keyStore = KeyStore.getInstance(keystoreType);
        File key = ResourceUtils.getFile(keystorePath);
        keyStore.load(new FileInputStream(key), keystorePassword.toCharArray());
        return keyStore;
    }

    private void signDetached(SignatureInterface signature, File inputFile, File outputFile) throws IOException {
        if (inputFile == null || !inputFile.exists()) {
            throw new FileNotFoundException("Document for signing does not exist");
        }

        try (FileOutputStream fos = new FileOutputStream(outputFile);
             PDDocument doc = PDDocument.load(inputFile)) {
            signDetached(signature, doc, fos);
        }
    }

    private void signDetached(SignatureInterface signature, PDDocument document, OutputStream output) throws IOException {
        PDSignature pdSignature = new PDSignature();
        pdSignature.setFilter(PDSignature.FILTER_ADOBE_PPKLITE);
        pdSignature.setSubFilter(PDSignature.SUBFILTER_ADBE_PKCS7_DETACHED);
        pdSignature.setName("Cai Smith");
        pdSignature.setReason("Learning how to produce and sign PDF's");

        // the signing date, needed for valid signature
        pdSignature.setSignDate(Calendar.getInstance());

        // register signature dictionary and sign interface
        document.addSignature(pdSignature, signature);

        // write incremental (only for signing purpose)
        // use saveIncremental to add signature, using plain save method may break up a document
        document.saveIncremental(output);
    }

}
