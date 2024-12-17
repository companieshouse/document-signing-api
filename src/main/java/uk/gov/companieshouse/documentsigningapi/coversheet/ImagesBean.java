package uk.gov.companieshouse.documentsigningapi.coversheet;

import java.io.File;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import java.io.IOException;

import static org.apache.commons.lang.StringUtils.isEmpty;

@Component
@PropertySource("classpath:application.properties")
public class ImagesBean {
    private final String imagesPath;

    public ImagesBean(@Value("${environment.coversheet.images.path}") String imagesPath) {
        this.imagesPath = imagesPath;
    }

    public PDImageXObject createImage(final String fileName, final PDDocument pdfDocument) throws IOException {
        final String filePath = !isEmpty(imagesPath) ? imagesPath + File.separator + fileName : fileName;
        return PDImageXObject.createFromFile(filePath, pdfDocument);
    }
}
