package uk.gov.companieshouse.documentsigningapi.coversheet;

import org.apache.commons.lang.WordUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.color.PDColor;
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceRGB;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.pdmodel.interactive.action.PDActionURI;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationLink;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDBorderStyleDictionary;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.documentsigningapi.dto.CoverSheetDataDTO;
import uk.gov.companieshouse.documentsigningapi.exception.CoverSheetException;
import uk.gov.companieshouse.documentsigningapi.logging.LoggingUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static org.apache.commons.lang.StringUtils.isEmpty;
import static org.apache.pdfbox.pdmodel.common.PDRectangle.A4;

@Service
public class CoverSheetService {

    private final LoggingUtils logger;

    private final String imagesPath;

    // Coversheet measurements
    private static final float DEFAULT_LEFT_MARGIN = 25;
    private static final float INFORMATION_SECTION_IMAGE_HEIGHT = 25;
    private static final float INFORMATION_SECTION_IMAGE_WIDTH = 25;

    // Coversheet Strings
    private static final String CERTIFIED_DOCUMENT_TYPE = "Certified document";
    private static final String DOCUMENT_SIGNED_TEXT = "This file has been electronically signed by Companies House.";
    private static final String EMAIL_HELPTEXT = "This file can be emailed. You cannot make changes to this file.";
    private static final String PAGE_HEADING = "Companies House";
    private static final String PAGE_SPACER = "___________________________________________________";
    private static final String PRINTER_HELPTEXT = "This file is only valid in digital form as it contains a unique " +
        "electronic signature. Printed copies of this file will not be accepted.";

    private static final String SIGNATURE_HELPTEXT_LINE_1 = "This file has an embedded electronic signature " +
            "that is only accessible in Adobe";
    private static final String SIGNATURE_HELPTEXT_LINE_2_START = "Reader. You can ";
    private static final String SIGNATURE_HELPTEXT_LINE_2_LINK = "download Adobe Reader";
    private static final String SIGNATURE_HELPTEXT_LINE_2_END = " for free.";
    private static final String ADOBE_DOWNLOAD_URL = "https://get.adobe.com/reader/";

    private static final String VIEW_FILE_HEADING = "View this file and signature in Adobe Reader";

    // Other constants
    private static final String DAY_MONTH_YEAR_FORMAT = "d MMMM uuuu";
    private static final String DIRECTORY_SEPARATOR = "/";

    private static final PDColor BLUE = new PDColor(new float[] { 0, 0, 1 }, PDDeviceRGB.INSTANCE);
    private static final PDColor BLACK = new PDColor(new float[] { 0, 0, 0 }, PDDeviceRGB.INSTANCE);

    public CoverSheetService(LoggingUtils logger, @Value("${environment.coversheet.images.path}") String imagesPath) {
        this.logger = logger;
        this.imagesPath = imagesPath;
    }

    public byte[] addCoverSheet(final byte[] document, final CoverSheetDataDTO coverSheetData) {
        try {
            final var pdfDocument = PDDocument.load(document);
            insertCoverSheet(pdfDocument, coverSheetData);
            return getContents(pdfDocument);
        } catch (IOException ioe) {
            logger.getLogger().error(ioe.getMessage(), ioe);
            throw new CoverSheetException("Failed to add cover sheet to document", ioe);
        }
    }

    private void insertCoverSheet(final PDDocument pdfDocument, final CoverSheetDataDTO coverSheetData)
            throws IOException {
        final var coverSheet = new PDPage(A4);
        buildCoverSheetContent(pdfDocument, coverSheet, coverSheetData);
        pdfDocument.getPages().insertBefore(coverSheet, pdfDocument.getPage(0));
    }

