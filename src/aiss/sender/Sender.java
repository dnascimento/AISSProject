package aiss.sender;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.security.Key;
import java.security.cert.X509Certificate;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;

import aiss.AissMime;
import aiss.shared.AISSUtils;
import aiss.shared.CCConnection;
import aiss.shared.ConfC;
import aiss.timestampServer.TimestampObject;

import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;


/**
 * Ler o certificado do cart‹o de cidad‹o Compressao da mensagem Cifrar com a box Associar
 * um timestamp seguro Return do texto como header+email+anexo+certificado
 */
public class Sender {
    private static final String ZIP_TEMP_FILE = "temp.zip";
    private static Key sharedSecretKey = null;

    /**
     * Main method
     */
    public static void main(String[] args) throws Exception {
        boolean sign;
        boolean encrypt;
        boolean timestamp;
        String emailInputDir;
        String outputFile;

        args = new String[] { "False", "True", "True", "thunderbox/inbox",
                "thunderbox/transferBox/email.out" };

        try {
            sign = Boolean.parseBoolean(args[0]);
            encrypt = Boolean.parseBoolean(args[1]);
            timestamp = Boolean.parseBoolean(args[2]);
            emailInputDir = args[3];
            outputFile = args[4];
        } catch (Exception e) {
            throw new Exception("Wrong parameters");
        }

        // //////////////////// ZIP FILES IF EXISTS ////////////////////////////////
        File arquivoZip = null;
        File inputDir = new File(emailInputDir);

        if (!inputDir.isDirectory() || inputDir.list().length == 0) {
            throw new Exception(
                    "Input must be a directory containing at least a 'email.txt' file");
        }
        String[] filesNamesToZip = inputDir.list();
        File filesToZip[] = new File[filesNamesToZip.length];
        int k = 0;
        for (String filename : filesNamesToZip) {
            filesToZip[k++] = new File(inputDir, filename);
        }
        arquivoZip = zipfiles(filesToZip, ZIP_TEMP_FILE);

        // ////////////////// END-ZIP ////////////////////////////////

        // Data transport object (DTO)
        AissMime mimeObject = new AissMime();

        // Read ZIP File and attach to mimo
        System.out.println("Create archive");
        mimeObject.data = AISSUtils.readFileToByteArray(arquivoZip);

        new File("ziptempfolder").delete();


        // Assinar
        if (sign) {
            System.out.println("Sign");
            mimeObject.signature = signDataUsingCC(mimeObject.data);
            mimeObject.certificate = getCCCertificate();
        }

        if (timestamp) {
            System.out.println("Timestamping");
            mimeObject.timestamp = getSecureTimeStamp(mimeObject.data);
        }

        // Cifrar com a caixa
        mimeObject.ciphered = encrypt;
        if (encrypt) {
            System.out.println("Ciphered");
            byte[] data = AISSUtils.ObjectToByteArray(mimeObject);
            mimeObject.data = cipherWithBox(data);
            mimeObject.cleanState();
        }


        // Serializar e guardar no ficheiro de saida

        // Base64 para guardar
        byte[] objBytes = AISSUtils.ObjectToByteArray(mimeObject);
        String objString = Base64.encode(objBytes);
        // TODO Escrever em ficheiro de texto objString
        FileOutputStream out = new FileOutputStream(outputFile);
        // out.wri
        // oos.writeObject(objString);
        // oos.flush();
        // oos.close();
        System.out.println("Done");
        // Clean temp fiz
        if (arquivoZip != null) {
            arquivoZip.delete();
        }
    }

    private static File zipfiles(File[] arquivos, String outputZip) throws Exception {
        new AppZip(arquivos, outputZip);
        return new File(outputZip);
    }

    private static byte[] signDataUsingCC(byte[] data) throws Exception {
        return CCConnection.SignData(data, ConfC.KEY_TYPE);
    }

    private static byte[] cipherWithBox(byte[] data) throws Exception {
        Cipher cipher = Cipher.getInstance(ConfC.AES_CIPHER_TYPE);
        IvParameterSpec ivspec = new IvParameterSpec(ConfC.IV);
        cipher.init(Cipher.ENCRYPT_MODE, loadKey(), ivspec);
        return cipher.doFinal(data);
    }


    private static Key loadKey() throws Exception {
        if (sharedSecretKey != null) {
            return sharedSecretKey;
        }

        // Open Keystore and get the key
        sharedSecretKey = AISSUtils.loadSharedSecretKey(ConfC.PROGRAM_STORE_LOCATION);
        return sharedSecretKey;
    }

    private static TimestampObject getSecureTimeStamp(byte[] hash) throws IOException,
            ClassNotFoundException {
        // Ler o return e devolver o return
        Socket socket = new Socket(ConfC.TS_SERVER_HOST, ConfC.TS_SERVER_PORT);
        ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
        ObjectInputStream in = new ObjectInputStream(socket.getInputStream());

        TimestampObject sendToTSSign = new TimestampObject(hash);
        oos.writeObject(sendToTSSign);
        // Wait server
        Object obj = in.readObject();
        TimestampObject tsObj = (TimestampObject) obj;
        socket.close();
        return tsObj;
    }

    public static X509Certificate getCCCertificate() throws Exception {
        X509Certificate certificates[] = CCConnection.getCertificate();
        switch (ConfC.KEY_TYPE) {
        case Assinatura:
            return certificates[1];

        case Autenticacao:
            return certificates[0];
        default:
            throw new Exception("Invalid Key type");
        }
    }
}
