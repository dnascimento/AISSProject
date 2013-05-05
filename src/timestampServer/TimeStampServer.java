package timestampServer;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.security.spec.PKCS8EncodedKeySpec;

import aiss.AISSUtils;



/**
 * Recebe os dados do utilizador e assina um timestamp com os dados recebidos e com o seu
 * certificado
 */
public class TimeStampServer {

    static final String PUBLIC_KEY_FILE = "keys/timestampServer/public.key";
    static String KEY_STORE_FILE = "keys/timestampServer/keystore.ks";

    static int LISTEN_PORT = 56789;
    static String KEY_STORE_INST = "JKS";
    static String PRIVATE_KEY = "PRIVATE_KEY";
    PrivateKey pkey;

    public static void main(String args[]) throws Exception {
        TimeStampServer server = new TimeStampServer();
        server.start();
    }


    public TimeStampServer() throws IOException, Exception {
        pkey = loadPrivateKey();
    }

    public void start() throws Exception {
        ServerSocket serverSocket = new ServerSocket(LISTEN_PORT);
        while (true) {
            Socket socket = serverSocket.accept();
            TimestampHandler client = new TimestampHandler(socket, pkey);
            client.starter();
        }
    }




    private PrivateKey loadPrivateKey() throws Exception {
        // Tentar carregar a chave do keystore
        KeyStore keyStore = KeyStore.getInstance(KEY_STORE_INST);
        char[] password = "aiss".toCharArray();
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(KEY_STORE_FILE);
            keyStore.load(fis, password);
        } catch (FileNotFoundException exception) {
            keyStore = null;
        } finally {
            if (fis != null) {
                fis.close();
            }
        }

        if (keyStore != null) {
            Key k = keyStore.getKey(PRIVATE_KEY, password);
            return (PrivateKey) k;
        }

        // keystore not found
        // If not exists, create new
        System.out.println("Keystore not found.Create new");
        keyStore = KeyStore.getInstance(KEY_STORE_INST);
        keyStore.load(null, password);

        // Generate Keypair
        KeyPair keyPair = AISSUtils.generateRSAKeyPair();
        X509Certificate certificate = AISSUtils.generateSelfSignCertificate(keyPair.getPublic());
        X509Certificate[] certificateChain = new X509Certificate[] { certificate };
        keyStore.setKeyEntry(PRIVATE_KEY,
                             keyPair.getPrivate(),
                             password,
                             certificateChain);
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(KEY_STORE_FILE);
            keyStore.store(fos, password);
        } finally {
            if (fos != null) {
                fos.close();
            }
        }
        // Store Public key:
        PKCS8EncodedKeySpec pkcs8EncodedKeySpec = new PKCS8EncodedKeySpec(
                keyPair.getPublic().getEncoded());
        fos = new FileOutputStream(PUBLIC_KEY_FILE);
        try {
            fos.write(pkcs8EncodedKeySpec.getEncoded());
        } catch (Exception e) {
            throw new IOException("Unexpected error storing ", e);
        } finally {
            fos.close();
        }
        return keyPair.getPrivate();
    }



}
