package uk.gov.companieshouse.documentsigningapi.coversheet;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.SignatureOptions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VisualSignatureTest {

    private static final int COVER_SHEET_PAGE_NO = 0;

    @InjectMocks
    private VisualSignature visualSignature;

    @Mock
    private PDPageContentStream contentStream;

    @Mock
    private PDDocument document;

    @Mock
    private PDPage coverSheet;

    @Mock
    private Calendar calendar;

    @Mock
    private Renderer renderer;

    @Mock
    private ImagesBean imagesBean;

    @Mock
    private PDImageXObject stamp;

    @Mock
    private PDRectangle cropBox;

    @Mock
    private OrdinalDateTimeFormatter formatter;

    @Mock
    private SignatureOptions options;

    @Test
    @DisplayName("renderPanel writes text and draws the Companies House stamp in the visual signature panel")
    void renderPanelWritesTextAndDrawsStamp() throws IOException {

        when(imagesBean.createImage(any(String.class), any(PDDocument.class))).thenReturn(stamp);
        when(coverSheet.getCropBox()).thenReturn(cropBox);

        visualSignature.renderPanel(contentStream, document, coverSheet, calendar);

        verify(contentStream, times(5)).showText(any(String.class));
        verify(contentStream).drawImage(
                eq(stamp), any(Float.class), any(Float.class), any(Float.class), any(Float.class));
    }

    @Test
    @DisplayName("renderSignatureLink sets up a visual signature link in the visual signature panel")
    void renderSignatureLinkSetsUpAVisualSignature() throws IOException {

        when(document.getPage(COVER_SHEET_PAGE_NO)).thenReturn(coverSheet);
        when(coverSheet.getCropBox()).thenReturn(cropBox);

        visualSignature.renderSignatureLink(options, document);

        verify(options).setVisualSignature(any(InputStream.class));
        verify(options).setPage(COVER_SHEET_PAGE_NO);
    }
}