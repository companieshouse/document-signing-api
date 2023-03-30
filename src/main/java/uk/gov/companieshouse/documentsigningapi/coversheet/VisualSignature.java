package uk.gov.companieshouse.documentsigningapi.coversheet;

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
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationWidget;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAppearanceDictionary;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAppearanceStream;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.SignatureOptions;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDField;
import org.apache.pdfbox.pdmodel.interactive.form.PDSignatureField;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.List;

import static uk.gov.companieshouse.documentsigningapi.coversheet.LayoutConstants.DEFAULT_MARGIN;
import static uk.gov.companieshouse.documentsigningapi.coversheet.LayoutConstants.POSTSCRIPT_TYPE_1_FONT_UPM;

@Component
public class VisualSignature {

    private static final String SIGNING_AUTHORITY_NAME = "Registrar of Companies";

    private static final String LINK_TEXT = "Check signature validation status";
    private final ImagesBean images;
    private final OrdinalDateTimeFormatter formatter;
    private final Renderer renderer;


    public VisualSignature(ImagesBean images,
                           OrdinalDateTimeFormatter formatter,
                           Renderer renderer) {
        this.images = images;
        this.formatter = formatter;
        this.renderer = renderer;
    }

    public void renderPanel(final PDPageContentStream contentStream,
                            final PDDocument document,
                            final PDPage coverSheet,
                            final Calendar signingDate) throws IOException {
        renderVisualSignaturePageSpacers(contentStream);

        final PDImageXObject img = images.createImage("digital-search-copy-stamp.jpeg", document);
        contentStream.drawImage(img, 350, 150, img.getWidth() * 0.25f, img.getHeight() * 0.25f);
        renderText(contentStream, coverSheet.getCropBox().getHeight() - 580, signingDate);
    }

    public void render(final SignatureOptions signatureOptions,
                       final PDDocument document) throws IOException {
        final PDPage coverSheet = document.getPage(0);
        // Set the signature rectangle
        // Although PDF coordinates start from the bottom, humans start from the top.
        // So a human would want to position a signature (x,y) units from the
        // top left of the displayed page, and the field has a horizontal width and a vertical height
        // regardless of page rotation.
        final Rectangle2D humanRect =
                new Rectangle2D.Float(
                        DEFAULT_MARGIN,
                        698,
                        getTextWidth(LINK_TEXT),
                        20);
        final PDRectangle signatureRectangle = createSignatureRectangle(document, humanRect);
        signatureOptions.setVisualSignature(
                createVisualSignatureTemplate(
                        coverSheet,
                        signatureRectangle));
        signatureOptions.setPage(0);
    }

    private PDRectangle createSignatureRectangle(final PDDocument doc, final Rectangle2D humanRect)
    {
        final float x = (float) humanRect.getX();
        final float y = (float) humanRect.getY();
        final float width = (float) humanRect.getWidth();
        final float height = (float) humanRect.getHeight();
        final PDPage coverSheet = doc.getPage(0);
        final PDRectangle coverSheetCropBox = coverSheet.getCropBox();
        final var signatureRectangle = new PDRectangle();
        signatureRectangle.setLowerLeftX(x);
        signatureRectangle.setUpperRightX(x + width);
        signatureRectangle.setLowerLeftY(coverSheetCropBox.getHeight() - y - height);
        signatureRectangle.setUpperRightY(coverSheetCropBox.getHeight() - y);
        return signatureRectangle;
    }

    // create a template PDF document with empty signature and return it as a stream.
    private InputStream createVisualSignatureTemplate(final PDPage coverSheet,
                                                      final PDRectangle signatureRectangle) throws IOException
    {
        final PDDocument doc = createDocumentForVisualSignature(coverSheet);
        final PDAnnotationWidget widget = buildVisualSignatureWidget(doc, signatureRectangle);
        final PDFormXObject form = buildVisualSignatureForm(doc, signatureRectangle);
        buildAppearanceDictionary(form, widget);
        return saveToInputStream(doc);
    }

    private InputStream saveToInputStream(final PDDocument doc) throws IOException {
        // no need to set annotations and /P entry
        final var outputStream = new ByteArrayOutputStream();
        doc.save(outputStream);
        doc.close();
        return new ByteArrayInputStream(outputStream.toByteArray());
    }

