package aiss.shared;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.security.Key;
import java.security.KeyStore;
import java.security.MessageDigest;
import java.security.cert.X509Certificate;



public class AISSUtils {

    private static final int BUFFER_SIZE = 1024;



    public static byte[] ObjectToByteArray(Object obj) throws Exception {
        if (obj == null) {
            return null;
        }
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutput out = null;
        try {
            out = new ObjectOutputStream(bos);
            out.writeObject(obj);
            return bos.toByteArray();
        } finally {
            out.close();
            bos.close();
        }
    }

    public static Object ByteArrayToObject(byte[] data) throws Exception {
        ByteArrayInputStream bis = new ByteArrayInputStream(data);

        ObjectInput in = null;
        try {
            in = new ObjectInputStream(bis);
            return in.readObject();
        } finally {
            bis.close();
            in.close();
        }
    }


    public static Key loadSharedSecretKey(String location) throws Exception {
        // Tentar carregar a chave do keystore
        KeyStore keyStore = KeyStore.getInstance(ConfC.KEY_STORE_INST);
        FileInputStream fis = null;
        fis = new FileInputStream(location);
        keyStore.load(fis, ConfC.PASSWORD);
        Key k = keyStore.getKey(ConfC.SHARED_SECRET, ConfC.PASSWORD);
        return k;
    }


    public static X509Certificate loadTimestampCertificate(String location) throws Exception {
        // Tentar carregar a chave do keystore
        KeyStore keyStore = KeyStore.getInstance(ConfC.KEY_STORE_INST);
        FileInputStream fis = null;
        fis = new FileInputStream(location);
        keyStore.load(fis, ConfC.PASSWORD);
        X509Certificate cert = (X509Certificate) keyStore.getCertificate(ConfC.TIMESTAMP_CERT);
        return cert;
    }

    public static byte[] readFileToByteArray(File emailTextFile) throws IOException {
        FileInputStream in = new FileInputStream(emailTextFile);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buf = new byte[BUFFER_SIZE];
        int bytesRead = in.read(buf);
        while (bytesRead != -1) {
            baos.write(buf, 0, bytesRead);
            bytesRead = in.read(buf);
        }
        baos.flush();
        in.close();
        return baos.toByteArray();
    }


    public static void byteArrayToFile(byte[] data, File destination) throws Exception {
        FileOutputStream fos = new FileOutputStream(destination);
        fos.write(data);
        fos.flush();
        fos.close();
    }


    public static byte[][] sliptByteArray(byte[] data, int pos) {
        byte[] begin = new byte[pos];
        byte[] end = new byte[data.length - pos];

        System.arraycopy(data, 0, begin, 0, pos);
        System.arraycopy(data, pos, end, 0, end.length);

        byte[][] result = new byte[2][];
        result[0] = begin;
        result[1] = end;
        return result;
    }

    public static byte[] concatByteArray(byte[] a, byte[] b) {
        byte[] result = new byte[a.length + b.length];
        System.arraycopy(a, 0, result, 0, a.length);
        System.arraycopy(b, 0, result, a.length, b.length);
        return result;
    }

    public static byte[] FileHash(File absoluteFile) throws IOException {
        BufferedInputStream reader = new BufferedInputStream(new FileInputStream(
                absoluteFile));
        int read;
        byte[] buffer = new byte[8192];
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            while ((read = reader.read(buffer)) > 0) {
                digest.update(buffer, 0, read);
            }
            byte[] hash = digest.digest();
            return hash;
        } catch (Exception e) {
            e.printStackTrace(System.err);
            return null;
        } finally {
            reader.close();
        }
    }

}
