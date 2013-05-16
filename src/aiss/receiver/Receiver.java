package aiss.receiver;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.Signature;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.apache.commons.codec.binary.Base64InputStream;

import aiss.AesBox;
import aiss.AissMime;
import aiss.interf.AISSInterface;
import aiss.shared.AISSUtils;
import aiss.shared.ConfC;
import aiss.shared.Mode;
import aiss.timestampServer.TimestampObject;


/**
 * Receiver main class: Retrieves args:
 * 
 * @param Email Object input
 * @param Folder to output The email object contains all information to decipher itself.
 *            The data in email object is deciphered, sign checked, timestamp checked and
 *            then unziped into destination folder
 */
public class Receiver {
    private static final String CERTIFICATE_DIR = "CACertificates";
    private static final boolean[] AUTH_CERT_KEY_USAGE = { true, false, false, false,
            true, false, false, false, false };
    private static final boolean[] SIGN_CERT_KEY_USAGE = { false, true, false, false,
            false, false, false, false, false };
    private static final int BUFFER_SIZE = 20000;

    private static List<X509Certificate> caCertificateList = new ArrayList<X509Certificate>();
    private static String AUTH_CERT_SN = "7196419480743688086";
    private static String SIGN_CERT_SN = "7176353201892883087";
    private static Key sharedSecretKey;
    private static X509Certificate tsServerCert;

    /**
     * 1¼ Validar a assinatura temporal 2¼ Decifrar com a caixa 3¼ Validar a assinatura
     * 
     * @param: signed cipher timestamp mailFile outputEmailText <outputZipDirectory>
     * @throws Exception
     */
    public static void begin(String inputMailObject, String outMainDirectoryPath) throws Exception {

        // Ler o objecto
        File inputMailFile = new File(inputMailObject);
        Base64InputStream in = new Base64InputStream(new FileInputStream(inputMailFile));
        ObjectInputStream ois = new ObjectInputStream(in);
        AissMime mimeObj = (AissMime) ois.readObject();
        System.out.println("done");
        ois.close();

        System.out.println("Readed");


        // Decifrar os dados
        if (mimeObj.ciphered) {
            System.out.println("DECIPHER - Starting Decipher...\n");
            AISSInterface.logreceiver.append("DECIPHER- Starting decipher...\n");
            byte[] data = decipherAES(mimeObj.data);
            mimeObj = (AissMime) AISSUtils.ByteArrayToObject(data);
            System.out.println("DECIPHER - Success.");
            AISSInterface.logreceiver.append("DECIPHER - Success.\n");
        }

        System.out.println("Check timestamp....");

        byte[] hash = byteArrayHash(mimeObj.data);


        // Checktimestamp sign
        if (mimeObj.timestamp != null) {
            AISSInterface.logreceiver.append("TIMESTAMP - Starting timestamp verification...\n");
            Date timestampDate = checkTimeStampSignature(hash, mimeObj.timestamp);
            AISSInterface.logreceiver.append("TIMESTAMP - Timestamp Sign: "
                    + timestampDate + "\n");
            System.out.println("TIMESTAMP - Timestamp Sign: " + timestampDate);
        }

        System.out.println("Check signature....");


        // Sacar a assinatura
        if (mimeObj.signature != null) {
            AISSInterface.logreceiver.append("SIGN - Starting signature verification...\n");
            if (mimeObj.certificate == null) {
                throw new Exception("Mail without certificate");
            }
            boolean isSigned = checkSignature(hash,
                                              mimeObj.signature,
                                              mimeObj.certificate);
            if (isSigned) {
                System.out.println("Assinatura v‡lida");
                AISSInterface.logreceiver.append("SIGN - Signature is valid.\n");
                System.out.println("This email is sign by: "
                        + extractCertificateOwner(mimeObj.certificate));
                AISSInterface.logreceiver.append("SIGN - This email is signed by "
                        + extractCertificateOwner(mimeObj.certificate) + "\n");
            } else {
                System.out.println("Assinatura inv‡lida");
                AISSInterface.logreceiver.append("SIGN - ATTENTION: Signature is NOT valid.\n");
            }
        }

        System.out.println("Done....");

        File outMainDirectory = new File(outMainDirectoryPath);
        if (!outMainDirectory.exists()) {
            throw new Exception("Output directory doesnt exists");
        }

        // File outDirectory = generateNewDirectory(outMainDirectory);
        System.out.println("generate directory");
        UnZip unZip = new UnZip();
        unZip.unZipIt(mimeObj.data, outMainDirectory);

        // Unzip do conteudo para dentro da pasta

        // inputMailFile.delete();
        System.out.println("Receiver done (all work)");
        // TODO escrever o log de resultado
        /**
         * Create 2 files and 1 directory: - email.txt -> clean email text - attachments
         * -> attachments folder - status.txt -> timestampSigned? authorSigned?
         */
    }



