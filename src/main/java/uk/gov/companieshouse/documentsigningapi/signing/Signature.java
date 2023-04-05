package uk.gov.companieshouse.documentsigningapi.signing;

import org.apache.pdfbox.pdmodel.interactive.digitalsignature.SignatureInterface;
import org.bouncycastle.cert.jcajce.JcaCertStore;
import org.bouncycastle.cms.CMSException;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.cms.CMSSignedDataGenerator;
import org.bouncycastle.cms.jcajce.JcaSignerInfoGeneratorBuilder;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.operator.jcajce.JcaDigestCalculatorProviderBuilder;
import uk.gov.companieshouse.documentsigningapi.exception.SigningException;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Optional;

public class Signature implements SignatureInterface {

    private static final String SIGNATURE_ALGORITHM = "SHA256WithRSA";

    private Certificate[] certificateChain;
    private PrivateKey privateKey;

    public Signature(KeyStore keyStore, char[] keyStorePassword, String appCertificateAlias)
        throws KeyStoreException, UnrecoverableKeyException, NoSuchAlgorithmException, IOException,
        CertificateNotYetValidException, CertificateExpiredException {

        this.certificateChain = Optional.ofNullable(keyStore.getCertificateChain(appCertificateAlias))
            .orElseThrow(() -> (new IOException("Could not find a proper certificate chain")));

        this.privateKey = (PrivateKey) keyStore.getKey(appCertificateAlias, keyStorePassword);

        var certificate = this.certificateChain[0];

        if (certificate instanceof X509Certificate) {
            ((X509Certificate) certificate).checkValidity();
        }
    }

    @Override
    public byte[] sign(InputStream inputStream) throws IOException {
        try {
        var gen = new CMSSignedDataGenerator();
        X509Certificate cert = (X509Certificate) this.certificateChain[0];
        ContentSigner sha1Signer = new JcaContentSignerBuilder(SIGNATURE_ALGORITHM).build(this.privateKey);

        gen.addSignerInfoGenerator(new JcaSignerInfoGeneratorBuilder(new JcaDigestCalculatorProviderBuilder().build()).build(sha1Signer, cert));
        gen.addCertificates(new JcaCertStore(Arrays.asList(this.certificateChain)));

        var msg = new CMSProcessableInputStream(inputStream);
        CMSSignedData signedData = gen.generate(msg, false);

        return signedData.getEncoded();
        } catch (OperatorCreationException | CertificateEncodingException | CMSException e) {
            throw new SigningException("Unable to sign certificate", e);
        }
    }

    public Certificate[] getCertificateChain() {
        return certificateChain;
    }
}
