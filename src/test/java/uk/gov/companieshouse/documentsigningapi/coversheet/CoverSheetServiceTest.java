package uk.gov.companieshouse.documentsigningapi.coversheet;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.PDPageTree;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.documentsigningapi.dto.CoverSheetDataDTO;
import uk.gov.companieshouse.documentsigningapi.exception.CoverSheetException;
import uk.gov.companieshouse.documentsigningapi.logging.LoggingUtils;
import uk.gov.companieshouse.logging.Logger;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CoverSheetServiceTest {

    private static final IOException PDF_BOX_ORIGINATED_EXCEPTION = new IOException("Test originated exception");

    @InjectMocks
    private CoverSheetService coverSheetService;

    @Mock
    private LoggingUtils loggingUtils;

    @Mock
    private Logger logger;

    @Mock
    private PDDocument document;

    @Mock
    private PDPageTree pages;

    @Mock
    private PDPage page;

    @Test
    @DisplayName("addCoverSheet delegates cover sheet creation to pdfBox")
    void delegatesCoverSheetCreationToPdfBox() throws IOException {
        try (final var pdfBox = mockStatic(PDDocument.class)) {
            try (final MockedConstruction<PDPage> pageConstructor = mockConstruction(PDPage.class)) {
                try (final var image = mockStatic(PDImageXObject.class)) {
                    try (final var stream = mockConstruction(PDPageContentStream.class)) {
                        pdfBox.when(() -> PDDocument.load(any(byte[].class))).thenReturn(document);
                        when(document.getPages()).thenReturn(pages);
                        when(document.getPage(0)).thenReturn(page);

                        final byte[] docWithCoverSheet =
                                coverSheetService.addCoverSheet(new byte[]{}, new CoverSheetDataDTO());

                        assertThat(pageConstructor.constructed().size(), is(1));
                        verify(pages).insertBefore(pageConstructor.constructed().get(0), page);
                        verify(document).save(any(ByteArrayOutputStream.class));
                        verify(document).close();
                        assertThat(docWithCoverSheet, is(new byte[]{}));
                    }
                }
            }
        }
    }

    @Test
    @DisplayName("addCoverSheet propagates IOException wrapped as a CoverSheetException")
    void propagatesIoExceptionAsCoverSheetException() {
        try (final var pdfBox = mockStatic(PDDocument.class)) {
            try (final MockedConstruction<PDPage> pageConstructor = mockConstruction(PDPage.class)){
                pdfBox.when(() -> PDDocument.load(any(byte[].class))).thenThrow(PDF_BOX_ORIGINATED_EXCEPTION);
                when(loggingUtils.getLogger()).thenReturn(logger);

                final CoverSheetException exception =
                        assertThrows(CoverSheetException.class,
                                () -> coverSheetService.addCoverSheet(new byte[]{}, new CoverSheetDataDTO()));

                assertThat(pageConstructor.constructed().size(), is(0));
                verify(logger).error(PDF_BOX_ORIGINATED_EXCEPTION.getMessage(), PDF_BOX_ORIGINATED_EXCEPTION);
                assertThat(exception.getMessage(), is("Failed to add cover sheet to document"));
                assertThat(exception.getCause(), is(PDF_BOX_ORIGINATED_EXCEPTION));
            }
        }
    }

}