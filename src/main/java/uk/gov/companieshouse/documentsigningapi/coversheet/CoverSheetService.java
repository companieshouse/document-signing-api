package uk.gov.companieshouse.documentsigningapi.coversheet;

import org.apache.commons.lang.WordUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.color.PDColor;
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceRGB;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.pdmodel.interactive.action.PDActionURI;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationLink;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDBorderStyleDictionary;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.documentsigningapi.dto.CoverSheetDataDTO;
import uk.gov.companieshouse.documentsigningapi.dto.SignPdfRequestDTO;
import uk.gov.companieshouse.documentsigningapi.exception.CoverSheetException;
import uk.gov.companieshouse.documentsigningapi.logging.LoggingUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.apache.pdfbox.pdmodel.common.PDRectangle.A4;
import static uk.gov.companieshouse.documentsigningapi.coversheet.LayoutConstants.DEFAULT_MARGIN;
import static uk.gov.companieshouse.documentsigningapi.coversheet.LayoutConstants.POSTSCRIPT_TYPE_1_FONT_UPM;

@Service
public class CoverSheetService {

    private static class Link {
        private final String text;
        private final String url;

        private Link(String text, String url) {
            this.text = text;
            this.url = url;
        }
    }

    private static class Position {
        private final float x;
        private final float y;

        private Position(float x, float y) {
            this.x = x;
            this.y = y;
        }
    }

    // Coversheet measurements
    private static final float INFORMATION_SECTION_IMAGE_HEIGHT = 25;
    private static final float INFORMATION_SECTION_IMAGE_WIDTH = 25;
    private static final float OFFSET_TO_RIGHT_OF_IMAGES = 70;

    // Coversheet Strings
    private static final String CERTIFIED_DOCUMENT_TYPE = "Certified document";
    private static final String DOCUMENT_SIGNED_TEXT = "This file has been electronically signed by Companies House.";
    private static final String EMAIL_HELPTEXT = "This file can be emailed. You cannot make changes to this file.";
    private static final String PAGE_HEADING = "Companies House";
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

    static final PDColor BLUE = new PDColor(new float[] { 0, 0, 1 }, PDDeviceRGB.INSTANCE);
    static final PDColor BLACK = new PDColor(new float[] { 0, 0, 0 }, PDDeviceRGB.INSTANCE);

    private static final float LINE_OFFSET_BELOW_FONT = 3;

    private final LoggingUtils logger;

    private final ImagesBean images;

    private final Renderer renderer;

    private final VisualSignature visualSignature;

    public CoverSheetService(LoggingUtils logger, ImagesBean images, Renderer renderer, VisualSignature visualSignature) {
        this.logger = logger;
        this.images = images;
        this.renderer = renderer;
        this.visualSignature = visualSignature;
    }

    public byte[] addCoverSheet(final byte[] document,
                                final CoverSheetDataDTO coverSheetData,
                                final SignPdfRequestDTO signPdfData,
                                final Calendar signingDate) {
        try {
            final var pdfDocument = PDDocument.load(document);
            insertCoverSheet(pdfDocument, coverSheetData, signPdfData, signingDate);
            return getContents(pdfDocument);
        } catch (IOException ioe) {
            logger.getLogger().error(ioe.getMessage(), ioe);
            throw new CoverSheetException("Failed to add cover sheet to document", ioe);
        }
    }

    private void insertCoverSheet(final PDDocument pdfDocument,
                                  final CoverSheetDataDTO coverSheetData,
                                  final SignPdfRequestDTO signPdfData,
                                  final Calendar signingDate)
            throws IOException {
        final var coverSheet = new PDPage(A4);
        buildCoverSheetContent(pdfDocument, coverSheet, coverSheetData, signPdfData, signingDate);
        pdfDocument.getPages().insertBefore(coverSheet, pdfDocument.getPage(0));
    }

