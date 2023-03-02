package uk.gov.companieshouse.documentsigningapi.environment;

import org.junit.Rule;
import org.junit.contrib.java.lang.system.EnvironmentVariables;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import software.amazon.awssdk.services.s3.S3Client;

import java.util.Arrays;

import static java.util.Arrays.stream;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.*;
import static uk.gov.companieshouse.documentsigningapi.environment.EnvironmentVariablesChecker.RequiredEnvironmentVariables.AWS_ACCESS_KEY_ID;
import static uk.gov.companieshouse.documentsigningapi.environment.EnvironmentVariablesChecker.RequiredEnvironmentVariables.AWS_REGION;
import static uk.gov.companieshouse.documentsigningapi.environment.EnvironmentVariablesChecker.RequiredEnvironmentVariables.AWS_SECRET_ACCESS_KEY;
import static uk.gov.companieshouse.documentsigningapi.environment.EnvironmentVariablesChecker.RequiredEnvironmentVariables.AWS_SESSION_TOKEN;
import static uk.gov.companieshouse.documentsigningapi.environment.EnvironmentVariablesChecker.RequiredEnvironmentVariables.SIGNED_DOC_BUCKET_NAME;

@SpringBootTest
class EnvironmentVariablesCheckerTest {

    private static final String TOKEN_VALUE = "token value";

    @Rule
    public EnvironmentVariables environmentVariables = new EnvironmentVariables();

    @MockBean
    S3Client s3Client;

    @AfterEach
    void tearDown() {
        final String[] AllEnvironmentVariableNames =
                Arrays.stream(EnvironmentVariablesChecker.RequiredEnvironmentVariables.class.getEnumConstants())
                        .map(Enum::name)
                        .toArray(String[]::new);
        environmentVariables.clear(AllEnvironmentVariableNames);
    }

    @DisplayName("returns true if all required environment variables are present")
    @Test
    void checkEnvironmentVariablesAllPresentReturnsTrue() {
        stream(EnvironmentVariablesChecker.RequiredEnvironmentVariables.values()).forEach(this::accept);
        boolean allPresent = EnvironmentVariablesChecker.allRequiredEnvironmentVariablesPresent();
        assertThat(allPresent, is(true));
    }

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

    @DisplayName("returns false if SIGNED_DOC_BUCKET_NAME is missing")
    @Test
    void checkEnvironmentVariablesAllPresentReturnsFalseIfSignedDocBucketNameMissing() {
        populateAllVariablesExceptOneAndAssertSomethingMissing(SIGNED_DOC_BUCKET_NAME);
    }

    @DisplayName("returns false if AWS_SESSION_TOKEN is missing")
    @Test
    void checkEnvironmentVariablesAllPresentReturnsFalseIfSessionTokenMissing() {
        populateAllVariablesExceptOneAndAssertSomethingMissing(AWS_SESSION_TOKEN);
    }

    private void populateAllVariablesExceptOneAndAssertSomethingMissing(
            final EnvironmentVariablesChecker.RequiredEnvironmentVariables excludedVariable) {
        stream(EnvironmentVariablesChecker.RequiredEnvironmentVariables.values()).forEach(variable -> {
            if (variable != excludedVariable) {
                environmentVariables.set(variable.getName(), TOKEN_VALUE);
            }
        });
        boolean allPresent = EnvironmentVariablesChecker.allRequiredEnvironmentVariablesPresent();
        assertFalse(allPresent);
    }

    private void accept(EnvironmentVariablesChecker.RequiredEnvironmentVariables variable) {
        environmentVariables.set(variable.getName(), TOKEN_VALUE);
    }
}