package uk.gov.companieshouse.documentsigningapi.coversheet;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.PDPageTree;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.pdmodel.interactive.action.PDActionURI;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotation;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationLink;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDBorderStyleDictionary;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.documentsigningapi.dto.CoverSheetDataDTO;
import uk.gov.companieshouse.documentsigningapi.exception.CoverSheetException;
import uk.gov.companieshouse.documentsigningapi.logging.LoggingUtils;
import uk.gov.companieshouse.logging.Logger;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.mockConstructionWithAnswer;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.companieshouse.documentsigningapi.coversheet.CoverSheetService.BLACK;
import static uk.gov.companieshouse.documentsigningapi.coversheet.CoverSheetService.BLUE;

@ExtendWith(MockitoExtension.class)
class CoverSheetServiceTest {

    private static final class TestCoverSheetService extends CoverSheetService {

        public TestCoverSheetService(LoggingUtils logger, ImagesBean images, Renderer renderer) {
            super(logger, images, renderer);
        }

        protected PDRectangle getMediaBox(final PDPage page) {
            return new PDRectangle();
        }
    }

    private static final IOException PDF_BOX_ORIGINATED_EXCEPTION = new IOException("Test originated exception");

    @InjectMocks
    private TestCoverSheetService coverSheetService;

    @Mock
    private LoggingUtils loggingUtils;

    @Mock
    private Logger logger;

    @Mock
    private ImagesBean imagesBean;

    @Mock
    private PDDocument document;

    @Mock
    private PDPageTree pages;

    @Mock
    private PDPage page;

    @Mock
    private PDImageXObject image;

    @Mock
    private List<PDAnnotation> annotations;

    @Mock
    private CoverSheetDataDTO coverSheetData;

    @Spy
    private Renderer renderer = new Renderer();

    @FunctionalInterface
    interface TestExecutor {
        void executeTest(MockedStatic<PDDocument> pdfBox,
                         MockedConstruction<PDPage> pageConstructor,
                         MockedConstruction<PDPageContentStream> streamConstructor,
                         MockedConstruction<PDAnnotationLink> linkConstructor) throws IOException;
    }

    @Test
    @DisplayName("addCoverSheet delegates cover sheet creation to pdfBox")
    void delegatesCoverSheetCreationToPdfBox() throws IOException {
        executeTest((pdfBox, pageConstructor, streamConstructor, linkConstructor) -> {

            final byte[] docWithCoverSheet =
                    coverSheetService.addCoverSheet(new byte[]{}, new CoverSheetDataDTO());

            assertThat(pageConstructor.constructed().size(), is(1));
            verify(pages).insertBefore(pageConstructor.constructed().get(0), page);
            verify(document).save(any(ByteArrayOutputStream.class));
            verify(document).close();
            assertThat(docWithCoverSheet, is(new byte[]{}));
        });
    }

    @Test
    @DisplayName("addCoverSheet loads and renders 3 images")
    void loadsAndRendersImages() throws IOException {
        executeTest((pdfBox, pageConstructor, streamConstructor, linkConstructor) -> {

            when(imagesBean.createImage(any(String.class), eq(document))).thenReturn(image);

            coverSheetService.addCoverSheet(new byte[]{}, new CoverSheetDataDTO());

            verify(imagesBean, times(3)).createImage(any(String.class), eq(document));
            assertThat(streamConstructor.constructed().size(), is (1));
            final var stream = streamConstructor.constructed().get(0);
            verify(stream, times(3))
                    .drawImage(eq(image),
                            any(float.class),
                            any(float.class),
                            any(float.class),
                            any(float.class));
        });
    }

    @Test
    @DisplayName("addCoverSheet renders text 17 times")
    void rendersText() throws IOException {
        executeTest((pdfBox, pageConstructor, streamConstructor, linkConstructor) -> {

            coverSheetService.addCoverSheet(new byte[]{}, new CoverSheetDataDTO());

            assertThat(streamConstructor.constructed().size(), is (1));
            final var stream = streamConstructor.constructed().get(0);
            verify(stream, times(17)).showText(any(String.class));
        });
    }

