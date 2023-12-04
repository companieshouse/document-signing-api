package uk.gov.companieshouse.documentsigningapi.coversheet;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.PDPageTree;
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

    private static final String FILING_HISTORY_DESCRIPTION = "**Registered office address changed** from {old_address} to {new_address} on {change_date}";

    private static final Map<String, String> FILING_HISTORY_DESCRIPTION_VALUES = Map.of("old_address", "1 Test Lane",
            "new_address", "2 Test Lane", "change_date", "01-01-2023");

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
    @DisplayName("filing history generator renders text a total of 3 times")
    void rendersText() throws IOException {
        executeTest((pdfBox, pageConstructor, streamConstructor, linkConstructor) -> {

            CoverSheetDataDTO coverSheetData = new CoverSheetDataDTO();
            coverSheetData.setFilingHistoryDescription(FILING_HISTORY_DESCRIPTION);

            SignPdfRequestDTO signPdfRequestDTO = new SignPdfRequestDTO();
            signPdfRequestDTO.setFilingHistoryDescriptionValues(FILING_HISTORY_DESCRIPTION_VALUES);

            filingHistoryGenerator.renderFilingHistoryDescriptionWithBoldText(
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
