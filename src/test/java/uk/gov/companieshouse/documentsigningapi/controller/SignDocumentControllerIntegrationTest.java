package uk.gov.companieshouse.documentsigningapi.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Rule;
import org.junit.contrib.java.lang.system.EnvironmentVariables;
import org.junit.jupiter.api.AfterAll;
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
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import uk.gov.companieshouse.documentsigningapi.dto.SignPdfRequestDTO;
import uk.gov.companieshouse.documentsigningapi.dto.SignPdfResponseDTO;
import uk.gov.companieshouse.documentsigningapi.environment.EnvironmentVariablesChecker;

import java.io.UnsupportedEncodingException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import static java.util.Arrays.stream;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@Testcontainers
@SpringBootTest
class SignDocumentControllerIntegrationTest {

    private static final String LOCALSTACK_IMAGE_NAME = "localstack/localstack:1.4";
    private static final String UNSIGNED_BUCKET_NAME = "document-api-images-cidev";
    private static final String SIGNED_BUCKET_NAME = "document-signing-api";
    private static final String UNSIGNED_DOCUMENT_NAME = "9616659670.pdf";
    private static final String UNKNOWN_UNSIGNED_DOCUMENT_NAME = "UNKNOWN.pdf";
    private static final String CERTIFIED_COPY_DOCUMENT_TYPE = "certified-copy";
    private static final List<String> SIGNATURE_OPTIONS = List.of("cover-sheet");
    public static final String SIGNED_DOC_STORAGE_PREFIX = "cidev";
    public static final String FOLDER_NAME = "certified-copy-folder";
    public static final String SIGNED_DOCUMENT_FILENAME = "CCD-123456-123456.pdf";

    @Container
    private static final LocalStackContainer localStackContainer =
            new LocalStackContainer(DockerImageName.parse(LOCALSTACK_IMAGE_NAME))
                    .withServices(LocalStackContainer.Service.S3);

    private static final String TOKEN_VALUE = "token value";

    @Rule
    private static final EnvironmentVariables ENVIRONMENT_VARIABLES;

