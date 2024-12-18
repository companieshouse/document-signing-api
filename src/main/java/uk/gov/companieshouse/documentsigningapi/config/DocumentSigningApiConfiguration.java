package uk.gov.companieshouse.documentsigningapi.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.companieshouse.documentsigningapi.coversheet.ImagesBean;
import uk.gov.companieshouse.environment.EnvironmentReader;
import uk.gov.companieshouse.environment.impl.EnvironmentReaderImpl;

@Configuration
public class DocumentSigningApiConfiguration {

    @Value("${environment.coversheet.images.path}")
    private String imagesPath;

    @Bean
    public ImagesBean imagesBean() {
        return new ImagesBean(imagesPath);
    }

    @Bean
    public EnvironmentReader environmentReader() {
        return new EnvironmentReaderImpl();
    }
}
