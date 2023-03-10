package uk.gov.companieshouse.documentsigningapi.signing;

import org.apache.commons.io.FileUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.common.PDStream;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.form.PDFormXObject;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationTextMarkup;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationWidget;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAppearanceDictionary;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAppearanceStream;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.PDSignature;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.SignatureInterface;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.SignatureOptions;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDField;
import org.apache.pdfbox.pdmodel.interactive.form.PDSignatureField;
import org.apache.pdfbox.util.Matrix;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;
import uk.gov.companieshouse.documentsigningapi.exception.DocumentSigningException;
import uk.gov.companieshouse.documentsigningapi.exception.DocumentUnavailableException;
import uk.gov.companieshouse.documentsigningapi.exception.ImageUnavailableException;
import uk.gov.companieshouse.documentsigningapi.logging.LoggingUtils;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.Calendar;
import java.util.List;

@Service
public class SigningService {

    private static final String SIGNING_AUTHORITY_NAME = "Companies House";

    private static final String DIGITAL_SEARCH_COPY_STAMP_PATH = "app/digital-search-copy-stamp.png";

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

    public byte[] signPDF(byte[] pdfToSign) throws DocumentSigningException, DocumentUnavailableException {
        try {
            KeyStore keyStore = getKeyStore();
            Signature signature = new Signature(keyStore, this.keystorePassword.toCharArray(), certificateAlias);

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
            throw new DocumentSigningException("Failed to obtain proper KeyStore or Certificate", e);
        } catch (IOException e) {
            throw new DocumentUnavailableException("Unable to load Keystore or Certificate", e);
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
        pdSignature.setName(SIGNING_AUTHORITY_NAME);
        pdSignature.setFilter(PDSignature.FILTER_ADOBE_PPKLITE);
        pdSignature.setSubFilter(PDSignature.SUBFILTER_ADBE_PKCS7_DETACHED);

        // the signing date, needed for valid signature
        pdSignature.setSignDate(Calendar.getInstance());

        // Set the signature rectangle
        // Although PDF coordinates start from the bottom, humans start from the top.
        // So a human would want to position a signature (x,y) units from the
        // top left of the displayed page, and the field has a horizontal width and a vertical height
        // regardless of page rotation.
        final Rectangle2D humanRect = new Rectangle2D.Float(50, 600, 500, 150);
        final PDRectangle rect = createSignatureRectangle(document, humanRect);
        final SignatureOptions signatureOptions = new SignatureOptions();
        final File imageFile = ResourceUtils.getFile(DIGITAL_SEARCH_COPY_STAMP_PATH);
        signatureOptions.setVisualSignature(
                createVisualSignatureTemplate(document, 0, rect, pdSignature, (Signature) signature, imageFile));
        signatureOptions.setPage(0);

        // register signature dictionary and sign interface
        document.addSignature(pdSignature, signature, signatureOptions);

        // write incremental (only for signing purpose)
        // use saveIncremental to add signature, using plain save method may break up a document
        document.saveIncremental(output);
    }

    private PDRectangle createSignatureRectangle(PDDocument doc, Rectangle2D humanRect)
    {
        float x = (float) humanRect.getX();
        float y = (float) humanRect.getY();
        float width = (float) humanRect.getWidth();
        float height = (float) humanRect.getHeight();
        PDPage page = doc.getPage(0);
        PDRectangle pageRect = page.getCropBox();
        PDRectangle rect = new PDRectangle();
        // signing should be at the same position regardless of page rotation.
        switch (page.getRotation())
        {
            case 90:
                rect.setLowerLeftY(x);
                rect.setUpperRightY(x + width);
                rect.setLowerLeftX(y);
                rect.setUpperRightX(y + height);
                break;
            case 180:
                rect.setUpperRightX(pageRect.getWidth() - x);
                rect.setLowerLeftX(pageRect.getWidth() - x - width);
                rect.setLowerLeftY(y);
                rect.setUpperRightY(y + height);
                break;
            case 270:
                rect.setLowerLeftY(pageRect.getHeight() - x - width);
                rect.setUpperRightY(pageRect.getHeight() - x);
                rect.setLowerLeftX(pageRect.getWidth() - y - height);
                rect.setUpperRightX(pageRect.getWidth() - y);
                break;
            case 0:
            default:
                rect.setLowerLeftX(x);
                rect.setUpperRightX(x + width);
                rect.setLowerLeftY(pageRect.getHeight() - y - height);
                rect.setUpperRightY(pageRect.getHeight() - y);
                break;
        }
        return rect;
    }

    // create a template PDF document with empty signature and return it as a stream.
    private InputStream createVisualSignatureTemplate(PDDocument srcDoc,
                                                      int pageNum,
                                                      PDRectangle rect,
                                                      PDSignature pdSignature,
                                                      Signature signature,
                                                      File imageFile) throws IOException
    {
        PDDocument doc = new PDDocument();

        PDPage page = new PDPage(srcDoc.getPage(pageNum).getMediaBox());
        doc.addPage(page);
        PDAcroForm acroForm = new PDAcroForm(doc);
        doc.getDocumentCatalog().setAcroForm(acroForm);
        PDSignatureField signatureField = new PDSignatureField(acroForm);
        PDAnnotationWidget widget = signatureField.getWidgets().get(0);
        List<PDField> acroFormFields = acroForm.getFields();
        acroForm.setSignaturesExist(true);
        acroForm.setAppendOnly(true);
        acroForm.getCOSObject().setDirect(true);
        acroFormFields.add(signatureField);

        widget.setRectangle(rect);

        // from PDVisualSigBuilder.createHolderForm()
        PDStream stream = new PDStream(doc);
        PDFormXObject form = new PDFormXObject(stream);
        PDResources res = new PDResources();
        form.setResources(res);
        form.setFormType(1);
        PDRectangle bbox = new PDRectangle(rect.getWidth(), rect.getHeight());
        float height = bbox.getHeight();
        Matrix initialScale = null;
        switch (srcDoc.getPage(pageNum).getRotation())
        {
            case 90:
                form.setMatrix(AffineTransform.getQuadrantRotateInstance(1));
                initialScale = Matrix.getScaleInstance(bbox.getWidth() / bbox.getHeight(), bbox.getHeight() / bbox.getWidth());
                height = bbox.getWidth();
                break;
            case 180:
                form.setMatrix(AffineTransform.getQuadrantRotateInstance(2));
                break;
            case 270:
                form.setMatrix(AffineTransform.getQuadrantRotateInstance(3));
                initialScale = Matrix.getScaleInstance(bbox.getWidth() / bbox.getHeight(), bbox.getHeight() / bbox.getWidth());
                height = bbox.getWidth();
                break;
            case 0:
            default:
                break;
        }
        form.setBBox(bbox);
        PDFont font = PDType1Font.HELVETICA;

        // from PDVisualSigBuilder.createAppearanceDictionary()
        PDAppearanceDictionary appearance = new PDAppearanceDictionary();
        appearance.getCOSObject().setDirect(true);
        PDAppearanceStream appearanceStream = new PDAppearanceStream(form.getCOSObject());
        appearance.setNormalAppearance(appearanceStream);
        widget.setAppearance(appearance);

        PDPageContentStream cs = new PDPageContentStream(doc, appearanceStream);

        // for 90° and 270° scale ratio of width / height
        // not really sure about this
        // why does scale have no effect when done in the form matrix???
        if (initialScale != null)
        {
            cs.transform(initialScale);
        }

        // show background (just for debugging, to see the rect size + position)
        cs.setNonStrokingColor(Color.lightGray);
        cs.addRect(-5000, -5000, 10000, 10000);
        cs.fill();

        if (imageFile != null)
        {
            // show background image
            // save and restore graphics if the image is too large and needs to be scaled
            cs.saveGraphicsState();
            cs.transform(Matrix.getScaleInstance(0.25f, 0.25f));
            try {
                PDImageXObject img = PDImageXObject.createFromFileByExtension(imageFile, doc);
                cs.drawImage(img, 1500, 150);
                cs.restoreGraphicsState();
            } catch (IOException ioe) {
                logger.getLogger().error(ioe.getMessage(), ioe);
                throw new ImageUnavailableException("Could not load image from file " + imageFile.getName(), ioe);
            }
        }

        // show text
        setTitle(cs, height,"Signature");
        addLine(cs, "This document has been digitally signed.");
        addLine(cs, "By: " + SIGNING_AUTHORITY_NAME);

        // TODO DCAC-108 Is date format in line with prototype? Also, needs to include timezone, unlike prototype.
        // See https://stackoverflow.com/questions/12575990
        // for better date formatting]
        addLine(cs, "On: " + pdSignature.getSignDate().getTime());

        addLine(cs, "");
        addPseudoLink(cs);
        cs.close();

        // no need to set annotations and /P entry
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        doc.save(baos);
        doc.close();
        return new ByteArrayInputStream(baos.toByteArray());
    }

    private void setTitle(final PDPageContentStream cs,
                          final float boxHeight,
                          final String title)
            throws IOException {
        final float fontSize = 14;
        final float leading = fontSize * 1.5f;
        cs.beginText();
        cs.setFont(PDType1Font.HELVETICA_BOLD, fontSize);
        cs.setNonStrokingColor(Color.black);
        cs.newLineAtOffset(fontSize, boxHeight - leading);
        cs.setLeading(leading);
        cs.showText(title);
        cs.newLine();
        cs.newLineAtOffset(fontSize * 3, 0);
    }

    private void addLine(final PDPageContentStream cs,
                         final String text) throws IOException {
        float fontSize = 10;
        float leading = fontSize * 1.5f;
        cs.setFont(PDType1Font.HELVETICA, fontSize);
        cs.setLeading(leading);
        cs.showText(text);
        cs.newLine();
    }

    private void addPseudoLink(final PDPageContentStream cs) throws IOException {
        cs.setNonStrokingColor(Color.BLUE);
        cs.showText ("Check signature validation status");
        cs.endText();
        cs.addRect(57,47, 145, 0.5F);
        cs.fill();
    }


}
