package aiss.receiver;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileReader;
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
import aiss.shared.ConfC;
import aiss.timestampServer.TimestampObject;

import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;



public class Receiver {
    private static final String CERTIFICATE_DIR = "CACertificates";
    private static final boolean[] AUTH_CERT_KEY_USAGE = { true, false, false, false,
            true, false, false, false, false };
    private static final boolean[] SIGN_CERT_KEY_USAGE = { false, true, false, false,
            false, false, false, false, false };

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
    public static void main(String[] args) throws Exception {
        args = new String[] { "thunderbox/transferBox/email.out", "thunderbox/outbox/" };

        String inputMailObject;
        String outMainDirectoryPath;
        try {
            inputMailObject = args[0];
            outMainDirectoryPath = args[1];
        } catch (Exception e) {
            throw new Exception("Wrong parameters");
        }

        // Ler o objecto
        File inputMailFile = new File(inputMailObject);
        BufferedReader in = new BufferedReader(new FileReader(inputMailFile));
        StringBuilder sb = new StringBuilder();
        String line = in.readLine();

        while (line != null) {
            sb.append(line);
            sb.append("\n");
            line = in.readLine();
        }
        String contentread = sb.toString();
        in.close();
        byte[] content = Base64.decode(contentread);
        AissMime mimeObj = (AissMime) AISSUtils.ByteArrayToObject(content);


        // Decifrar os dados
        if (mimeObj.ciphered) {
            System.out.println("Decipher");
            byte[] data = decipherAES(mimeObj.data);
            mimeObj = (AissMime) AISSUtils.ByteArrayToObject(data);
        }



        // Checktimestamp sign
        if (mimeObj.timestamp != null) {
            Date timestampDate = checkTimeStampSignature(mimeObj.data, mimeObj.timestamp);
            System.out.println("Timestamp Sign: " + timestampDate);
        }



        // Sacar a assinatura
        if (mimeObj.signature != null) {
            System.out.println("Checktimestamp");

            if (mimeObj.certificate == null) {
                throw new Exception("Mail without certificate");
            }
            boolean isSigned = checkSignature(mimeObj.data,
                                              mimeObj.signature,
                                              mimeObj.certificate);
            if (isSigned) {
                System.out.println("Assinatura v‡lida");
            } else {
                System.out.println("Assinatura inv‡lida");
                return;
            }
        }

        File outMainDirectory = new File(outMainDirectoryPath);
        if (!outMainDirectory.exists()) {
            throw new Exception("Output directory doesnt exists");
        }

        File outDirectory = generateNewDirectory(outMainDirectory);
        UnZip unZip = new UnZip();
        unZip.unZipIt(mimeObj.data, outDirectory);

        // Unzip do conteudo para dentro da pasta

        inputMailFile.delete();

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
        Signature signatureEngine = Signature.getInstance(ConfC.SIGN_ALGO_CC);
        signatureEngine.initVerify(certificate.getPublicKey());
        signatureEngine.update(clearText);
        boolean result = signatureEngine.verify(signature);
        return result;
    }


    public static byte[] decipherAES(byte[] data) throws Exception {
        Cipher cipher = Cipher.getInstance(ConfC.AES_CIPHER_TYPE);
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
            String newName = "out" + i;
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
