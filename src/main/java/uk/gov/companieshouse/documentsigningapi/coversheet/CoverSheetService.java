package uk.gov.companieshouse.documentsigningapi.coversheet;

import static org.apache.pdfbox.pdmodel.common.PDRectangle.A4;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.documentsigningapi.exception.CoverSheetException;
import uk.gov.companieshouse.documentsigningapi.logging.LoggingUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Service
public class CoverSheetService {

    private final LoggingUtils logger;

    public CoverSheetService(LoggingUtils logger) {
        this.logger = logger;
    }

    public byte[] addCoverSheet(final byte[] document) {
        try {
            final var pdfDocument = PDDocument.load(document);
            insertBlankCoverSheet(pdfDocument);
            return getContents(pdfDocument);
        } catch (IOException ioe) {
            logger.getLogger().error(ioe.getMessage(), ioe);
            throw new CoverSheetException("Failed to add cover sheet to document", ioe);
        }
    }

    private void insertBlankCoverSheet(final PDDocument pdfDocument) throws IOException {
        final var blankPage = new PDPage(A4);

//        PDImageXObject signatureImage = PDImageXObject.createFromFile("./app/resources/coversheet/signature.jpeg", pdfDocument);

        // Build the coversheet
        PDPageContentStream contentStream = new PDPageContentStream(pdfDocument, blankPage);
        contentStream.beginText();
        contentStream.setFont(PDType1Font.HELVETICA_BOLD, 30);
        contentStream.newLineAtOffset(25, 770);
        contentStream.showText("Companies House");
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(PDType1Font.HELVETICA, 18);
        contentStream.newLineAtOffset(25, 750);
        contentStream.showText("2nd November 2022");
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(PDType1Font.HELVETICA_BOLD, 24);
        contentStream.newLineAtOffset(25, 650);
        contentStream.showText("Certified document");
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(PDType1Font.HELVETICA, 18);
        contentStream.newLineAtOffset(25, 620);
        contentStream.showText("TEST COMPANY NAME (00000000)");
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(PDType1Font.HELVETICA, 18);
        contentStream.newLineAtOffset(25, 590);
        contentStream.showText("Change of Registered Office Address (ADO1) ef");
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(PDType1Font.HELVETICA, 18);
        contentStream.newLineAtOffset(25, 560);
        contentStream.showText("This file has been electronically signed by Companies House.");
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(PDType1Font.HELVETICA, 18);
        contentStream.newLineAtOffset(25, 530);
        contentStream.showText("---------------------------------------------------------------------------------");
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(PDType1Font.HELVETICA_BOLD, 18);
        contentStream.newLineAtOffset(25, 480);
        contentStream.showText("View this file and signature in Adobe Reader");
        contentStream.endText();


//        contentStream.drawImage(signatureImage, 25, 450);



        contentStream.close();


        pdfDocument.getPages().insertBefore(blankPage, pdfDocument.getPage(0));
    }

    private byte[] getContents(final PDDocument pdfDocument) throws IOException {
        final var byteArrayOutputStream = new ByteArrayOutputStream();
        pdfDocument.save(byteArrayOutputStream);
        pdfDocument.close();
        return byteArrayOutputStream.toByteArray();
    }
}
