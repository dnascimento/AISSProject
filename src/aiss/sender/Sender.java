package aiss.sender;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

import aiss.AissMime;
import aiss.shared.AppZip;
import aiss.shared.CCConnection;
import aiss.shared.KeyType;

/**
 * Ler o certificado do cart‹o de cidad‹o Compressao da mensagem Cifrar com a box Associar
 * um timestamp seguro Return do texto como header+email+anexo+certificado
 */
public class Sender {
    private static final String ZIP_TEMP_FILE = "temp.zip";
    private static final int BUFFER_SIZE = 1024;
    // Define the CC certificate
    static KeyType KEY_TYPE = KeyType.Autenticacao;

    /**
     * Main method
     * 
     * @param signed cipher timestamp fileEmailText outputFile attachment1 attachment2...
     *            ex: false true false c://dario.txt c://goncalo.carito.img c://out.zip
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        boolean sign;
        boolean encrypt;
        boolean timestamp;
        String emailTextFilename;
        String outputFile;
        try {
            sign = Boolean.parseBoolean(args[0]);
            encrypt = Boolean.parseBoolean(args[1]);
            timestamp = Boolean.parseBoolean(args[2]);
            emailTextFilename = args[3];
            outputFile = args[4];
        } catch (Exception e) {
            throw new Exception("Wrong parameters");
        }

        // //////////////////// ZIP ////////////////////////////////
        File arquivoZip = null;
        if (args.length > 5) {
            File filesToZip[] = new File[args.length - 5];
            for (int i = 5; i < args.length; i++) {
                filesToZip[i - 5] = new File(args[i]);
            }
            zipfiles(filesToZip, ZIP_TEMP_FILE);
            arquivoZip = new File(ZIP_TEMP_FILE);
        }
        // //////////////////// END-ZIP ////////////////////////////////

        AissMime mimeObject = new AissMime();

        File emailTextFile = new File(emailTextFilename);
        byte[] data = readFileToByteArray(emailTextFile);
        mimeObject.emailTextLenght = data.length;

        if (arquivoZip != null) {
            byte[] zip = readFileToByteArray(arquivoZip);
            mimeObject.zipLenght = zip.length;
            data = concatByteArray(data, zip);
        }


        // Assinar
        if (sign) {
            byte[] signature = signDataUsingCC(data);
            mimeObject.dataSignLengh = signature.length;
            data = concatByteArray(data, signature);
            mimeObject.certificate = getCCCertificate();
        }

        // Cifrar com a caixa
        mimeObject.ciphered = encrypt;
        if (encrypt) {
            data = cipherWithBox(data);
        }

        mimeObject.rawdata = data;

        if (timestamp) {
            mimeObject.timestampSign = getSecureTimeStamp(data);
        }

        // Serializar e guardar no ficheiro de saida
        ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(outputFile));
        oos.writeObject(mimeObject);
        oos.flush();
        oos.close();
    }

    private static void zipfiles(File[] arquivos, String outputZip) throws Exception {
        AppZip zipzip = new AppZip(arquivos, outputZip);
    }

    private static byte[] signDataUsingCC(byte[] data) throws Exception {
        return CCConnection.SignData(data, KEY_TYPE);
    }

    private static byte[] cipherWithBox(byte[] data) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, loadKey());
        return cipher.doFinal(data);
    }


    private static byte[] getSecureTimeStamp(byte[] hash) {
        // TODO
        // http: // www.itconsult.co.uk/stamper/stampinf.htm
        return null;
    }

    public static X509Certificate getCCCertificate() throws Exception {
        X509Certificate certificates[] = CCConnection.getCertificate();
        switch (KEY_TYPE) {
        case Assinatura:
            return certificates[1];

        case Autenticacao:
            return certificates[0];
        default:
            throw new Exception("Invalid Key type");
        }
    }

    private static byte[] readFileToByteArray(File emailTextFile) throws IOException {
        FileInputStream in = new FileInputStream(emailTextFile);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buf = new byte[BUFFER_SIZE];
        int bytesRead = in.read(buf);
        while (bytesRead != -1) {
            baos.write(buf, 0, bytesRead);
            bytesRead = in.read(buf);
        }
        baos.flush();
        return baos.toByteArray();
    }


    private static Key loadKey() throws FileNotFoundException,
            IOException,
            ClassNotFoundException {
        ObjectInputStream in = new ObjectInputStream(new FileInputStream("key"));
        SecretKey key = (SecretKey) in.readObject();
        in.close();
        return key;
    }


    public static byte[] concatByteArray(byte[] a, byte[] b) {
        byte[] result = new byte[a.length + b.length];
        System.arraycopy(a, 0, result, 0, a.length);
        System.arraycopy(b, 0, result, a.length, b.length);
        return result;
    }


    @SuppressWarnings("unused")
    private static Key generateSecretKey() throws FileNotFoundException, IOException {
        KeyGenerator kgen;
        try {
            kgen = KeyGenerator.getInstance("AES");
            kgen.init(128);
            SecretKey key = kgen.generateKey();
            ObjectOutputStream os = new ObjectOutputStream(new FileOutputStream("key"));
            os.writeObject(key);
            os.flush();
            os.close();
            return key;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }
}
