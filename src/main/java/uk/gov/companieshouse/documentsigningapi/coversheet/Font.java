package uk.gov.companieshouse.documentsigningapi.coversheet;

import org.apache.pdfbox.pdmodel.font.PDFont;

class Font {

    private static final float LEADING_FACTOR = 1.5f;

    private final PDFont pdFont;
    private final float size;

    Font(PDFont pdFont, float size) {
        this.pdFont = pdFont;
        this.size = size;
    }

    PDFont getPdFont() {
        return pdFont;
    }

    float getSize() {
        return size;
    }

    float getLeading() {
        return size * LEADING_FACTOR;
    }
}