    private void buildCoverSheetContent(final PDDocument pdfDocument,
                                        final PDPage coverSheet,
                                        final CoverSheetDataDTO coverSheetData,
                                        final SignPdfRequestDTO signPdfData,
                                        final Calendar signingDate) throws IOException {
        PDImageXObject signatureImage = images.createImage("signature.jpeg", pdfDocument);
        PDImageXObject emailImage = images.createImage("email.jpeg", pdfDocument);
        PDImageXObject printerImage = images.createImage("printer.jpeg", pdfDocument);

        var contentStream = new PDPageContentStream(pdfDocument, coverSheet);

        renderer.insertText(contentStream, PAGE_HEADING, PDType1Font.HELVETICA_BOLD, 30, 770);
        renderer.insertText(contentStream, getTodaysDate(), PDType1Font.HELVETICA, 18, 750);
        renderer.insertText(contentStream, CERTIFIED_DOCUMENT_TYPE, PDType1Font.HELVETICA_BOLD, 24, 650);

        textWrapper(contentStream, getCompany(coverSheetData), 18, DEFAULT_MARGIN, 620);

        renderFilingHistoryDescriptionWithBoldText(
                extractFilingHistoryDescriptionHead(coverSheetData),
                buildFilingHistoryDescriptionTailWithValues(signPdfData, coverSheetData),
                new Font(PDType1Font.HELVETICA_BOLD, 18),
                new Font(PDType1Font.HELVETICA, 18),
                coverSheet,
                contentStream,
                new Position(DEFAULT_MARGIN, 590)
        );
        textWrapper(contentStream, DOCUMENT_SIGNED_TEXT, 18, DEFAULT_MARGIN, 555);

        renderer.renderPageSpacer(contentStream, 530);
        renderer.insertText(contentStream, VIEW_FILE_HEADING, PDType1Font.HELVETICA_BOLD, 18, 460);

        contentStream.drawImage(signatureImage, DEFAULT_MARGIN, 420, INFORMATION_SECTION_IMAGE_WIDTH, INFORMATION_SECTION_IMAGE_HEIGHT);
        textWrapper(contentStream, SIGNATURE_HELPTEXT_LINE_1, 14, OFFSET_TO_RIGHT_OF_IMAGES, 440);

        renderTextWithLink(
                SIGNATURE_HELPTEXT_LINE_2_START,
                SIGNATURE_HELPTEXT_LINE_2_END,
                new Link(SIGNATURE_HELPTEXT_LINE_2_LINK, ADOBE_DOWNLOAD_URL),
                new Font(PDType1Font.HELVETICA, 14),
                coverSheet,
                contentStream,
                new Position(OFFSET_TO_RIGHT_OF_IMAGES, 420));

        contentStream.drawImage(emailImage, DEFAULT_MARGIN, 370, INFORMATION_SECTION_IMAGE_WIDTH, INFORMATION_SECTION_IMAGE_HEIGHT);
        textWrapper(contentStream, EMAIL_HELPTEXT, 14, OFFSET_TO_RIGHT_OF_IMAGES, 385);

        contentStream.drawImage(printerImage, DEFAULT_MARGIN, 325, INFORMATION_SECTION_IMAGE_WIDTH, INFORMATION_SECTION_IMAGE_HEIGHT);
        textWrapper(contentStream, PRINTER_HELPTEXT, 14, OFFSET_TO_RIGHT_OF_IMAGES, 340);

        visualSignature.renderPanel(contentStream, pdfDocument, coverSheet, signingDate);

        contentStream.close();
    }

