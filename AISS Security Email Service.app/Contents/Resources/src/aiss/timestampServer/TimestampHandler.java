package aiss.timestampServer;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.SignatureException;
import java.util.Date;

import aiss.shared.AISSUtils;
import aiss.shared.ConfC;

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
     */
    public void starter() throws Exception {

        System.out.println("New request");
        ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
        ObjectInputStream in = new ObjectInputStream(socket.getInputStream());

        TimestampObject ts = (TimestampObject) in.readObject();

        byte[] emailHash = ts.dataHash;
        long timestamp = new Date().getTime();
        TimestampObject timestampSignatureObject = new TimestampObject(emailHash,
                timestamp);

        byte[] struct = AISSUtils.ObjectToByteArray(timestampSignatureObject);
        byte[] sign = signHash(struct, privkey);
        timestampSignatureObject.setSignature(sign);
        System.out.println("Response");

        out.writeObject(timestampSignatureObject);
        out.close();
    }



    private byte[] signHash(byte[] hash, PrivateKey prvKey) throws NoSuchAlgorithmException,
            InvalidKeyException,
            SignatureException {
        Signature sig = Signature.getInstance(ConfC.SIGN_ALGO_TS);
        sig.initSign(prvKey);
        sig.update(hash);
        byte[] signature = sig.sign();
        return signature;
    }

}
