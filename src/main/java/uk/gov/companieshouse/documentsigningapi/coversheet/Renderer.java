package uk.gov.companieshouse.documentsigningapi.coversheet;

import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.springframework.stereotype.Component;

import java.io.IOException;

import static uk.gov.companieshouse.documentsigningapi.coversheet.LayoutConstants.DEFAULT_MARGIN;

@Component
public class Renderer {

    private static final String PAGE_SPACER = "___________________________________________________";

    public void renderPageSpacer(final PDPageContentStream contentStream, final float yPosition) throws IOException {
        insertText(contentStream, PAGE_SPACER, PDType1Font.HELVETICA, 18, yPosition);
    }

    public void insertText(final PDPageContentStream contentStream,
                           final String text,
                           final PDType1Font font,
                           final float fontSize,
                           final float yPosition) throws IOException {
        contentStream.beginText();
        contentStream.setFont(font, fontSize);
        contentStream.newLineAtOffset(DEFAULT_MARGIN, yPosition);
        contentStream.showText(text);
        contentStream.endText();
    }

}