    static {
        ENVIRONMENT_VARIABLES = new EnvironmentVariables();
        stream(EnvironmentVariablesChecker.RequiredEnvironmentVariables.values()).forEach(variable -> {
            switch (variable) {
                case AWS_REGION:
                    ENVIRONMENT_VARIABLES.set(variable.getName(), "eu-west-2");
                    break;
                case SIGNED_DOC_BUCKET_NAME:
                    ENVIRONMENT_VARIABLES.set(variable.getName(), SIGNED_BUCKET_NAME);
                    break;
                default:
                    ENVIRONMENT_VARIABLES.set(variable.getName(), TOKEN_VALUE);
                    break;
            }
        });
    }

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
        setUpSignedDocumentBucket();
    }

    @AfterAll
    static void tearDown() {
        final String[] AllEnvironmentVariableNames =
                Arrays.stream(EnvironmentVariablesChecker.RequiredEnvironmentVariables.class.getEnumConstants())
                        .map(Enum::name)
                        .toArray(String[]::new);
        ENVIRONMENT_VARIABLES.clear(AllEnvironmentVariableNames);
    }

    @Test
    @DisplayName("signPdf returns the signed document location and stores signed document there")
    void signPdfReturnsSignedDocumentLocation() throws Exception {

        // It seems that LocalStack S3 is somewhat region-agnostic.
        final var unsignedDocumentLocation =
                "https://" + UNSIGNED_BUCKET_NAME + ".s3.eu-west-2.amazonaws.com/" + UNSIGNED_DOCUMENT_NAME;
        final var signPdfRequestDTO = createSignPdfRequest(unsignedDocumentLocation);

        final var resultActions = mockMvc.perform(post("/document-signing/sign-pdf")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(signPdfRequestDTO)))
                .andExpect(status().isCreated());
        
        final var signPdfResponseDTO = getResponseDTO(resultActions);

        // Note the following is an assertion about the resultant location when using LocalStack S3.
        // This location differs from that with the real S3 service. For example:
        // 1. LocalStack: http://127.0.0.1:55720/document-signing-api/cidev/certified-copy-folder/CCD-123456-123456.pdf
        // 2. S3: https://document-signing-api.s3.eu-west-2.amazonaws.com/docker/certified-copy/CCD-123456-123456.pdf
        assertThat(signPdfResponseDTO.getSignedDocumentLocation(),
                containsString("/" + SIGNED_BUCKET_NAME +
                                        "/" + SIGNED_DOC_STORAGE_PREFIX +
                                        "/" + FOLDER_NAME +
                                        "/" + SIGNED_DOCUMENT_FILENAME));

        verifySignedDocStoredInExpectedLocation(SIGNED_BUCKET_NAME,
                                                SIGNED_DOC_STORAGE_PREFIX,
                                                FOLDER_NAME,
                                                SIGNED_DOCUMENT_FILENAME);
    }

    @Test
    @DisplayName("signPdf with invalid unsigned document location responds with bad request")
    void signPdfWithInvalidDocumentLocation() throws Exception {

        // It seems that LocalStack S3 is somewhat region-agnostic.
        final var unsignedDocumentLocation =
                "https:// " + UNSIGNED_BUCKET_NAME + ".s3.eu-west-2.amazonaws.com/" + UNSIGNED_DOCUMENT_NAME;
        final var signPdfRequestDTO = createSignPdfRequest(unsignedDocumentLocation);

        final var resultActions = mockMvc.perform(post("/document-signing/sign-pdf")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(signPdfRequestDTO)))
                .andExpect(status().isBadRequest());

        final var body = resultActions.andReturn().getResponse().getContentAsString();
        assertThat(body, is("Illegal character in authority at index 8: " +
                "https:// document-api-images-cidev.s3.eu-west-2.amazonaws.com/9616659670.pdf"));
    }

    @Test
    @DisplayName("signPdf with incorrect unsigned document location responds with not found")
    void signPdfWithUnknownDocumentLocation() throws Exception {

        // It seems that LocalStack S3 is somewhat region-agnostic.
        final var unsignedDocumentLocation =
                "https://" + UNSIGNED_BUCKET_NAME + ".s3.eu-west-2.amazonaws.com/" + UNKNOWN_UNSIGNED_DOCUMENT_NAME;
        final var signPdfRequestDTO = createSignPdfRequest(unsignedDocumentLocation);

        final var resultActions = mockMvc.perform(post("/document-signing/sign-pdf")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(signPdfRequestDTO)))
                .andExpect(status().isNotFound());

        final var body = resultActions.andReturn().getResponse().getContentAsString();
        assertThat(body, startsWith("The specified key does not exist. " +
                "(Service: S3, Status Code: 404, Request ID: 7a62c49f-347e-4fc4-9331-6e8eEXAMPLE, " +
                "Extended Request ID: "));
    }

    private void setUpUnsignedDocumentInBucket() {
        final var request =
                CreateBucketRequest.builder()
                        .bucket(UNSIGNED_BUCKET_NAME)
                        .build();
        s3Client.createBucket(request);
        final var request2 = PutObjectRequest.builder()
                .bucket(UNSIGNED_BUCKET_NAME)
                .key(UNSIGNED_DOCUMENT_NAME)
                .contentType(MediaType.APPLICATION_PDF.toString())
                .build();
        s3Client.putObject(request2, Path.of("src/test/resources/" + UNSIGNED_DOCUMENT_NAME));
    }

    private void setUpSignedDocumentBucket() {
        final var request =
                CreateBucketRequest.builder()
                        .bucket(SIGNED_BUCKET_NAME)
                        .build();
        s3Client.createBucket(request);
    }

    private SignPdfResponseDTO getResponseDTO(final ResultActions resultActions)
            throws JsonProcessingException, UnsupportedEncodingException {
        final var result = resultActions.andReturn();
        final var response = result.getResponse();
        final var contentAsString = response.getContentAsString();
        return mapper.readValue(contentAsString, SignPdfResponseDTO.class);
    }

    private void verifySignedDocStoredInExpectedLocation(final String bucketName,
                                                         final String signedDocStoragePrefix,
                                                         final String folderName,
                                                         final String filename) {
        final var getObjectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(signedDocStoragePrefix + "/" + folderName + "/" + filename)
                .build();
        final var s3Response = s3Client.getObject(getObjectRequest);
        final var isOk = s3Response.response().sdkHttpResponse().isSuccessful();
        assertThat(isOk, is(true));
    }

    private SignPdfRequestDTO createSignPdfRequest(final String unsignedDocumentLocation) {
        final var signPdfRequestDTO = new SignPdfRequestDTO();
        signPdfRequestDTO.setDocumentLocation(unsignedDocumentLocation);
        signPdfRequestDTO.setDocumentType(CERTIFIED_COPY_DOCUMENT_TYPE);
        signPdfRequestDTO.setSignatureOptions(SIGNATURE_OPTIONS);
        signPdfRequestDTO.setFolderName(SIGNED_DOC_STORAGE_PREFIX + "/" + FOLDER_NAME);
        signPdfRequestDTO.setFilename(SIGNED_DOCUMENT_FILENAME);
        return signPdfRequestDTO;
    }

}