    private void buildCoverSheetContent(final PDDocument pdfDocument,
                                        final PDPage coverSheet,
                                        final CoverSheetDataDTO coverSheetData) throws IOException {
        PDImageXObject signatureImage = createImage("signature.jpeg", pdfDocument);
        PDImageXObject emailImage = createImage("email.jpeg", pdfDocument);
        PDImageXObject printerImage = createImage("printer.jpeg", pdfDocument);

        PDPageContentStream contentStream = new PDPageContentStream(pdfDocument, coverSheet);

        insertText(contentStream, PAGE_HEADING, PDType1Font.HELVETICA_BOLD, 30, 770);
        insertText(contentStream, getTodaysDate(), PDType1Font.HELVETICA, 18, 750);
        insertText(contentStream, CERTIFIED_DOCUMENT_TYPE, PDType1Font.HELVETICA_BOLD, 24, 650);

        textWrapper(contentStream, getCompany(coverSheetData), 18, DEFAULT_LEFT_MARGIN, 620);
        textWrapper(contentStream, getFilingHistory(coverSheetData), 18, 25, 590);
        textWrapper(contentStream, DOCUMENT_SIGNED_TEXT, 18, DEFAULT_LEFT_MARGIN, 560);

        insertText(contentStream, PAGE_SPACER, PDType1Font.HELVETICA, 18, 530);
        insertText(contentStream, VIEW_FILE_HEADING, PDType1Font.HELVETICA_BOLD, 18, 480);

        contentStream.drawImage(signatureImage, DEFAULT_LEFT_MARGIN, 420, INFORMATION_SECTION_IMAGE_WIDTH, INFORMATION_SECTION_IMAGE_HEIGHT);
        textWrapper(contentStream, SIGNATURE_HELPTEXT_LINE_1, 14, 70, 440);

        renderTextWithLink(
                SIGNATURE_HELPTEXT_LINE_2_START,
                SIGNATURE_HELPTEXT_LINE_2_LINK,
                SIGNATURE_HELPTEXT_LINE_2_END,
                ADOBE_DOWNLOAD_URL,
                PDType1Font.HELVETICA,
                14,
                coverSheet,
                contentStream,
                70,
                420);

        contentStream.drawImage(emailImage, DEFAULT_LEFT_MARGIN, 370, INFORMATION_SECTION_IMAGE_WIDTH, INFORMATION_SECTION_IMAGE_HEIGHT);
        textWrapper(contentStream, EMAIL_HELPTEXT, 14, 70, 385);

        contentStream.drawImage(printerImage, DEFAULT_LEFT_MARGIN, 325, INFORMATION_SECTION_IMAGE_WIDTH, INFORMATION_SECTION_IMAGE_HEIGHT);
        textWrapper(contentStream, PRINTER_HELPTEXT, 14, 70, 340);

        contentStream.close();
    }

    private void insertText(PDPageContentStream contentStream, String text, PDType1Font font,
                            float fontSize, float yPosition) throws IOException {
        contentStream.beginText();
        contentStream.setFont(font, fontSize);
        contentStream.newLineAtOffset(DEFAULT_LEFT_MARGIN, yPosition);
        contentStream.showText(text);
        contentStream.endText();
    }

    private void textWrapper(PDPageContentStream contentStream, String textToWrap, float fontSize, float xPosition, float yPosition) throws IOException {
        String[] wrappedText = WordUtils.wrap(textToWrap, 80).split("\\r?\\n");

        for(int i = 0; i < wrappedText.length; i++){
            contentStream.beginText();
            contentStream.setFont(PDType1Font.HELVETICA, fontSize);
            contentStream.newLineAtOffset(xPosition,yPosition-i*15);
            contentStream.showText(wrappedText[i]);
            contentStream.newLine();
            contentStream.endText();
        }
    }

    private byte[] getContents(final PDDocument pdfDocument) throws IOException {
        final var byteArrayOutputStream = new ByteArrayOutputStream();
        pdfDocument.save(byteArrayOutputStream);
        pdfDocument.close();
        return byteArrayOutputStream.toByteArray();
    }

    private String getTodaysDate() {
        final var dtf = DateTimeFormatter.ofPattern(DAY_MONTH_YEAR_FORMAT);
        final var localDate = LocalDate.now();
        return dtf.format(localDate);
    }

    private String getCompany(final CoverSheetDataDTO data) {
        return data.getCompanyName() + " (" + data.getCompanyNumber()+ ")";
    }

