package uk.gov.companieshouse.documentsigningapi.coversheet;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.PDPageTree;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotation;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationLink;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.junit.jupiter.MockitoExtension;

import uk.gov.companieshouse.documentsigningapi.dto.CoverSheetDataDTO;
import uk.gov.companieshouse.documentsigningapi.dto.SignPdfRequestDTO;

import java.io.IOException;
import java.util.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class FilingHistoryGeneratorTest {

    private static final String FILING_HISTORY_DESCRIPTION_TYPE_1 = "Notice of **Administrator's proposal**";

    private static final String FILING_HISTORY_DESCRIPTION_TYPE_2 = "**Statement of Affairs**";

    private static final String FILING_HISTORY_DESCRIPTION_TYPE_3 = "**Registered office address changed** from {old_address} to {new_address} on {change_date}";

    private static final String FILING_HISTORY_DESCRIPTION_TYPE_4 = "{original_description}";

    private static final String FILING_HISTORY_DESCRIPTION_TYPE_5 = "Certificate that Creditors have been paid in full";
    private static final Map<String, String> FILING_HISTORY_DESCRIPTION_TYPE_3_VALUES = Map.of("old_address", "1 Test Lane",
            "new_address", "2 Test Lane", "change_date", "2023-01-01");

    private static final Map<String, String> FILING_HISTORY_DESCRIPTION_TYPE_4_VALUES = Map.of("original_description", "Test original description");

    @InjectMocks
    private FilingHistoryGenerator filingHistoryGenerator;

    @Mock
    private PDPageContentStream contentStream;

    @Mock
    private PDDocument document;

    @Mock
    private Font font;

    @Mock
    private List<PDAnnotation> annotations;

    @Mock
    private PDPageTree pages;

    @Mock
    private PDPage page;

    @BeforeEach
    void setUp() {
        filingHistoryGenerator = new FilingHistoryGenerator();
    }

    @Test
    @DisplayName("filing history generator renders filing history descriptions of type 1 text a total of 1 times")
    void rendersFilingHistoryDescriptionType1Text() throws IOException {
        executeTest((pdfBox, pageConstructor, streamConstructor, linkConstructor) -> {

            CoverSheetDataDTO coverSheetData = new CoverSheetDataDTO();
            coverSheetData.setFilingHistoryDescription(FILING_HISTORY_DESCRIPTION_TYPE_1);

            filingHistoryGenerator.renderFilingHistoryDescriptionType1(
                                                        coverSheetData,
                                                        new Font(PDType1Font.HELVETICA, 18),
                                                        contentStream,
                                                        25F,
                                                        590F);

            verify(contentStream, times(1)).showText(any(String.class));

        });
    }

    @Test
    @DisplayName("filing history generator renders filing history descriptions of type 2 text a total of 1 times")
    void rendersFilingHistoryDescriptionType2Text() throws IOException {
        executeTest((pdfBox, pageConstructor, streamConstructor, linkConstructor) -> {

            CoverSheetDataDTO coverSheetData = new CoverSheetDataDTO();
            coverSheetData.setFilingHistoryDescription(FILING_HISTORY_DESCRIPTION_TYPE_2);

            filingHistoryGenerator.renderFilingHistoryDescriptionType2(
                                                        coverSheetData,
                                                        new Font(PDType1Font.HELVETICA, 18),
                                                        contentStream,
                                                        25F,
                                                        590F);

            verify(contentStream, times(1)).showText(any(String.class));

        });
    }

    @Test
    @DisplayName("filing history generator renders filing history descriptions of type 3 text a total of 3 times")
    void rendersFilingHistoryDescriptionType3Text() throws IOException {
        executeTest((pdfBox, pageConstructor, streamConstructor, linkConstructor) -> {

            CoverSheetDataDTO coverSheetData = new CoverSheetDataDTO();
            coverSheetData.setFilingHistoryDescription(FILING_HISTORY_DESCRIPTION_TYPE_3);

            SignPdfRequestDTO signPdfRequestDTO = new SignPdfRequestDTO();
            signPdfRequestDTO.setFilingHistoryDescriptionValues(FILING_HISTORY_DESCRIPTION_TYPE_3_VALUES);

            filingHistoryGenerator.renderFilingHistoryDescriptionType3(
                                                        coverSheetData,
                                                        signPdfRequestDTO,
                                                        font,
                                                        font,
                                                        page,
                                                        contentStream,
                                                       25F,
                                                        590F);

            verify(contentStream, times(3)).showText(any(String.class));
        });
    }

    @Test
    @DisplayName("filing history generator renders filing history descriptions of type 4 text a total of 1 times")
    void rendersFilingHistoryDescriptionType4Text() throws IOException {
        executeTest((pdfBox, pageConstructor, streamConstructor, linkConstructor) -> {

            CoverSheetDataDTO coverSheetData = new CoverSheetDataDTO();
            coverSheetData.setFilingHistoryDescription(FILING_HISTORY_DESCRIPTION_TYPE_4);

            SignPdfRequestDTO signPdfRequestDTO = new SignPdfRequestDTO();
            signPdfRequestDTO.setFilingHistoryDescriptionValues(FILING_HISTORY_DESCRIPTION_TYPE_4_VALUES);

            filingHistoryGenerator.renderFilingHistoryDescriptionType4(
                                                        signPdfRequestDTO,
                                                        coverSheetData,
                                                        contentStream,
                                                        25F,
                                                        590F);

            verify(contentStream, times(1)).showText(any(String.class));
        });
    }

    @Test
    @DisplayName("filing history generator renders filing history descriptions of type 5 just once")
    void rendersFilingHistoryDescriptionType5Text() throws IOException {
        executeTest((pdfBox, pageConstructor, streamConstructor, linkConstructor) -> {

            CoverSheetDataDTO coverSheetData = new CoverSheetDataDTO();
            coverSheetData.setFilingHistoryDescription(FILING_HISTORY_DESCRIPTION_TYPE_5);
            SignPdfRequestDTO signPdfRequestDTO = new SignPdfRequestDTO();

            filingHistoryGenerator.renderFilingHistoryDescriptionType4(
                    signPdfRequestDTO,
                    coverSheetData,
                    contentStream,
                    25F,
                    590F);

            verify(contentStream, times(1)).showText(any(String.class));
        });
    }

    private void executeTest(final CoverSheetServiceTest.TestExecutor executor) throws IOException {
        try (final var pdfBox = mockStatic(PDDocument.class)) {
            try (final MockedConstruction<PDPage> pageConstructor =
                         mockConstructionWithAnswer(PDPage.class, invocationOnMock -> annotations)) {
                try (final var streamConstructor = mockConstruction(PDPageContentStream.class)) {
                    try (final var linkConstructor = mockConstruction(PDAnnotationLink.class)) {
                        pdfBox.when(() -> PDDocument.load(any(byte[].class))).thenReturn(document);
                        executor.executeTest(pdfBox, pageConstructor, streamConstructor, linkConstructor);
                    }
                }
            }
        }
    }

}
