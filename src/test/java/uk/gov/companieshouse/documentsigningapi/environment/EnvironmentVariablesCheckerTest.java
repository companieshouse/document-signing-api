package uk.gov.companieshouse.documentsigningapi.environment;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import software.amazon.awssdk.services.s3.S3Client;

import java.util.Arrays;
import uk.org.webcompere.systemstubs.environment.EnvironmentVariables;
import uk.org.webcompere.systemstubs.jupiter.SystemStub;
import uk.org.webcompere.systemstubs.jupiter.SystemStubsExtension;

import static java.util.Arrays.stream;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.*;
import static uk.gov.companieshouse.documentsigningapi.environment.EnvironmentVariablesChecker.RequiredEnvironmentVariables.AWS_ACCESS_KEY_ID;
import static uk.gov.companieshouse.documentsigningapi.environment.EnvironmentVariablesChecker.RequiredEnvironmentVariables.AWS_REGION;
import static uk.gov.companieshouse.documentsigningapi.environment.EnvironmentVariablesChecker.RequiredEnvironmentVariables.AWS_SECRET_ACCESS_KEY;
import static uk.gov.companieshouse.documentsigningapi.environment.EnvironmentVariablesChecker.RequiredEnvironmentVariables.CERTIFICATE_ALIAS;
import static uk.gov.companieshouse.documentsigningapi.environment.EnvironmentVariablesChecker.RequiredEnvironmentVariables.KEYSTORE_PASSWORD;
import static uk.gov.companieshouse.documentsigningapi.environment.EnvironmentVariablesChecker.RequiredEnvironmentVariables.KEYSTORE_PATH;
import static uk.gov.companieshouse.documentsigningapi.environment.EnvironmentVariablesChecker.RequiredEnvironmentVariables.KEYSTORE_TYPE;
import static uk.gov.companieshouse.documentsigningapi.environment.EnvironmentVariablesChecker.RequiredEnvironmentVariables.SIGNED_DOC_BUCKET_NAME;

@ExtendWith(SystemStubsExtension.class)
class EnvironmentVariablesCheckerTest {

    private static final String TOKEN_VALUE = "token value";

    @SystemStub
    public EnvironmentVariables ENVIRONMENT_VARIABLES;

    @BeforeEach
    void setUp() {
        ENVIRONMENT_VARIABLES = new EnvironmentVariables();
    }

    @MockBean
    S3Client s3Client;

    @DisplayName("returns false if AWS_REGION is missing")
    @Test
    void checkEnvironmentVariablesAllPresentReturnsFalseIfRegionMissing() {
        populateAllVariablesExceptOneAndAssertSomethingMissing(AWS_REGION);
    }

    @DisplayName("returns false if AWS_ACCESS_KEY_ID is missing")
    @Test
    void checkEnvironmentVariablesAllPresentReturnsFalseIfAccessKeyIdMissing() {
        populateAllVariablesExceptOneAndAssertSomethingMissing(AWS_ACCESS_KEY_ID);
    }

    @DisplayName("returns false if AWS_SECRET_ACCESS_KEY is missing")
    @Test
    void checkEnvironmentVariablesAllPresentReturnsFalseIfSecretAccessKeyMissing() {
        populateAllVariablesExceptOneAndAssertSomethingMissing(AWS_SECRET_ACCESS_KEY);
    }

    @DisplayName("returns false if KEYSTORE_TYPE is missing")
    @Test
    void checkEnvironmentVariablesAllPresentReturnsFalseIfKeystoreTypeMissing() {
        populateAllVariablesExceptOneAndAssertSomethingMissing(KEYSTORE_TYPE);
    }

    @DisplayName("returns false if KEYSTORE_PATH is missing")
    @Test
    void checkEnvironmentVariablesAllPresentReturnsFalseIfKeystorePathMissing() {
        populateAllVariablesExceptOneAndAssertSomethingMissing(KEYSTORE_PATH);
    }

    @DisplayName("returns false if KEYSTORE_PASSWORD is missing")
    @Test
    void checkEnvironmentVariablesAllPresentReturnsFalseIfKeystorePasswordMissing() {
        populateAllVariablesExceptOneAndAssertSomethingMissing(KEYSTORE_PASSWORD);
    }

    @DisplayName("returns false if CERTIFICATE_ALIAS is missing")
    @Test
    void checkEnvironmentVariablesAllPresentReturnsFalseIfCertificateAliasMissing() {
        populateAllVariablesExceptOneAndAssertSomethingMissing(CERTIFICATE_ALIAS);
    }

    @DisplayName("returns false if SIGNED_DOC_BUCKET_NAME is missing")
    @Test
    void checkEnvironmentVariablesAllPresentReturnsFalseIfSignedDocBucketNameMissing() {
        populateAllVariablesExceptOneAndAssertSomethingMissing(SIGNED_DOC_BUCKET_NAME);
    }

    private void populateAllVariablesExceptOneAndAssertSomethingMissing(
            final EnvironmentVariablesChecker.RequiredEnvironmentVariables excludedVariable) {
        stream(EnvironmentVariablesChecker.RequiredEnvironmentVariables.values()).forEach(variable -> {
            if (variable != excludedVariable) {
                ENVIRONMENT_VARIABLES.set(variable.getName(), TOKEN_VALUE);
            }
        });
        boolean allPresent = EnvironmentVariablesChecker.allRequiredEnvironmentVariablesPresent();
        assertFalse(allPresent);
    }

    private void accept(EnvironmentVariablesChecker.RequiredEnvironmentVariables variable) {
        ENVIRONMENT_VARIABLES.set(variable.getName(), TOKEN_VALUE);
    }
}