package uk.gov.companieshouse.documentsigningapi.signing;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class SigningService {

    private final String keystoreType;
    private final String keystorePath;
    private final String keystorePassword;
    private final String certificateAlias;

    public SigningService(@Value("${environment.keystore.type}") String keystoreType,
                          @Value("${environment.keystore.path}") String keystorePath,
                          @Value("${environment.keystore.password}") String keystorePassword,
                          @Value("${environment.certificate.alias}") String certificateAlias) {
        this.keystoreType = keystoreType;
        this.keystorePath = keystorePath;
        this.keystorePassword = keystorePassword;
        this.certificateAlias = certificateAlias;
    }

    public void signPDF() {
        System.out.println("KEYSTORE TYPE: " + keystoreType);
        System.out.println("KEYSTORE PATH: " + keystorePath);
        System.out.println("KEYSTORE PASSWORD: " + keystorePassword);
        System.out.println("CERTIFICATE ALIAS:" + certificateAlias);
    }

}
