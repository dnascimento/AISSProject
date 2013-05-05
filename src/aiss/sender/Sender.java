package aiss.sender;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.security.cert.X509Certificate;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import aiss.AISSUtils;
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
    public static int PORT = 15678;

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
        AISSUtils.generateSecretKey("keys/secretSharedKey");
        byte[] dataBytes = new byte[1024];
        byte[] coisa = getSecureTimeStamp(dataBytes);
        System.out.println(coisa);
        try {
            sign = Boolean.parseBoolean(args[0]);
            encrypt = Boolean.parseBoolean(args[1]);
            timestamp = Boolean.parseBoolean(args[2]);
            emailTextFilename = args[3];
            outputFile = args[4];
        } catch (Exception e) {
            throw new Exception("Wrong parameters");
        }

        // //////////////////// ZIP FILES IF EXISTS ////////////////////////////////
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

        // Data transport object
        AissMime mimeObject = new AissMime();

        // Read email file and attach to DTO
        File emailTextFile = new File(emailTextFilename);
        byte[] data = AISSUtils.readFileToByteArray(emailTextFile);
        mimeObject.emailTextLenght = data.length;

        // Read ZIP File and attach to mimo
        if (arquivoZip != null) {
            System.out.println("Create archive");
            byte[] zip = AISSUtils.readFileToByteArray(arquivoZip);
            mimeObject.zipLenght = zip.length;
            data = AISSUtils.concatByteArray(data, zip);
        }


        // Assinar
        if (sign) {
            System.out.println("Sign");
            byte[] signature = signDataUsingCC(data);
            mimeObject.dataSignLengh = signature.length;
            data = AISSUtils.concatByteArray(data, signature);
            mimeObject.certificate = getCCCertificate();
        }

        // Cifrar com a caixa
        mimeObject.ciphered = encrypt;
        if (encrypt) {
            System.out.println("Ciphering...");
            data = cipherWithBox(data);
        }

        mimeObject.rawdata = data;


        if (timestamp) {
            System.out.println("Timestamping");
            mimeObject.timestampSign = getSecureTimeStamp(data);
        }
        // Serializar e guardar no ficheiro de saida
        ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(outputFile));
        oos.writeObject(mimeObject);
        oos.flush();
        oos.close();
        System.out.println("Done");
        // Clean temp fiz
        if (arquivoZip != null) {
            arquivoZip.delete();
        }
    }

    private static AppZip zipfiles(File[] arquivos, String outputZip) throws Exception {
        return new AppZip(arquivos, outputZip);
    }

    private static byte[] signDataUsingCC(byte[] data) throws Exception {
        return CCConnection.SignData(data, KEY_TYPE);
    }

    private static byte[] cipherWithBox(byte[] data) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        byte[] iv = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
        IvParameterSpec ivspec = new IvParameterSpec(iv);
        cipher.init(Cipher.ENCRYPT_MODE, loadKey(), ivspec);
        return cipher.doFinal(data);
    }


    private static byte[] getSecureTimeStamp(byte[] hash) throws IOException {
        // TODO Abrir um socket para o servidor que criaste em timeStampServer,
        // Enviar o hash
        // Ler o return e devolver o return
        Socket socket = new Socket("localhost", PORT);
        InputStream stream = socket.getInputStream();
        byte[] timestamp = new byte[1024];
        stream.read(timestamp);
        socket.close();
        return timestamp;
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



    public static SecretKeySpec loadKey() throws FileNotFoundException,
            IOException,
            ClassNotFoundException {
        File file = new File("key");
        byte[] keyBytes = AISSUtils.readFileToByteArray(file);
        SecretKeySpec keySpec = new SecretKeySpec(keyBytes, "AES");
        return keySpec;
    }





}