    @Test
    @DisplayName("addCoverSheet creates hyperlink and adds it to the coversheet")
    void createsLinkAndAddsItToCoverSheet() throws IOException {
        executeTest((pdfBox, pageConstructor, streamConstructor, linkConstructor) -> {

            coverSheetService.addCoverSheet(new byte[]{}, new CoverSheetDataDTO());

            assertThat(pageConstructor.constructed().size(), is(1));
            final var coverSheet = pageConstructor.constructed().get(0);
            assertThat(linkConstructor.constructed().size(), is (1));
            final var link = linkConstructor.constructed().get(0);
            verify(coverSheet).getAnnotations();
            verify(annotations).add(link);
        });
    }

    @Test
    @DisplayName("addCoverSheet renders link text in blue")
    void rendersLinkTextInBlue() throws IOException {
        executeTest((pdfBox, pageConstructor, streamConstructor, linkConstructor) -> {

            coverSheetService.addCoverSheet(new byte[]{}, new CoverSheetDataDTO());

            assertThat(streamConstructor.constructed().size(), is (1));
            final var stream = streamConstructor.constructed().get(0);
            verify(stream).setNonStrokingColor(BLUE);
            verify(stream).setNonStrokingColor(BLACK);
        });
    }

    @Test
    @DisplayName("addCoverSheet underlines the hyperlink")
    void underlinesLink() throws IOException {
        executeTest((pdfBox, pageConstructor, streamConstructor, linkConstructor) -> {

            coverSheetService.addCoverSheet(new byte[]{}, new CoverSheetDataDTO());

            assertThat(linkConstructor.constructed().size(), is (1));
            final var link = linkConstructor.constructed().get(0);
            verify(link).setColor(BLUE);
            verify(link).setBorderStyle(any(PDBorderStyleDictionary.class));
        });
    }

    @Test
    @DisplayName("addCoverSheet marks up the clickable area")
    void marksUpClickableArea() throws IOException {
        executeTest((pdfBox, pageConstructor, streamConstructor, linkConstructor) -> {

            coverSheetService.addCoverSheet(new byte[]{}, new CoverSheetDataDTO());

            assertThat(linkConstructor.constructed().size(), is (1));
            final var link = linkConstructor.constructed().get(0);
            verify(link).setRectangle(any(PDRectangle.class));
            verify(link).constructAppearances();
        });
    }

    @Test
    @DisplayName("addCoverSheet sets up the hyperlink with a URI action")
    void setsUpLinkAction() throws IOException {
        executeTest((pdfBox, pageConstructor, streamConstructor, linkConstructor) -> {

            coverSheetService.addCoverSheet(new byte[]{}, new CoverSheetDataDTO());

            assertThat(linkConstructor.constructed().size(), is (1));
            final var link = linkConstructor.constructed().get(0);
            verify(link).setAction(any(PDActionURI.class));
        });
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
                                () -> coverSheetService.addCoverSheet(new byte[]{}, coverSheetData));

                assertThat(pageConstructor.constructed().size(), is(0));
                verify(logger).error(PDF_BOX_ORIGINATED_EXCEPTION.getMessage(), PDF_BOX_ORIGINATED_EXCEPTION);
                assertThat(exception.getMessage(), is("Failed to add cover sheet to document"));
                assertThat(exception.getCause(), is(PDF_BOX_ORIGINATED_EXCEPTION));
            }
        }
    }

    private void executeTest(final TestExecutor executor) throws IOException {
        try (final var pdfBox = mockStatic(PDDocument.class)) {
            try (final MockedConstruction<PDPage> pageConstructor =
                         mockConstructionWithAnswer(PDPage.class, invocationOnMock -> annotations)) {
                try (final var streamConstructor = mockConstruction(PDPageContentStream.class)) {
                    try (final var linkConstructor = mockConstruction(PDAnnotationLink.class)) {
                        pdfBox.when(() -> PDDocument.load(any(byte[].class))).thenReturn(document);
                        when(document.getPages()).thenReturn(pages);
                        when(document.getPage(0)).thenReturn(page);
                        executor.executeTest(pdfBox, pageConstructor, streamConstructor, linkConstructor);
                    }
                }
            }
        }
    }

}