package timestampServer;

import java.net.ServerSocket;
import java.net.Socket;
import java.security.*;
import java.io.IOException;
import java.io.File;
import java.io.FileInputStream;

import java.io.FileOutputStream;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.spec.*;



/**
 * Recebe os dados do utilizador e assina um timestamp com os dados recebidos e com o seu
 * certificado
 */
public class TimeStampServer {

    public static int LISTEN_PORT = 15678; 
    public String filenameCS = CreateAndStore();
    public PrivateKey pkey;
    
    public TimeStampServer() throws IOException, Exception {
    	//TODO Read do certificado em disco  this.certificate = read
    	this.pkey = LoadPrivateKey(filenameCS);
    }

    public void start() 
    throws Exception{
        ServerSocket serverSocket = new ServerSocket(LISTEN_PORT);
        TimeStampServer tst = new TimeStampServer();
        PrivateKey privatekey = tst.pkey;
        while (true) {
            Socket socket = serverSocket.accept();
            TimestampClient client = new TimestampClient(socket, privatekey);
            client.starter();
        }   
    }
    
    public String CreateAndStore() 
    					throws IOException, Exception {
    	// Generate a key-pair
    	KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
    	kpg.initialize(1024);
    	KeyPair kp = kpg.generateKeyPair();
    	PublicKey publicKey = kp.getPublic();
    	PrivateKey privateKey = kp.getPrivate();
    	String filename = "private.key";
        
        //TODO: store public key
        // X509EncodedKeySpec x509EncodedKeySpec = new X509EncodedKeySpec(
        //			publicKey.getEncoded());
    	
    	// Store Private Key.
		PKCS8EncodedKeySpec pkcs8EncodedKeySpec = new PKCS8EncodedKeySpec(
				privateKey.getEncoded());
    	FileOutputStream fos = new FileOutputStream("private.key");
		
    	try { 
    		fos.write(pkcs8EncodedKeySpec.getEncoded());
    	}
    	catch (Exception e) {
    		throw new IOException("Unexpected error storing ", e); } 
    	finally {
    		fos.close();
    	}
    	return filename;
    }
    
    public PrivateKey LoadPrivateKey(String filename)
    				throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
    
    	// Read Private Key.
    	filename = "private.key";
		File filePrivateKey = new File("private.key");
		FileInputStream fis = new FileInputStream("private.key");
		byte[] encodedPrivateKey = new byte[(int) filePrivateKey.length()];
		fis.read(encodedPrivateKey);
		fis.close();
		KeyFactory keyFactory = KeyFactory.getInstance("RSA");
		PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(
				encodedPrivateKey);
		PrivateKey privateKey = keyFactory.generatePrivate(privateKeySpec);
		return privateKey;
    }
    
    
}
