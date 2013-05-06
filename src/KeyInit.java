import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStore.ProtectionParameter;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.Calendar;
import java.util.Date;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

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
import aiss.shared.CCConnection;
import aiss.shared.ConfC;

/**
 * Esta classe gera todas as chaves e keystore necess‡rios para crirar uma distribuicao
 * deste programa.
 * 
 * @Sender: SecretShared
 * @Receiver: SecretShared, TSPubKey
 * @TimeStampServer: TSPubKey and TSPrivKey
 */
public class KeyInit {
    static int CERTIFICATE_AVAILABLE_MONTHS = 24;

    public static void main(String args[]) throws Exception, IOException {
        // Generate secretSharedKey
        SecretKey sharedSecret = generateSecretKey();
        // Generate TS KeyPair
        KeyPair TSKeys = generateRSAKeyPair();

        // Generate TS Root Certificate
        X509Certificate cert = generateSelfSignCertificate(TSKeys.getPublic());

        initProgramKeys(sharedSecret, cert);
        initTimeStampServer(TSKeys, cert);

        // saveCCCertificatesToDisk();
    }


    public static void saveCCCertificatesToDisk() throws Exception {
        X509Certificate certificates[] = CCConnection.getCertificate();
        for (int i = 0; i < certificates.length; i++) {
            File file = new File("Certificate_" + i + ".cer");
            FileOutputStream out = new FileOutputStream(file);
            out.write(certificates[i].getEncoded());
            out.flush();
            out.close();
        }
    }



    static void initProgramKeys(SecretKey sharedSecret, X509Certificate tsCertificate) throws Exception {
        // Criar o keystore e guardar a chave
        KeyStore store = newKeyStore();
        KeyStore.SecretKeyEntry entry = new KeyStore.SecretKeyEntry(sharedSecret);
        ProtectionParameter protParam = new KeyStore.PasswordProtection(ConfC.PASSWORD);
        store.setEntry(ConfC.SHARED_SECRET, entry, protParam);
        store.setCertificateEntry(ConfC.TIMESTAMP_CERT, tsCertificate);
        saveKeyStore(store, ConfC.PROGRAM_STORE_LOCATION);
    }

    static void initTimeStampServer(KeyPair keyPair, X509Certificate tsCertificate) throws Exception {
        // Criar o keystore e guardar a chave
        KeyStore store = newKeyStore();
        X509Certificate certChain[] = new X509Certificate[] { tsCertificate };
        store.setKeyEntry(ConfC.TIMESTAMP_PRIVATE,
                          keyPair.getPrivate(),
                          ConfC.PASSWORD,
                          certChain);

        store.setCertificateEntry(ConfC.TIMESTAMP_CERT, tsCertificate);
        saveKeyStore(store, ConfC.TS_STORE);
    }

    static void saveKeyStore(KeyStore keyStore, String location) throws Exception {
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(location);
            keyStore.store(fos, ConfC.PASSWORD);
        } finally {
            if (fos != null) {
                fos.close();
            }
        }
    }



    static KeyStore newKeyStore() throws Exception {
        System.out.println("Create new Keystore");
        KeyStore keyStore = KeyStore.getInstance(ConfC.KEY_STORE_INST);
        keyStore.load(null, ConfC.PASSWORD);
        return keyStore;
    }



    public static SecretKey generateSecretKey() throws Exception {
        KeyGenerator kgen;
        kgen = KeyGenerator.getInstance("AES");
        kgen.init(256);
        SecretKey key = kgen.generateKey();
        return key;
    }

    public static KeyPair generateRSAKeyPair() throws NoSuchAlgorithmException {
        // Generate a key-pair
        KeyPairGenerator kpg = KeyPairGenerator.getInstance(ConfC.RSA_ALGORITHM);
        kpg.initialize(2048);
        KeyPair kp = kpg.generateKeyPair();
        return kp;
    }


    /**
     * Create a self-signed X.509 Certificate.
     * 
     * @param subjectName the X.509 Distinguished Name, eg "CN=Test, L=London, C=GB"
     * @param pair the KeyPair
     * @param minutes how many days from now the Certificate is valid for
     * @param algorithm the signing algorithm, eg "SHA1withRSA" or "md5WithRSA"
     */
    public static X509Certificate generateSelfSignCertificate(PublicKey publicKey) throws GeneralSecurityException,
            IOException {
        X500Name owner = new X500Name("O=aiss");
        X509CertInfo info = new X509CertInfo(); // 1 segundo = 1000 | 1 minuto = 60
        Calendar c = Calendar.getInstance();
        Date from = c.getTime();
        // Durante quantos meses e que o certificado e x valido
        c.add(Calendar.MONTH, CERTIFICATE_AVAILABLE_MONTHS);
        Date to = c.getTime();
        // Validade do certificado
        CertificateValidity interval = new CertificateValidity(from, to);

        // Nœmero serie do certificado
        BigInteger serialnumber = new BigInteger(64, new SecureRandom());

        info.set(X509CertInfo.VERSION, new CertificateVersion(CertificateVersion.V3));
        info.set(X509CertInfo.SERIAL_NUMBER, new CertificateSerialNumber(serialnumber));
        info.set(X509CertInfo.ISSUER, new CertificateIssuerName(owner));
        info.set(X509CertInfo.VALIDITY, interval);
        info.set(X509CertInfo.SUBJECT, new CertificateSubjectName(owner));
        info.set(X509CertInfo.KEY, new CertificateX509Key(publicKey));

        AlgorithmId algorithmID = new AlgorithmId(AlgorithmId.sha256WithRSAEncryption_oid);
        info.set(X509CertInfo.ALGORITHM_ID, new CertificateAlgorithmId(algorithmID));

        // Sign the cert to identify the algorithm that's used.
        X509CertImpl cert = new X509CertImpl(info);

        PrivateKey masterKey = generateRSAKeyPair().getPrivate();
        cert.sign(masterKey, ConfC.SIGN_ALGO_TS);

        // Update the algorith, and resign.
        algorithmID = (AlgorithmId) cert.get(X509CertImpl.SIG_ALG);
        info.set(CertificateAlgorithmId.NAME + "." + CertificateAlgorithmId.ALGORITHM,
                 algorithmID);
        cert = new X509CertImpl(info);

        cert.sign(masterKey, ConfC.SIGN_ALGO_TS);
        return cert;
    }




}