    private static byte[] byteArrayHash(byte[] data) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        digest.update(data);
        byte[] hash = digest.digest();
        return hash;
    }



    /**
     * Check Portuguse Citzan Card digital signature
     * 
     * @param clearText: original text
     * @param signature: signature
     * @param certificate: Citzen Card certificate
     * @return: signed or not
     * @throws Exception
     */
    public static Boolean checkSignature(
            byte[] clearText,
                byte[] signature,
                X509Certificate certificate) throws Exception {
        if (!CCCertificateValidation(certificate)) {
            return false;
        }
        Signature signatureEngine = Signature.getInstance(ConfC.SIGN_ALGO_CC);
        signatureEngine.initVerify(certificate.getPublicKey());
        signatureEngine.update(clearText);
        boolean result = signatureEngine.verify(signature);
        return result;
    }


    public static String extractCertificateOwner(X509Certificate cert) {
        return cert.getSubjectDN().getName();
    }


    public static byte[] decipherAES(byte[] data) throws Exception {
        AesBox box = new AesBox();
        box.init(Mode.Decipher);
        return box.doIt(data);
        // Cipher cipher = Cipher.getInstance(ConfC.AES_CIPHER_TYPE);
        // byte[] iv = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
        // IvParameterSpec ivspec = new IvParameterSpec(iv);
        // cipher.init(Cipher.DECRYPT_MODE, loadKey(), ivspec);
        // return cipher.doFinal(data);
    }

    /*
     * Devolve a data em que foi assinado. Se nao for valido, devolve null
     */
    public static Date checkTimeStampSignature(byte[] dataHash, TimestampObject signObj) throws Exception {
        // Load da keystore do certificado
        if (tsServerCert == null) {
            X509Certificate cert = AISSUtils.loadTimestampCertificate(ConfC.PROGRAM_STORE_LOCATION);
            tsServerCert = cert;
        }
        // Extrair a assinatura
        byte[] signature = signObj.extractSignature();

        // Verificar se o hash e o mesmo
        if (!Arrays.equals(dataHash, signObj.dataHash)) {
            throw new Exception("Invalid Signature: Original data changed");
        }

        // Fazer a verificacao de assinatura
        Signature sig = Signature.getInstance(ConfC.SIGN_ALGO_TS);
        sig.initVerify(tsServerCert);
        byte[] struct = AISSUtils.ObjectToByteArray(signObj);
        sig.update(struct);

        boolean isSigned = sig.verify(signature);
        if (!isSigned) {
            throw new Exception("Invalid Signature: Signature is not valid");
        }
        Date stamp = new Date(signObj.timestamp);
        System.out.println("Timestamp signature is valid");
        return stamp;
    }

    private static Key loadKey() throws Exception {
        if (sharedSecretKey != null) {
            return sharedSecretKey;
        }
        // Open Keystore and get the key
        sharedSecretKey = AISSUtils.loadSharedSecretKey(ConfC.PROGRAM_STORE_LOCATION);
        return sharedSecretKey;
    }




    private static void loadCaCertificateList() throws Exception {
        File dir = new File(CERTIFICATE_DIR);
        File[] certFiles = dir.listFiles();
        for (int i = 0; i < certFiles.length; i++) {
            byte[] certByteArray = AISSUtils.readFileToByteArray(certFiles[i]);
            ByteArrayInputStream in = new ByteArrayInputStream(certByteArray);
            CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
            X509Certificate cert = (X509Certificate) certFactory.generateCertificate(in);
            caCertificateList.add(cert);

            if (cert.getSerialNumber().toString().equals(AUTH_CERT_SN)) {
                System.out.println("Auth CA Key Loaded");
            }
            if (cert.getSerialNumber().toString().equals(SIGN_CERT_SN)) {
                System.out.println("Sign CA Key Loaded");
            }
        }
    }

    private static Boolean CCCertificateValidation(X509Certificate cert) throws Exception {
        PublicKey key;
        if (caCertificateList.isEmpty()) {
            loadCaCertificateList();
        }
        X509Certificate caCert = null;
        // if (cert.getKeyUsage().equals(AUTH_CERT_KEY_USAGE)) {
        caCert = getCACertificate(AUTH_CERT_SN);
        // }
        // if (cert.getKeyUsage().equals(SIGN_CERT_KEY_USAGE)) {
        // caCert = getCACertificate(SIGN_CERT_SN);
        // }
        if (caCert == null) {
            throw new Exception(
                    "Invalid Certificate type: Only accepts Authentication cert");
        }
        try {
            key = caCert.getPublicKey();
            cert.verify(key);
        } catch (Exception e) {
            return false;
        }
        return true;
    }


    private static X509Certificate getCACertificate(String serialNumber) throws Exception {
        for (X509Certificate cert : caCertificateList) {
            if (cert.getSerialNumber().toString().equals(serialNumber)) {
                return cert;
            }
        }
        throw new Exception("Certificate doesnt exists");
    }

    private static File generateNewDirectory(File parent) {
        String[] childDirsName = parent.list();
        int i = childDirsName.length;
        while (true) {
            boolean exits = false;
            String newName = "out" + i++;
            for (String filename : childDirsName) {
                if (newName.equals(filename)) {
                    exits = true;
                    break;
                }
            }
            if (!exits) {
                File out = new File(parent, newName);
                out.mkdir();
                return out;
            }
        }
    }


}
