package aiss.sender;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.security.Key;
import java.security.cert.X509Certificate;

import aiss.AesBox;
import aiss.AissMime;
import aiss.interf.AISSInterface;
import aiss.shared.AISSUtils;
import aiss.shared.CCConnection;
import aiss.shared.ConfC;
import aiss.shared.Mode;
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
    public static void begin(boolean sign, boolean encrypt, boolean timestamp, String emailInputDir, String outputFile) 
    throws Exception {

        // //////////////////// ZIP FILES IF EXISTS ////////////////////////////////
        File arquivoZip = null;
        File inputDir = new File(emailInputDir);

        if (!inputDir.isDirectory() || inputDir.list().length == 0) {
        	AISSInterface.logsender.append("LOAD - Input must be a directory containing at least one file\n");
            return;
        }
        arquivoZip = zipfiles(inputDir, ZIP_TEMP_FILE);

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
        	AISSInterface.logsender.append("SIGN - signing...\n");
            mimeObject.signature = signDataUsingCC(mimeObject.data);
            mimeObject.certificate = getCCCertificate();
            AISSInterface.logsender.append("SIGN - success.\n");
        }

        if (timestamp) {
            System.out.println("Timestamping");
            AISSInterface.logsender.append("TIMESTAMP - applying...\n");
            mimeObject.timestamp = getSecureTimeStamp(mimeObject.data);
            AISSInterface.logsender.append("TIMESTAMP - success.\n");
        }

        // Cifrar com a caixa
        mimeObject.ciphered = encrypt;
        if (encrypt) {
            System.out.println("Ciphered");
            AISSInterface.logsender.append("CIPHER - ciphering...\n");
            byte[] data = AISSUtils.ObjectToByteArray(mimeObject);
            mimeObject.data = cipherWithBox(data);
            mimeObject.cleanState();
            AISSInterface.logsender.append("CIPHER - success.\n");
        }


        // Serializar e guardar no ficheiro de saida


        // Base64 para guardar
        byte[] objBytes = AISSUtils.ObjectToByteArray(mimeObject);
        String objString = Base64.encode(objBytes);
        // escreve em ficheiro de texto o objString
        BufferedWriter out = new BufferedWriter(new FileWriter(outputFile));
        // PrintWriter out = new PrintWriter(outputFile);
        out.write(objString);
        out.close();
        
        // invoke thunderbird with attachment added
        openThunderbird(new File(outputFile).getAbsolutePath());
        
        System.out.println("Done");
        // Clean temp fiz
        if (arquivoZip != null) {
            arquivoZip.delete();
        }
    }

    private static File zipfiles(File dir, String outputZip) throws Exception {
        new AppZip(dir, outputZip);
        return new File(outputZip);
    }

    private static byte[] signDataUsingCC(byte[] data) throws Exception {
        return CCConnection.SignData(data, ConfC.KEY_TYPE);
    }

    private static byte[] cipherWithBox(byte[] data) throws Exception {
        AesBox box = new AesBox();
        box.init(Mode.Cipher);
        return box.doFinal(data);
        // Cipher cipher = Cipher.getInstance(ConfC.AES_CIPHER_TYPE);
        // IvParameterSpec ivspec = new IvParameterSpec(ConfC.IV);
        // cipher.init(Cipher.ENCRYPT_MODE, loadKey(), ivspec);
        // return cipher.doFinal(data);

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
    
    public static void openThunderbird(String file){
    	Runtime rt = Runtime.getRuntime();
    	try {
			Process pr = rt.exec(new String[] {"open","/Applications/Thunderbird.app","--args","-compose","attachment="+file});
		} catch (IOException e) {
			AISSInterface.logsender.append("Thunderbird was not found in your system.\n");
		}
    }
}
