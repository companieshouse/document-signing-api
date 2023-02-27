package uk.gov.companieshouse.documentsigningapi.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
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
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import uk.gov.companieshouse.documentsigningapi.dto.SignPdfRequestDTO;
import uk.gov.companieshouse.documentsigningapi.dto.SignPdfResponseDTO;

import java.io.UnsupportedEncodingException;
import java.nio.file.Path;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@Testcontainers
@SpringBootTest
class SignDocumentControllerIntegrationTest {

    private static final String BUCKET_NAME = "document-api-images-cidev";
    private static final String UNSIGNED_DOCUMENT_NAME = "9616659670.pdf";
    private static final String UNKNOWN_UNSIGNED_DOCUMENT_NAME = "UNKNOWN.pdf";

    @Container
    private static final LocalStackContainer localStackContainer =
            new LocalStackContainer().withServices(LocalStackContainer.Service.S3);

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
        setUpUnsignedDocumentInBucket();
    }

    @Test
    @DisplayName("signPdf returns the signed document location")
    void signPdfReturnsSignedDocumentLocation() throws Exception {

        final SignPdfRequestDTO signPdfRequestDTO = new SignPdfRequestDTO();
        // It seems that LocalStack S3 is somewhat region-agnostic.
        final String unsignedDocumentLocation =
                "https://" + BUCKET_NAME + ".s3.eu-west-2.amazonaws.com/" + UNSIGNED_DOCUMENT_NAME;
        signPdfRequestDTO.setDocumentLocation(unsignedDocumentLocation);
        signPdfRequestDTO.setDocumentType("certified-copy");
        signPdfRequestDTO.setSignatureOptions(List.of("cover-sheet"));

        final ResultActions resultActions = mockMvc.perform(post("/document-signing/sign-pdf")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(signPdfRequestDTO)))
                .andExpect(status().isCreated());
        
        final SignPdfResponseDTO signPdfResponseDTO = getResponseDTO(resultActions);
        assertThat(signPdfResponseDTO.getSignedDocumentLocation(), is(unsignedDocumentLocation));
    }

    @Test
    @DisplayName("signPdf with invalid unsigned document location responds with bad request")
    void signPdfWithInvalidDocumentLocation() throws Exception {

        final SignPdfRequestDTO signPdfRequestDTO = new SignPdfRequestDTO();
        // It seems that LocalStack S3 is somewhat region-agnostic.
        final String unsignedDocumentLocation =
                "https:// " + BUCKET_NAME + ".s3.eu-west-2.amazonaws.com/" + UNSIGNED_DOCUMENT_NAME;
        signPdfRequestDTO.setDocumentLocation(unsignedDocumentLocation);
        signPdfRequestDTO.setDocumentType("certified-copy");
        signPdfRequestDTO.setSignatureOptions(List.of("cover-sheet"));

        final ResultActions resultActions = mockMvc.perform(post("/document-signing/sign-pdf")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(signPdfRequestDTO)))
                .andExpect(status().isBadRequest());

        final String body = resultActions.andReturn().getResponse().getContentAsString();
        assertThat(body, is("Illegal character in authority at index 8: " +
                "https:// document-api-images-cidev.s3.eu-west-2.amazonaws.com/9616659670.pdf"));
    }

    @Test
    @DisplayName("signPdf with incorrect unsigned document location responds with not found")
    void signPdfWithUnknownDocumentLocation() throws Exception {

        final SignPdfRequestDTO signPdfRequestDTO = new SignPdfRequestDTO();
        // It seems that LocalStack S3 is somewhat region-agnostic.
        final String unsignedDocumentLocation =
                "https://" + BUCKET_NAME + ".s3.eu-west-2.amazonaws.com/" + UNKNOWN_UNSIGNED_DOCUMENT_NAME;
        signPdfRequestDTO.setDocumentLocation(unsignedDocumentLocation);
        signPdfRequestDTO.setDocumentType("certified-copy");
        signPdfRequestDTO.setSignatureOptions(List.of("cover-sheet"));

        final ResultActions resultActions = mockMvc.perform(post("/document-signing/sign-pdf")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(signPdfRequestDTO)))
                .andExpect(status().isNotFound());

        final String body = resultActions.andReturn().getResponse().getContentAsString();
        assertThat(body, startsWith("The specified key does not exist. " +
                "(Service: S3, Status Code: 404, Request ID: 7a62c49f-347e-4fc4-9331-6e8eEXAMPLE)"));
    }

    private void setUpUnsignedDocumentInBucket() {
        final var request =
                CreateBucketRequest.builder()
                        .bucket(BUCKET_NAME)
                        .build();
        s3Client.createBucket(request);
        final var request2 = PutObjectRequest.builder()
                .bucket(BUCKET_NAME)
                .key(UNSIGNED_DOCUMENT_NAME)
                .contentType(MediaType.APPLICATION_PDF.toString())
                .build();
        s3Client.putObject(request2, Path.of(UNSIGNED_DOCUMENT_NAME));
    }

    private SignPdfResponseDTO getResponseDTO(final ResultActions resultActions)
            throws JsonProcessingException, UnsupportedEncodingException {
        final MvcResult result = resultActions.andReturn();
        final MockHttpServletResponse response = result.getResponse();
        final String contentAsString = response.getContentAsString();
        return mapper.readValue(contentAsString, SignPdfResponseDTO.class);
    }

}
