package uk.gov.companieshouse.documentsigningapi.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Rule;
import org.junit.contrib.java.lang.system.EnvironmentVariables;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import uk.gov.companieshouse.documentsigningapi.dto.SignPdfRequestDTO;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@Testcontainers
@SpringBootTest
class SignDocumentControllerIntegrationTest {

    @Container
    private static final LocalStackContainer localStackContainer =
            new LocalStackContainer().withServices(LocalStackContainer.Service.S3);

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper mapper;

    @Rule
    public EnvironmentVariables environmentVariables = new EnvironmentVariables();

    @Test
    @DisplayName("signPdf returns the signed property location")
    void signPdfReturnsSignedPropertyLocation() throws Exception {

        environmentVariables.set("AWS_REGION", localStackContainer.getRegion());
        environmentVariables.set("AWS_ACCESS_KEY_ID", localStackContainer.getAccessKey());
        environmentVariables.set("AWS_SECRET_ACCESS_KEY", localStackContainer.getSecretKey());

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
