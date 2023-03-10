package uk.gov.companieshouse.documentsigningapi.coversheet;

import static org.apache.pdfbox.pdmodel.common.PDRectangle.A4;

import org.apache.commons.lang.WordUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.documentsigningapi.exception.CoverSheetException;
import uk.gov.companieshouse.documentsigningapi.logging.LoggingUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Service
public class CoverSheetService {

    private final LoggingUtils logger;

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
    private static final String SIGNATURE_HELPTEXT = "This file has an embedded electronic signature " +
        "that is only accessible in Adobe Reader. You can download Adobe Reader for free";
    private static final String VIEW_FILE_HEADING = "View this file and signature in Adobe Reader";

    public CoverSheetService(LoggingUtils logger) {
        this.logger = logger;
    }

    public byte[] addCoverSheet(final byte[] document) {
        try {
            final var pdfDocument = PDDocument.load(document);
            insertCoverSheet(pdfDocument);
            return getContents(pdfDocument);
        } catch (IOException ioe) {
            logger.getLogger().error(ioe.getMessage(), ioe);
            throw new CoverSheetException("Failed to add cover sheet to document", ioe);
        }
    }

    private void insertCoverSheet(final PDDocument pdfDocument) throws IOException {
        final var coverSheet = new PDPage(A4);
        buildCoverSheetContent(pdfDocument, coverSheet);
        pdfDocument.getPages().insertBefore(coverSheet, pdfDocument.getPage(0));
    }

    private void buildCoverSheetContent(PDDocument pdfDocument, PDPage coverSheet) throws IOException {
        //TODO CURRENTLY HARDCODED THE LOCATION OF THE IMAGES FOR DOCKER ENV, WILL NEED TO BE UPDATED
        PDImageXObject signatureImage = PDImageXObject.createFromFile("./app/resources/coversheet/signature.jpeg", pdfDocument);
        PDImageXObject emailImage = PDImageXObject.createFromFile("./app/resources/coversheet/email.jpeg", pdfDocument);
        PDImageXObject printerImage = PDImageXObject.createFromFile("./app/resources/coversheet/printer.jpeg", pdfDocument);

        PDPageContentStream contentStream = new PDPageContentStream(pdfDocument, coverSheet);

        insertText(contentStream, PAGE_HEADING, PDType1Font.HELVETICA_BOLD, 30, 770);

        // TODO REPLACE WITH DATE OF SIGNATURE
        insertText(contentStream, "2nd November 2022", PDType1Font.HELVETICA, 18, 750);
        insertText(contentStream, CERTIFIED_DOCUMENT_TYPE, PDType1Font.HELVETICA_BOLD, 24, 650);

        //TODO REPLACE WITH COMPANY NAME FROM REQUEST
        textWrapper(contentStream, "TEST COMPANY NAME (00000000)", 18, DEFAULT_LEFT_MARGIN, 620);
        //TODO REPLACE WITH DETAILS OF DOUCMENT BEING SIGNED
        textWrapper(contentStream, "Change of Registered Office Address (ADO1) ef", 18, 25, 590);
        textWrapper(contentStream, DOCUMENT_SIGNED_TEXT, 18, DEFAULT_LEFT_MARGIN, 560);

        insertText(contentStream, PAGE_SPACER, PDType1Font.HELVETICA, 18, 530);
        insertText(contentStream, VIEW_FILE_HEADING, PDType1Font.HELVETICA_BOLD, 18, 480);

        contentStream.drawImage(signatureImage, DEFAULT_LEFT_MARGIN, 420, INFORMATION_SECTION_IMAGE_WIDTH, INFORMATION_SECTION_IMAGE_HEIGHT);
        textWrapper(contentStream, SIGNATURE_HELPTEXT, 14, 70, 440);

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
}
