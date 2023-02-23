package uk.gov.companieshouse.documentsigningapi.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import uk.gov.companieshouse.documentsigningapi.dto.SignPdfRequestDTO;

import java.nio.file.Path;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@Testcontainers
@SpringBootTest
class SignDocumentControllerIntegrationTest {

    @Container
    private static final LocalStackContainer localStackContainer =
            new LocalStackContainer(DockerImageName.parse("localstack/localstack:1.4"))
                    .withServices(LocalStackContainer.Service.S3);

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private S3Client s3Client;

    @TestConfiguration
    public static class Config {
        @Bean
        public S3Client s3Client() {
            return S3Client.builder().
                    endpointOverride(localStackContainer.getEndpointOverride(LocalStackContainer.Service.S3))
                    .region(Region.of(localStackContainer.getRegion()))
                    .credentialsProvider(
                            StaticCredentialsProvider.create(
                                    AwsBasicCredentials.create(
                                            localStackContainer.getAccessKey(),
                                            localStackContainer.getSecretKey())))
                    .build();
        }

        @Bean
        public ObjectMapper objectMapper() {
            return new ObjectMapper();
        }
    }

    @BeforeEach
    void setUp() {
        final var request =
                CreateBucketRequest.builder()
                        .bucket("document-api-images-cidev")
                        .build();
        s3Client.createBucket(request);
        final var request2 = PutObjectRequest.builder()
                        .bucket("document-api-images-cidev")
                        .key("9616659670.pdf")
                                .contentType(MediaType.APPLICATION_PDF.toString())
                                .build();
        s3Client.putObject(request2, Path.of("9616659670.pdf"));
    }

    @Test
    @DisplayName("signPdf returns the signed property location")
    void signPdfReturnsSignedPropertyLocation() throws Exception {

        final SignPdfRequestDTO signPdfRequestDTO = new SignPdfRequestDTO();
        signPdfRequestDTO.setDocumentLocation("https://document-api-images-cidev.s3.eu-west-2.amazonaws.com/9616659670.pdf");
        signPdfRequestDTO.setDocumentType("certified-copy");
        signPdfRequestDTO.setSignatureOptions(List.of("cover-sheet"));

        final ResultActions resultActions = mockMvc.perform(post("/document-signing/sign-pdf")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(signPdfRequestDTO)))
                .andExpect(status().isCreated());

    }

}
