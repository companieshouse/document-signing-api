package uk.gov.companieshouse.documentsigningapi;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import software.amazon.awssdk.services.s3.S3Client;

@SpringBootTest
class DocumentSigningApiApplicationTests {

	@MockBean
	S3Client s3Client;

	@SuppressWarnings("squid:S2699") // at least one assertion
	@Test
	void contextLoads() {
	}

}
