package uk.gov.companieshouse.documentsigningapi;

import org.junit.Rule;
import org.junit.contrib.java.lang.system.EnvironmentVariables;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import software.amazon.awssdk.services.s3.S3Client;
import uk.gov.companieshouse.documentsigningapi.environment.EnvironmentVariablesChecker;

import java.util.Arrays;

import static java.util.Arrays.stream;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static uk.gov.companieshouse.documentsigningapi.environment.EnvironmentVariablesChecker.RequiredEnvironmentVariables.AWS_ACCESS_KEY_ID;
import static uk.gov.companieshouse.documentsigningapi.environment.EnvironmentVariablesChecker.RequiredEnvironmentVariables.AWS_REGION;

@SpringBootTest
class DocumentSigningApiApplicationTests {

	private static final String TOKEN_VALUE = "token value";

	@MockBean
	private S3Client s3Client;

	@Rule
	public EnvironmentVariables environmentVariables = new EnvironmentVariables();

	@BeforeEach
	void setUp() {
		stream(EnvironmentVariablesChecker.RequiredEnvironmentVariables.values()).forEach(variable -> {
			environmentVariables.set(variable.getName(), TOKEN_VALUE);
			if (variable!= AWS_REGION) {
				environmentVariables.set(variable.getName(), TOKEN_VALUE);
			} else {
				environmentVariables.set(AWS_REGION.getName(), "eu-west-2");
			}
		});
	}

	@AfterEach
	void tearDown() {
		final String[] AllEnvironmentVariableNames =
				Arrays.stream(EnvironmentVariablesChecker.RequiredEnvironmentVariables.class.getEnumConstants())
						.map(Enum::name)
						.toArray(String[]::new);
		environmentVariables.clear(AllEnvironmentVariableNames);
	}

	@SuppressWarnings("squid:S2699") // at least one assertion
	@DisplayName("context loads")
	@Test
	void contextLoads() {
	}

	@DisplayName("runs app when all required environment variables are present")
	@Test
	void runsAppWhenAllRequiredEnvironmentVariablesPresent() {

		try (final var app = mockStatic(SpringApplication.class)) {
			app.when(() -> SpringApplication.run(DocumentSigningApiApplication.class, new String[0])).thenReturn(null);

			DocumentSigningApiApplication.main(new String[]{});

			app.verify(() -> SpringApplication.run(DocumentSigningApiApplication.class, new String[0]));
		}

	}

	@DisplayName("does not run app when a required environment variable is missing")
	@Test
	void doesNotRunAppWhenRequiredEnvironmentVariableMissing() {

		environmentVariables.clear(AWS_ACCESS_KEY_ID.getName());

		try (final var app = mockStatic(SpringApplication.class)) {

			DocumentSigningApiApplication.main(new String[]{});

			app.verify(() -> SpringApplication.run(DocumentSigningApiApplication.class, new String[0]), times(0));
		}

	}

}
