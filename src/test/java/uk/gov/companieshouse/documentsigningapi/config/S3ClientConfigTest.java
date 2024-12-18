package uk.gov.companieshouse.documentsigningapi.config;

import org.mockito.Mock;
import uk.gov.companieshouse.environment.EnvironmentReader;
import uk.org.webcompere.systemstubs.environment.EnvironmentVariables;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

class S3ClientConfigTest {

    @Mock
    static EnvironmentReader environmentReader;

    private static final S3ClientConfig CONFIG = new S3ClientConfig(environmentReader) {
        @Override
        protected String getRegion() {
            return "eu-west-2";
        }
    };

    public EnvironmentVariables environmentVariables = new EnvironmentVariables();

    @DisplayName("is able to produce an S3Client if a region is provided")
    @Test
    void isAbleToProduceS3ClientIfRegionIsProvided() {
        assertThat(CONFIG.s3Client(), is(notNullValue()));
    }

}