    private PDFormXObject buildVisualSignatureForm(final PDDocument doc, final PDRectangle signatureRectangle) {
        final var stream = new PDStream(doc);
        final var form = new PDFormXObject(stream);
        final var res = new PDResources();
        form.setResources(res);
        form.setFormType(1);
        final var bbox = new PDRectangle(signatureRectangle.getWidth(), signatureRectangle.getHeight());
        form.setBBox(bbox);
        return form;
    }

    private PDAnnotationWidget buildVisualSignatureWidget(final PDDocument doc, final PDRectangle signatureRectangle)
            throws IOException {
        final var acroForm = new PDAcroForm(doc);
        doc.getDocumentCatalog().setAcroForm(acroForm);
        final var signatureField = new PDSignatureField(acroForm);
        final PDAnnotationWidget widget = signatureField.getWidgets().get(0);
        final List<PDField> acroFormFields = acroForm.getFields();
        acroForm.setSignaturesExist(true);
        acroForm.setAppendOnly(true);
        acroForm.getCOSObject().setDirect(true);
        acroFormFields.add(signatureField);

        widget.setRectangle(signatureRectangle);
        return widget;
    }

    private PDDocument createDocumentForVisualSignature(final PDPage coverSheet) {
        final var doc = new PDDocument();
        final var page = new PDPage(coverSheet.getMediaBox());
        doc.addPage(page);
        return doc;
    }

    private void buildAppearanceDictionary(final PDFormXObject form, final PDAnnotationWidget widget) {
        final var appearance = new PDAppearanceDictionary();
        appearance.getCOSObject().setDirect(true);
        final var appearanceStream = new PDAppearanceStream(form.getCOSObject());
        appearance.setNormalAppearance(appearanceStream);
        widget.setAppearance(appearance);
    }

    private void renderText(final PDPageContentStream contentStream,
                            final float height,
                            final Calendar signingDate) throws IOException {
        contentStream.beginText();
        setTitle(contentStream, height,"Signature");
        addLine(contentStream, "This document has been digitally signed.");
        addLine(contentStream, "By: " + SIGNING_AUTHORITY_NAME);
        addLine(contentStream, "On: " + formatter.getDateTimeString(signingDate.getTime()));
        addPseudoLink(LINK_TEXT, contentStream);
    }

    private void setTitle(final PDPageContentStream cs,
                          final float boxHeight,
                          final String title)
            throws IOException {
        final float fontSize = 18;
        final float leading = fontSize * 1.5f;
        cs.setFont(PDType1Font.HELVETICA, fontSize);
        cs.setNonStrokingColor(Color.black);
        cs.newLineAtOffset(DEFAULT_MARGIN, boxHeight - leading);
        cs.setLeading(leading);
        cs.showText(title);
        cs.newLine();
        cs.newLineAtOffset(0, 0);
    }

    private void addLine(final PDPageContentStream cs,
                         final String text) throws IOException {
        float fontSize = 14;
        float leading = fontSize * 1.5f;
        cs.setFont(PDType1Font.HELVETICA, fontSize);
        cs.setLeading(leading);
        cs.showText(text);
        cs.newLine();
    }

    private void addPseudoLink(final String linkText, final PDPageContentStream cs) throws IOException {
        cs.newLineAtOffset(0, -14);
        cs.setNonStrokingColor(Color.BLUE);
        cs.showText(linkText);
        cs.endText();
        cs.addRect(DEFAULT_MARGIN, 128, getTextWidth(linkText), 1);
        cs.fill();
    }

    private void renderVisualSignaturePageSpacers(final PDPageContentStream contentStream) throws IOException {
        renderer.renderPageSpacer(contentStream, 280);
        renderer.renderPageSpacer(contentStream, 100);
    }

    private float getTextWidth(final String text) throws IOException {
        final float fontSize = 14;
        final PDFont font = PDType1Font.HELVETICA;
        return font.getStringWidth(text) / POSTSCRIPT_TYPE_1_FONT_UPM * fontSize;
    }

}