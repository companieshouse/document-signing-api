package uk.gov.companieshouse.documentsigningapi.coversheet;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.common.PDStream;
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
    private static final String TITLE_TEXT = "Signature";
    private static final String THIS_DOCUMENT_TEXT = "This document has been digitally signed.";
    private static final String SIGNING_AUTHORITY_PREFIX = "By: ";
    private static final String SIGNING_DATE_PREFIX = "On: ";
    private static final String LINK_TEXT = "Check signature validation status";

    private static final int TOP_PAGE_SPACER_OFFSET_FROM_BOTTOM = 280;
    private static final int  BOTTOM_PAGE_SPACER_OFFSET_FROM_BOTTOM = 100;
    private static final float STAMP_SCALING_FACTOR = 0.25f;
    private static final int STAMP_OFFSET_FROM_LEFT = 350;
    private static final int STAMP_OFFSET_FROM_BOTTOM = 150;
    private static final int TEXT_OFFSET_FROM_TOP = 580;
    private static final int LINK_OFFSET_FROM_TOP = 698;
    private static final int LINK_HEIGHT = 20;
    private static final int LINK_UNDERLINING_OFFSET_FROM_BOTTOM = 128;
    private static final int LINK_UNDERLINING_THICKNESS = 1;
    private static final int COVER_SHEET_PAGE_NO = 0;

    private static final Font TITLE_FONT = new Font(PDType1Font.HELVETICA, 18);
    private static final Font TEXT_FONT = new Font(PDType1Font.HELVETICA, 14);

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
        renderCompaniesHouseStamp(contentStream, document);
        renderText(contentStream, coverSheet.getCropBox().getHeight() - TEXT_OFFSET_FROM_TOP, signingDate);
    }

    public void renderSignatureLink(final SignatureOptions signatureOptions,
                                    final PDDocument document) throws IOException {
        // Set the signature rectangle
        // Although PDF coordinates start from the bottom, humans start from the top.
        // So a human would want to position a signature (x,y) units from the
        // top left of the displayed page, and the field has a horizontal width and a vertical height
        // regardless of page rotation.
        final Rectangle2D humanRect =
                new Rectangle2D.Float(DEFAULT_MARGIN, LINK_OFFSET_FROM_TOP, getTextWidth(LINK_TEXT), LINK_HEIGHT);
        final PDRectangle signatureRectangle = createSignatureRectangle(document, humanRect);
        final PDPage coverSheet = getCoverSheet(document);
        signatureOptions.setVisualSignature(createVisualSignatureTemplate(coverSheet, signatureRectangle));
        signatureOptions.setPage(COVER_SHEET_PAGE_NO);
    }

    private PDRectangle createSignatureRectangle(final PDDocument document, final Rectangle2D humanRect)
    {
        final float x = (float) humanRect.getX();
        final float y = (float) humanRect.getY();
        final float width = (float) humanRect.getWidth();
        final float height = (float) humanRect.getHeight();
        final PDRectangle coverSheetCropBox = getCoverSheet(document).getCropBox();
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

    private InputStream saveToInputStream(final PDDocument document) throws IOException {
        // no need to set annotations and /P entry
        final var outputStream = new ByteArrayOutputStream();
        document.save(outputStream);
        document.close();
        return new ByteArrayInputStream(outputStream.toByteArray());
    }

    private PDFormXObject buildVisualSignatureForm(final PDDocument document, final PDRectangle signatureRectangle) {
        final var stream = new PDStream(document);
        final var form = new PDFormXObject(stream);
        final var res = new PDResources();
        form.setResources(res);
        form.setFormType(1);
        final var bbox = new PDRectangle(signatureRectangle.getWidth(), signatureRectangle.getHeight());
        form.setBBox(bbox);
        return form;
    }

    private PDAnnotationWidget buildVisualSignatureWidget(
            final PDDocument document, final PDRectangle signatureRectangle)
            throws IOException {
        final var acroForm = new PDAcroForm(document);
        document.getDocumentCatalog().setAcroForm(acroForm);
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
        final var document = new PDDocument();
        final var page = new PDPage(coverSheet.getMediaBox());
        document.addPage(page);
        return document;
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
        setTitle(contentStream, height, TITLE_TEXT);
        addLine(contentStream, THIS_DOCUMENT_TEXT);
        addLine(contentStream, SIGNING_AUTHORITY_PREFIX + SIGNING_AUTHORITY_NAME);
        addLine(contentStream, SIGNING_DATE_PREFIX + formatter.getDateTimeString(signingDate.getTime()));
        addPseudoLink(LINK_TEXT, contentStream);
    }

    private void setTitle(final PDPageContentStream contentStream,
                          final float boxHeight,
                          final String title)
            throws IOException {
        contentStream.setFont(TITLE_FONT.getPdFont(), TITLE_FONT.getSize());
        contentStream.setNonStrokingColor(Color.black);
        contentStream.newLineAtOffset(DEFAULT_MARGIN, boxHeight - TITLE_FONT.getLeading());
        contentStream.setLeading(TITLE_FONT.getLeading());
        contentStream.showText(title);
        contentStream.newLine();
        contentStream.newLineAtOffset(0, 0);
    }

    private void addLine(final PDPageContentStream contentStream,
                         final String text) throws IOException {
        contentStream.setFont(TEXT_FONT.getPdFont(), TEXT_FONT.getSize());
        contentStream.setLeading(TEXT_FONT.getLeading());
        contentStream.showText(text);
        contentStream.newLine();
    }

    private void addPseudoLink(final String linkText, final PDPageContentStream contentStream) throws IOException {
        contentStream.newLineAtOffset(0, -TEXT_FONT.getSize());
        contentStream.setNonStrokingColor(Color.BLUE);
        contentStream.showText(linkText);
        contentStream.endText();
        contentStream.addRect(
                DEFAULT_MARGIN,
                LINK_UNDERLINING_OFFSET_FROM_BOTTOM,
                getTextWidth(linkText),
                LINK_UNDERLINING_THICKNESS);
        contentStream.fill();
    }

    private void renderVisualSignaturePageSpacers(final PDPageContentStream contentStream) throws IOException {
        renderer.renderPageSpacer(contentStream, TOP_PAGE_SPACER_OFFSET_FROM_BOTTOM);
        renderer.renderPageSpacer(contentStream, BOTTOM_PAGE_SPACER_OFFSET_FROM_BOTTOM);
    }

    private float getTextWidth(final String text) throws IOException {
        return TEXT_FONT.getPdFont().getStringWidth(text) / POSTSCRIPT_TYPE_1_FONT_UPM * TEXT_FONT.getSize();
    }

    private void renderCompaniesHouseStamp(final PDPageContentStream contentStream,
                                           final PDDocument document) throws IOException {
        final PDImageXObject img = images.createImage("digital-search-copy-stamp.jpeg", document);
        contentStream.drawImage(
                img,
                STAMP_OFFSET_FROM_LEFT,
                STAMP_OFFSET_FROM_BOTTOM,
                img.getWidth() * STAMP_SCALING_FACTOR,
                img.getHeight() * STAMP_SCALING_FACTOR);
    }

    private PDPage getCoverSheet(final PDDocument document) {
        return document.getPage(COVER_SHEET_PAGE_NO);
    }

}