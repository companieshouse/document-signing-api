package uk.gov.companieshouse.documentsigningapi.coversheet;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.documentsigningapi.exception.CoverSheetException;
import uk.gov.companieshouse.documentsigningapi.logging.LoggingUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.apache.pdfbox.pdmodel.common.PDRectangle.A4;

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

    private void insertBlankCoverSheet(final PDDocument pdfDocument) {
        final var blankPage = new PDPage(A4);
        pdfDocument.getPages().insertBefore(blankPage, pdfDocument.getPage(0));
    }

    private byte[] getContents(final PDDocument pdfDocument) throws IOException {
        final var byteArrayOutputStream = new ByteArrayOutputStream();
        pdfDocument.save(byteArrayOutputStream);
        pdfDocument.close();
        return byteArrayOutputStream.toByteArray();
    }
}
