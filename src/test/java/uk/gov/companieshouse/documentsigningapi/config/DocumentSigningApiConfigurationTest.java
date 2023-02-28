package uk.gov.companieshouse.documentsigningapi.config;

import org.junit.Rule;
import org.junit.contrib.java.lang.system.EnvironmentVariables;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

class DocumentSigningApiConfigurationTest {

    private static final DocumentSigningApiConfiguration CONFIG = new DocumentSigningApiConfiguration() {
        @Override
        protected String getRegion() {
            return "eu-west-2";
        }
    };

    @Rule
    public EnvironmentVariables environmentVariables = new EnvironmentVariables();

    @DisplayName("is able to produce an S3Client if a region is provided")
    @Test
    void isAbleToProduceS3ClientIfRegionIsProvided() {
        assertThat(CONFIG.s3Client(), is(notNullValue()));
    }

}