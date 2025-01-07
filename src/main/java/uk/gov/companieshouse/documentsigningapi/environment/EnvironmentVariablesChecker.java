package uk.gov.companieshouse.documentsigningapi.environment;

import uk.gov.companieshouse.environment.EnvironmentReader;
import uk.gov.companieshouse.environment.exception.EnvironmentVariableException;
import uk.gov.companieshouse.environment.impl.EnvironmentReaderImpl;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

import static uk.gov.companieshouse.documentsigningapi.logging.LoggingUtilsConfiguration.APPLICATION_NAME_SPACE;

public class EnvironmentVariablesChecker {

    private static final Logger LOGGER = LoggerFactory.getLogger(APPLICATION_NAME_SPACE);

    public enum RequiredEnvironmentVariables {
        AWS_REGION("AWS_REGION"),
        AWS_ACCESS_KEY_ID("AWS_ACCESS_KEY_ID"),
        AWS_SECRET_ACCESS_KEY("AWS_SECRET_ACCESS_KEY"),
        KEYSTORE_TYPE("KEYSTORE_TYPE"),
        KEYSTORE_PATH("KEYSTORE_PATH"),
        KEYSTORE_PASSWORD("KEYSTORE_PASSWORD"),
        CERTIFICATE_ALIAS("CERTIFICATE_ALIAS"),
        SIGNED_DOC_BUCKET_NAME("SIGNED_DOC_BUCKET_NAME");

        private final String name;

        RequiredEnvironmentVariables(String name) { this.name = name; }

        public String getName() { return this.name; }
    }

    /**
     * Method to check if all of the required configuration variables
     * defined in the RequiredEnvironmentVariables enum have been set to a value
     * @return <code>true</code> if all required environment variables have been set, <code>false</code> otherwise
     */
    public static boolean allRequiredEnvironmentVariablesPresent() {
        EnvironmentReader environmentReader = new EnvironmentReaderImpl();
        var allVariablesPresent = true;
        LOGGER.info("Checking all environment variables present");
        for(RequiredEnvironmentVariables param : RequiredEnvironmentVariables.values()) {
            try{
                environmentReader.getMandatoryString(param.getName());
            } catch (EnvironmentVariableException eve) {
                allVariablesPresent = false;
                LOGGER.error(String.format("Required config item %s missing", param.getName()));
            }
        }

        return allVariablesPresent;
    }
}
