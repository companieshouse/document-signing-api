package uk.gov.companieshouse.documentsigningapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import static uk.gov.companieshouse.documentsigningapi.environment.EnvironmentVariablesChecker.allRequiredEnvironmentVariablesSet;

@SpringBootApplication
public class DocumentSigningApiApplication {

	public static void main(String[] args) {
		if (allRequiredEnvironmentVariablesSet()) {
			SpringApplication.run(DocumentSigningApiApplication.class, args);
		}
	}

}
