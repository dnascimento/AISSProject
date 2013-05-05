package aiss.receiver;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.math.BigInteger;
import java.security.Key;
import java.security.PublicKey;
import java.security.Signature;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;

import aiss.AissMime;
import aiss.shared.AISSUtils;
import aiss.shared.CCConnection;
import aiss.shared.ConfC;
import aiss.timestampServer.TimestampObject;



public class Receiver {
    private static final int DATA = 0;
    private static final int SIGNATURE = 1;
    private static final int EMAIL = 0;
    private static final int ZIP = 1;
    private static final String CERTIFICATE_DIR = "CACertificates";
    private static final boolean[] AUTH_CERT_KEY_USAGE = { true, false, false, false,
            true, false, false, false, false };
    private static final boolean[] SIGN_CERT_KEY_USAGE = { false, true, false, false,
            false, false, false, false, false };

    private static List<X509Certificate> caCertificateList = new ArrayList<X509Certificate>();
    private static BigInteger SIGN_CERT_SN = new BigInteger("7196419480743688086");
    private static BigInteger AUTH_CERT_SN = new BigInteger("7176353201892883087");
    private static Key sharedSecretKey;
    private static X509Certificate tsServerCert;

    private CCConnection provider;

    /**
     * 1¼ Validar a assinatura temporal 2¼ Decifrar com a caixa 3¼ Validar a assinatura
     * 
     * @param: signed cipher timestamp mailFile outputEmailText <outputZipDirectory>
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        String inputMailObject;
        // Directorio onde vai guardar o email.txt, o directorio zip de anexos e o txt
        // o resultado das validacoes
        String outDirectoryPath;
        try {
            inputMailObject = args[0];
            outDirectoryPath = args[1];
        } catch (Exception e) {
            throw new Exception("Wrong parameters");
        }

        // Ler o objecto
        ObjectInputStream ois = new ObjectInputStream(
                new FileInputStream(inputMailObject));
        AissMime mimeObj = (AissMime) ois.readObject();
        ois.close();


        // Checktimestamp sign
        if (mimeObj.timestamp != null) {
            Date timestampDate = checkTimeStampSignature(mimeObj.rawdata,
                                                         mimeObj.timestamp);
            System.out.println("Timestamp Sign: " + timestampDate);
        }



        byte[] data;
        // Decifrar os dados
        if (mimeObj.ciphered) {
            System.out.println("Decipher");
            data = decipherAES(mimeObj.rawdata);
        } else {
            data = mimeObj.rawdata;
        }


        // Sacar a assinatura
        if (mimeObj.dataSignLengh != 0) {
            System.out.println("Checktimestamp");
            int signatureBegin = mimeObj.rawdata.length - mimeObj.dataSignLengh;

            byte[][] result = AISSUtils.sliptByteArray(mimeObj.rawdata, signatureBegin);

            if (mimeObj.certificate == null) {
                throw new Exception("Mail without certificate");
            }
            boolean isSigned = checkSignature(result[DATA],
                                              result[SIGNATURE],
                                              mimeObj.certificate);
            if (isSigned) {
                System.out.println("Assinatura v‡lida");
            } else {
                System.out.println("Assinatura inv‡lida");
            }
            data = result[DATA];
        }

        File outDirectory = new File(outDirectoryPath);
        if (outDirectory.exists()) {
            throw new Exception("Output directory already exists");
        }
        outDirectory.mkdirs();

        // Split do zip e do texto de email
        File file;
        byte[][] emailAndZip = AISSUtils.sliptByteArray(data, mimeObj.emailTextLenght);
        if (emailAndZip[EMAIL].length != 0) {
            System.out.println("Get mail");
            file = new File(outDirectory, "email.txt");
            AISSUtils.byteArrayToFile(emailAndZip[EMAIL], file);

        }
        if (emailAndZip[ZIP].length != 0) {
            System.out.println("Get zip");
            file = new File(outDirectory, "data.zip");
            AISSUtils.byteArrayToFile(emailAndZip[ZIP], file);
        }


        // TODO escrever o log de resultado
        /**
         * Create 2 files and 1 directory: - email.txt -> clean email text - attachments
         * -> attachments folder - status.txt -> timestampSigned? authorSigned?
         */


    }

    public static Boolean checkSignature(
            byte[] clearText,
                byte[] signature,
                X509Certificate certificate) throws Exception {
        if (!CCCertificateValidation(certificate)) {
            return false;
        }
        Signature signatureEngine = Signature.getInstance(ConfC.SIGN_ALGO);
        signatureEngine.initVerify(certificate.getPublicKey());
        signatureEngine.update(clearText);
        boolean result = signatureEngine.verify(signature);
        return result;
    }


    public static byte[] decipherAES(byte[] data) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        byte[] iv = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
        IvParameterSpec ivspec = new IvParameterSpec(iv);
        cipher.init(Cipher.DECRYPT_MODE, loadKey(), ivspec);
        return cipher.doFinal(data);
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
        Signature sig = Signature.getInstance(ConfC.SIGN_ALGO);
        sig.initVerify(tsServerCert);
        byte[] struct = AISSUtils.ObjectToByteArray(signObj);
        sig.update(struct);

        boolean isSigned = sig.verify(signature);
        if (!isSigned) {
            throw new Exception("Invalid Signature: Signature is not valid");
        }
        Date stamp = new Date(signObj.timestamp);
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

            if (cert.getSerialNumber() == new BigInteger("7176353201892883087")) {
                // Auth Key
            }
            if (cert.getSerialNumber() == new BigInteger("7196419480743688086")) {
                // Sign Key
            }
        }
    }

    private static Boolean CCCertificateValidation(X509Certificate cert) throws Exception {
        PublicKey key;
        if (caCertificateList.isEmpty()) {
            loadCaCertificateList();
        }
        X509Certificate caCert = null;
        if (cert.getKeyUsage().equals(AUTH_CERT_KEY_USAGE)) {
            caCert = getCACertificate(AUTH_CERT_SN);
        }
        if (cert.getKeyUsage().equals(SIGN_CERT_KEY_USAGE)) {
            caCert = getCACertificate(SIGN_CERT_SN);
        }
        if (caCert == null) {
            throw new Exception("Invalid Key type");
        }
        try {
            key = caCert.getPublicKey();
            cert.verify(key);
        } catch (Exception e) {
            return false;
        }
        return true;
    }


    private static X509Certificate getCACertificate(BigInteger serialNumber) throws Exception {
        for (X509Certificate cert : caCertificateList) {
            if (cert.getSerialNumber().equals(serialNumber)) {
                return cert;
            }
        }
        throw new Exception("Certificate doesnt exists");
    }


}
