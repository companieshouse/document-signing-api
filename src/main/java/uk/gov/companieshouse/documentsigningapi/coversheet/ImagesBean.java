package uk.gov.companieshouse.documentsigningapi.coversheet;

import java.io.IOException;
import java.util.Objects;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.springframework.stereotype.Component;

@Component
public class ImagesBean {

    // Classpath mapping once compiled
    private static final String CLASSPATH_FOLDER = "/coversheet/";

    public PDImageXObject createImage(final String fileName, final PDDocument pdfDocument)
            throws IOException {
        final String filePath = Objects.requireNonNull(
                getClass().getResource(CLASSPATH_FOLDER + fileName)).getPath();
        return PDImageXObject.createFromFile(filePath, pdfDocument);
    }
}