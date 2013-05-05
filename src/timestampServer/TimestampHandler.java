package timestampServer;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.SignatureException;
import java.util.Date;

import aiss.AISSUtils;

public class TimestampHandler extends
        Thread {

    PrivateKey privkey;
    // ler o certificado
    private Socket socket;

    public TimestampHandler(Socket socket, PrivateKey privkey) {
        this.socket = socket;
        this.privkey = privkey;
    }


    /**
     * Read the hash and sign it
     * 
     * @throws Exception
     */
    public void starter() throws Exception {
        byte[] data = new byte[1024];
        ByteArrayOutputStream outputArray = new ByteArrayOutputStream();
        OutputStream out = socket.getOutputStream();
        out.flush();
        ObjectOutputStream oos = new ObjectOutputStream(outputArray);
        InputStream in = socket.getInputStream();
        int bytesReaded;
        bytesReaded = in.read(data);
        if (bytesReaded > 1024 || bytesReaded == 0) {
            System.out.println("Error");
            return;
        }
        byte[] emailHash = new byte[bytesReaded];
        System.arraycopy(data, 0, emailHash, 0, bytesReaded);

        long timestamp = new Date().getTime();
        TimestampObject timestampSignatureObject = new TimestampObject(data, timestamp);

        byte[] struct = AISSUtils.ObjectToByteArray(timestampSignatureObject);
        byte[] sign = signHash(struct, privkey);
        timestampSignatureObject.setSignature(sign);

        oos.writeObject(timestampSignatureObject);
        out.write(outputArray.toByteArray());
        oos.close();
    }



    private byte[] signHash(byte[] hash, PrivateKey prvKey) throws NoSuchAlgorithmException,
            InvalidKeyException,
            SignatureException {
        Signature sig = Signature.getInstance("SHA1withRSA");
        sig.initSign(prvKey);
        sig.update(hash);
        byte[] signature = sig.sign();
        return signature;
    }

}
