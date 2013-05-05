package timestampServer;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Date;

import sun.security.x509.AlgorithmId;
import sun.security.x509.CertificateAlgorithmId;
import sun.security.x509.CertificateIssuerName;
import sun.security.x509.CertificateSerialNumber;
import sun.security.x509.CertificateSubjectName;
import sun.security.x509.CertificateValidity;
import sun.security.x509.CertificateVersion;
import sun.security.x509.CertificateX509Key;
import sun.security.x509.X500Name;
import sun.security.x509.X509CertImpl;
import sun.security.x509.X509CertInfo;

/**
 * Recebe os dados do utilizador e assina um timestamp com os dados recebidos e com o seu
 * certificado
 */
public class TimeStampServer {

    public static int LISTEN_PORT = 5678;
    X509Certificate certificate = null;
    int validityDays = 712;

    // C, ST, L, O, OU, CN, EMAIL.
    String DOMANIN_NAME = "cn=ca";


    public static void main(String[] args) throws Exception {
        String keystoreFile = "keyStoreFile.bin";
        String caAlias = "caAlias";
        String certToSignAlias = "cert";
        String newAlias = "newAlias";

        char[] password = new char[] { 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h' };
        char[] caPassword = new char[] { 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h' };
        char[] certPassword = new char[] { 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h' };


        KeyPair pair = gene
        PrivateKey caPrivateKey = 
        java.security.cert.Certificate caCert = keyStore.getCertificate(caAlias);

        byte[] encoded = caCert.getEncoded();
        X509CertImpl caCertImpl = new X509CertImpl(encoded);

        X509CertInfo caCertInfo = (X509CertInfo) caCertImpl.get(X509CertImpl.NAME + "."
                + X509CertImpl.INFO);

        X500Name issuer = (X500Name) caCertInfo.get(X509CertInfo.SUBJECT + "."
                + CertificateIssuerName.DN_NAME);

        java.security.cert.Certificate cert = keyStore.getCertificate(certToSignAlias);
        PrivateKey privateKey = (PrivateKey) keyStore.getKey(certToSignAlias,
                                                             certPassword);
        encoded = cert.getEncoded();
        X509CertImpl certImpl = new X509CertImpl(encoded);
        X509CertInfo certInfo = (X509CertInfo) certImpl.get(X509CertImpl.NAME + "."
                + X509CertImpl.INFO);

        Date firstDate = new Date();
        Date lastDate = new Date(firstDate.getTime() + 365 * 24 * 60 * 60 * 1000L);
        CertificateValidity interval = new CertificateValidity(firstDate, lastDate);

        certInfo.set(X509CertInfo.VALIDITY, interval);

        certInfo.set(X509CertInfo.SERIAL_NUMBER, new CertificateSerialNumber(
                (int) (firstDate.getTime() / 1000)));

        certInfo.set(X509CertInfo.ISSUER + "." + CertificateSubjectName.DN_NAME, issuer);

        AlgorithmId algorithm = new AlgorithmId(AlgorithmId.md5WithRSAEncryption_oid);
        certInfo.set(CertificateAlgorithmId.NAME + "." + CertificateAlgorithmId.ALGORITHM,
                     algorithm);
        X509CertImpl newCert = new X509CertImpl(certInfo);

        newCert.sign(caPrivateKey, "MD5WithRSA");

        keyStore.setKeyEntry(newAlias,
                             privateKey,
                             certPassword,
                             new java.security.cert.Certificate[] { newCert });

        FileOutputStream output = new FileOutputStream(keystoreFile);
        keyStore.store(output, password);
        output.close();

    }


    // Gerar um certificado aleat—rio ˆ m‹o, gravar num ficheiro. Quando o servidor
    // arranca, ler o certificado
    public TimeStampServer() throws Exception {
        // TODO Read do certificado em disco this.certificate = read
        KeyPair pair = generateKeyPair();
        X509Certificate cert = generateCertificate(pair);
    }




    public KeyPair generateKeyPair() throws NoSuchAlgorithmException {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        SecureRandom sr = new SecureRandom();
        keyGen.initialize(1024, sr);
        KeyPair keypair = keyGen.generateKeyPair();
        return keypair;
    }




    public X509Certificate generateCertificate(KeyPair keypair) throws IOException,
            CertificateException {
        X509CertInfo info = new X509CertInfo();
        Date from = new Date();
        Date to = new Date(from.getTime() + validityDays * 86400000l);
        CertificateValidity interval = new CertificateValidity(from, to);
        BigInteger sn = new BigInteger(64, new SecureRandom());
        X500Name owner = new X500Name(DOMANIN_NAME);

        info.set(X509CertInfo.VALIDITY, interval);
        info.set(X509CertInfo.SERIAL_NUMBER, new CertificateSerialNumber(sn));
        info.set(X509CertInfo.SUBJECT, new CertificateIssuerName(owner));
        info.set(X509CertInfo.KEY, new CertificateX509Key(keypair.getPublic()));
        info.set(X509CertInfo.VERSION, new CertificateVersion(CertificateVersion.V3));
        AlgorithmId algo = new AlgorithmId(AlgorithmId.sha256WithRSAEncryption_oid);
        info.set(X509CertInfo.ALGORITHM_ID, new CertificateAlgorithmId(algo));

        X509CertImpl cert = new X509CertImpl(info);
        // Sign the certificate (dont need, this is a root CERT)
        // cert.sign(privkey, algorithm);
        return cert;
    }


    public void start() throws IOException {
        ServerSocket serverSocket = new ServerSocket(LISTEN_PORT);
        while (true) {
            Socket socket = serverSocket.accept();
            TimestampClient client = new TimestampClient(socket, certificate);
            client.start();
        }
    }
}
