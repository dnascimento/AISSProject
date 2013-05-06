package aiss.timestampServer;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.Key;
import java.security.KeyStore;
import java.security.PrivateKey;

import aiss.shared.ConfC;



/**
 * Recebe os dados do utilizador e assina um timestamp com os dados recebidos e com o seu
 * certificado
 */
public class TimeStampServer {


    PrivateKey pkey;

    public static void main(String args[]) throws Exception {
        TimeStampServer server = new TimeStampServer();
        server.start();
    }


    public TimeStampServer() throws IOException, Exception {
        pkey = loadPrivateKey();
    }

    public void start() throws Exception {
        System.out.println("Server Started. Waiting for requests.....");
        ServerSocket serverSocket = new ServerSocket(ConfC.TS_SERVER_PORT);
        while (true) {
            Socket socket = serverSocket.accept();
            TimestampHandler client = new TimestampHandler(socket, pkey);
            client.starter();
        }
    }

    private PrivateKey loadPrivateKey() throws Exception {
        // Tentar carregar a chave do keystore
        KeyStore keyStore = KeyStore.getInstance(ConfC.KEY_STORE_INST);
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(ConfC.TS_STORE);
            keyStore.load(fis, ConfC.PASSWORD);
        } catch (FileNotFoundException exception) {
            keyStore = null;
        } finally {
            if (fis != null) {
                fis.close();
            }
        }

        if (keyStore == null) {
            throw new Exception("TIMESTAMP SERVER: KeyStore not available");
        }
        Key k = keyStore.getKey(ConfC.TIMESTAMP_PRIVATE, ConfC.PASSWORD);
        if (k != null) {
            System.out.println("Private key loaded");
        }
        return (PrivateKey) k;
    }
}