    private void textWrapper(PDPageContentStream contentStream, String textToWrap, float fontSize, float xPosition, float yPosition) throws IOException {
        String[] wrappedText = WordUtils.wrap(textToWrap, 80).split("\\r?\\n");

        for(var i = 0; i < wrappedText.length; i++){
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

    private String extractFilingHistoryDescriptionHead(final CoverSheetDataDTO coverSheetDataDTO) {
        String fullDescription = "Test";
        if (coverSheetDataDTO != null) {
            fullDescription = coverSheetDataDTO.getFilingHistoryDescription();


            if (fullDescription != null) {

                Pattern pattern = Pattern.compile("\\*\\*(.*?)\\*\\*");
                Matcher matcher = pattern.matcher(fullDescription);

                if (matcher.find()) {
                    return matcher.group(1);
                }
            }

        }
        return fullDescription;
    }

    private String extractFilingHistoryDescriptionTail(final CoverSheetDataDTO coverSheetDataDTO) {
        if (coverSheetDataDTO != null) {
            String fullDescription = coverSheetDataDTO.getFilingHistoryDescription();

            if (fullDescription != null) {

                Pattern pattern = Pattern.compile("\\*\\*(.*?)\\*\\*");
                Matcher matcher = pattern.matcher(fullDescription);

                if (matcher.find()) {
                    int endIndex = matcher.end();
                    return fullDescription.substring(endIndex);
                }
            }
        }
        return "";
    }

    private String buildFilingHistoryDescriptionTailWithValues(final SignPdfRequestDTO signPdfData, final  CoverSheetDataDTO coverSheetData) {
        String filingHistoryDescriptionTail = extractFilingHistoryDescriptionTail(coverSheetData);

        if(signPdfData != null && coverSheetData != null) {
            Map<String, String> descriptionValues = signPdfData.getFilingHistoryDescriptionValues();
            if (descriptionValues!= null) {
                filingHistoryDescriptionTail = replaceFilingHistoryDescriptionPlaceholders(filingHistoryDescriptionTail, descriptionValues);
            }
            filingHistoryDescriptionTail += " (" + coverSheetData.getFilingHistoryType() + ")";
        }
        return filingHistoryDescriptionTail;
    }

    private String replaceFilingHistoryDescriptionPlaceholders(String input, Map<String, String> placeholderValues) {
        for (Map.Entry<String, String> entry : placeholderValues.entrySet()) {
            String placeholder = "{" + entry.getKey() + "}";
            String replacement = entry.getValue();
            input = input.replace(placeholder, replacement);
        }
        return input;
    }

//    private void renderFilingHistoryDescriptionWithBoldText(final String filingHistoryDescriptionHead,
//                                                            final String filingHistoryDescriptionTail,
//                                                            final Font font1,
//                                                            final Font font2,
//                                                            final PDPage page,
//                                                            final PDPageContentStream contentStream,
//                                                            final Position position)
//            throws IOException {
//
//        String combinedText = filingHistoryDescriptionHead + filingHistoryDescriptionTail;
//
//        String[] wrappedText = WordUtils.wrap(combinedText, 60). split("\\r?\\n");
//
//        contentStream.beginText();
//        contentStream.newLineAtOffset(position.x, position.y);
//
//        for (int i=0; i < wrappedText.length; i++) {
//            String line = wrappedText[i];
//
//            int headIndex = line.indexOf(filingHistoryDescriptionHead);
//            boolean lineContainsHead = headIndex >= 0;
//
//            contentStream.setFont(font2.getPdFont(), font2.getSize());
//            contentStream.showText(lineContainsHead ? line.substring(0, headIndex) : line);
//
//            contentStream.setFont(font1.getPdFont(), font1.getSize());
//            if (lineContainsHead && line.length() > headIndex) {
//                contentStream.showText(line.substring(headIndex));
//            }
//
//            if (i < wrappedText.length - 1) {
//                contentStream.newLineAtOffset(0, -Math.max(font2.getSize(), font2.getSize()));
//            }
//        }
//
//        contentStream.endText();
//    };


    private void renderFilingHistoryDescriptionWithBoldText(final String filingHistoryDescriptionHead,
                                                            final String filingHistoryDescriptionTail,
                                                            final Font font1,
                                                            final Font font2,
                                                            final PDPage page,
                                                            final PDPageContentStream contentStream,
                                                            final Position position)
                                                            throws IOException {


        String combinedText = filingHistoryDescriptionHead + filingHistoryDescriptionTail;



        String[] wrappedText = WordUtils.wrap(combinedText, 60). split("\\r?\\n");

        contentStream.beginText();
        contentStream.newLineAtOffset(position.x, position.y);

        for (int i=0; i < wrappedText.length; i++) {
            String line = wrappedText[i];

            if(filingHistoryDescriptionHead != null){

            int headIndex = line.indexOf(filingHistoryDescriptionHead);



            if (headIndex >= 0) {
                contentStream.setFont(font1.getPdFont(), font1.getSize());
                contentStream.showText(line.substring(0, headIndex));

                contentStream.setFont(font2.getPdFont(), font2.getSize());
                contentStream.showText(line.substring(headIndex));
                if (i < wrappedText.length - 1) {
                    contentStream.newLineAtOffset(0, -font1.getSize());
                }
            } else {
                contentStream.setFont(font2.getPdFont(), font2.getSize());
                contentStream.showText(line);
                if (i < wrappedText.length - 1) {
                    contentStream.newLineAtOffset(0, -font2.getSize());
                }

            }
        }
        }
        contentStream.endText();
    };

    private void renderTextWithLink(final String preLinkText,
                                    final String postLinkText,
                                    final Link link,
                                    final Font font,
                                    final PDPage page,
                                    final PDPageContentStream contentStream,
                                    final Position position)
            throws IOException {
        final float upperRightY = getMediaBox(page).getUpperRightY();
        renderTextWithLink(preLinkText, link.text, postLinkText, contentStream, font, upperRightY, position);
        buildLink(preLinkText, link, font, page, upperRightY, position);
    }

    private void renderTextWithLink(final String preLinkText,
                                    final String linkText,
                                    final String postLinkText,
                                    final PDPageContentStream contentStream,
                                    final Font font,
                                    final float upperRightY,
                                    final Position position) throws IOException {
        contentStream.beginText();
        contentStream.setFont(font.getPdFont(), font.getSize());
        contentStream.newLineAtOffset(position.x, upperRightY - position.y);
        contentStream.showText(preLinkText);

        contentStream.setNonStrokingColor(BLUE);
        contentStream.showText(linkText);

        contentStream.setNonStrokingColor(BLACK);
        contentStream.showText(postLinkText);
        contentStream.endText();
    }

    private void buildLink(final String preLinkText,
                           final Link link,
                           final Font font,
                           final PDPage page,
                           final float upperRightY,
                           final Position position) throws IOException {
        final var linkAnnotation = new PDAnnotationLink();
        underlineLink(linkAnnotation);
        markUpClickableArea(preLinkText, link.text, linkAnnotation, font, upperRightY, position);
        setUpLinkAction(linkAnnotation, link.url);
        page.getAnnotations().add(linkAnnotation);
    }

    private void underlineLink(final PDAnnotationLink link) {
        final var underline = new PDBorderStyleDictionary();
        underline.setStyle(PDBorderStyleDictionary.STYLE_UNDERLINE);
        link.setColor(BLUE);
        link.setBorderStyle(underline);
    }

    private void markUpClickableArea(final String preLinkText,
                                     final String linkText,
                                     final PDAnnotationLink link,
                                     final Font font,
                                     final float upperRightY,
                                     final Position position) throws IOException {
        final float offset = font.getPdFont().getStringWidth(preLinkText) / POSTSCRIPT_TYPE_1_FONT_UPM * font.getSize();
        final float textWidth = font.getPdFont().getStringWidth(linkText) / POSTSCRIPT_TYPE_1_FONT_UPM * font.getSize();
        final float textHeight =
                font.getPdFont().getFontDescriptor().getCapHeight() / POSTSCRIPT_TYPE_1_FONT_UPM * font.getSize();
        final var rectangle = new PDRectangle();
        rectangle.setLowerLeftX(position.x + offset);
        rectangle.setLowerLeftY(upperRightY - position.y - LINE_OFFSET_BELOW_FONT);
        rectangle.setUpperRightX(position.x + offset + textWidth);
        rectangle.setUpperRightY(upperRightY - position.y + textHeight);
        link.setRectangle(rectangle);

        // Apparently this ensures the reliable rendering of annotations.
        // See https://issues.apache.org/jira/browse/PDFBOX-3141.
        // Without it, cannot see the link underline in Chrome browser.
        // Call this AFTER the rectangle has been set.
        link.constructAppearances();
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