    private String getFilingHistory(final CoverSheetDataDTO data) {
        return data.getFilingHistoryDescription() + " (" + data.getFilingHistoryType() + ")";
    }

    private PDImageXObject createImage(final String fileName, final PDDocument pdfDocument) throws IOException {
        final String filePath = !isEmpty(imagesPath) ? imagesPath + DIRECTORY_SEPARATOR + fileName : fileName;
        return PDImageXObject.createFromFile(filePath, pdfDocument);
    }


    // TODO DCAC-97 Reduce number of parameters.
    private void renderTextWithLink(final String preLinkText,
                                    final String linkText,
                                    final String postLinkText,
                                    final String linkUrl,
                                    final PDFont font,
                                    final float fontSize,
                                    final PDPage page,
                                    final PDPageContentStream contentStream,
                                    final float xPosition,
                                    final float yPosition)
            throws IOException {
        final float upperRightY = getMediaBox(page).getUpperRightY();
        renderTextWithLink(
                preLinkText, linkText, postLinkText, contentStream, font, fontSize, upperRightY, xPosition, yPosition);
        buildLink(preLinkText, linkText, linkUrl, font, fontSize, page, upperRightY, xPosition, yPosition);
    }

    private void renderTextWithLink(final String preLinkText,
                                    final String linkText,
                                    final String postLinkText,
                                    final PDPageContentStream contentStream,
                                    final PDFont font,
                                    final float fontSize,
                                    final float upperRightY,
                                    final float xPosition,
                                    final float yPosition) throws IOException {
        contentStream.beginText();
        contentStream.setFont(font, fontSize);
        contentStream.moveTextPositionByAmount( xPosition, upperRightY - yPosition);
        contentStream.drawString(preLinkText);

        contentStream.setNonStrokingColor(BLUE);
        contentStream.drawString(linkText);

        contentStream.setNonStrokingColor(BLACK);
        contentStream.drawString(postLinkText);
        contentStream.endText();
    }

    private void buildLink(final String preLinkText,
                           final String linkText,
                           final String linkUrl,
                           final PDFont font,
                           final float fontSize,
                           final PDPage page,
                           final float upperRightY,
                           final float xPosition,
                           final float yPosition) throws IOException {
        final var link = new PDAnnotationLink();
        underlineLink(link);
        markUpClickableArea(preLinkText, linkText, link, font, fontSize, upperRightY, xPosition, yPosition);
        setUpLinkAction(link, linkUrl);
        page.getAnnotations().add(link);
    }

    // TODO DCAC-97 This line does not render in Chrome. Can this be fixed?
    private void underlineLink(final PDAnnotationLink link) {
        final var underline = new PDBorderStyleDictionary();
        underline.setStyle(PDBorderStyleDictionary.STYLE_UNDERLINE);
        link.setColor(BLUE);
        link.setBorderStyle(underline);
    }

    private void markUpClickableArea(final String preLinkText,
                                     final String linkText,
                                     final PDAnnotationLink link,
                                     final PDFont font,
                                     final float fontSize,
                                     final float upperRightY,
                                     final float xPosition,
                                     final float yPosition) throws IOException {
        float offset = (font.getStringWidth(preLinkText) / 1000) * fontSize;
        float textWidth = (font.getStringWidth(linkText) / 1000) * fontSize;
        float textHeight = (float )(font.getFontDescriptor().getFontBoundingBox().getHeight() / 1000 * fontSize * 0.865);
        final var position = new PDRectangle();
        position.setLowerLeftX(xPosition + offset);
        position.setLowerLeftY(upperRightY - yPosition - 3);
        position.setUpperRightX(xPosition + offset + textWidth);
        position.setUpperRightY(upperRightY - yPosition + textHeight);
        link.setRectangle(position);
    }

    private void setUpLinkAction(final PDAnnotationLink link, final String linkUrl) {
        final var action = new PDActionURI();
        action.setURI(linkUrl);
        link.setAction(action);
    }

    protected PDRectangle getMediaBox(final PDPage page) {
        return page.getMediaBox();
    }
}
