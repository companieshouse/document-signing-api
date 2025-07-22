package uk.gov.companieshouse.documentsigningapi.coversheet;

import static org.apache.commons.lang3.StringUtils.isEmpty;

import java.io.File;
import java.io.IOException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

public class ImagesBean {

    private final String imagesPath;

    public ImagesBean(String imagesPath) {
        this.imagesPath = imagesPath;
    }

    public PDImageXObject createImage(final String fileName, final PDDocument pdfDocument)
            throws IOException {
        final String filePath =
                !isEmpty(imagesPath) ? imagesPath + File.separator + fileName : fileName;
        return PDImageXObject.createFromFile(filePath, pdfDocument);
    }
}
