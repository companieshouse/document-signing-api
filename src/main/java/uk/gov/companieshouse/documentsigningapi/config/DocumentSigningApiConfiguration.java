package uk.gov.companieshouse.documentsigningapi.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.companieshouse.documentsigningapi.coversheet.ImagesBean;
import uk.gov.companieshouse.environment.EnvironmentReader;
import uk.gov.companieshouse.environment.impl.EnvironmentReaderImpl;

@Configuration
public class DocumentSigningApiConfiguration {

    @Bean
    public ImagesBean imagesBean(
            @Value("${environment.coversheet.images.path:/opt}") String imagesPath) {
        return new ImagesBean(imagesPath);
    }

    @Bean
    public EnvironmentReader environmentReader() {
        return new EnvironmentReaderImpl();
    }
}
