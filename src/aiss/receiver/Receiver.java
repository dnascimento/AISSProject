package aiss.receiver;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.security.PublicKey;
import java.security.Signature;
import java.security.cert.X509Certificate;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;

import aiss.AissMime;
import aiss.sender.Sender;
import aiss.shared.CCConnection;
import aiss.shared.KeyType;



public class Receiver {


    private static final KeyType KEY_TYPE = KeyType.Autenticacao;
    private static final String ASSINATURA = "assinatura";
    private static final String AUTENTICACAO = "autenticacao";
    private static final String SIGN_ALGORITHM = "SHA1withRSA";
    private static final String KEY_STORE_INST = "JKS";
    private static final int DATA = 0;
    private static final int SIGNATURE = 1;
    private static final int EMAIL = 0;
    private static final int ZIP = 1;

    private static X509Certificate[] caCertificateList = new X509Certificate[2];

    private CCConnection provider;

    /**
     * 1¼ Validar a assinatura temporal 2¼ Decifrar com a caixa 3¼ Validar a assinatura
     * 
     * @param: signed cipher timestamp mailFile outputEmailText <outputZipDirectory>
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        String inputMailObject;
        // Directorio onde vai guardar o email.txt, o directorio zip de anexos e o txt com
        // o resultado das validacoes
        String outDirectoryPath;
        try {
            inputMailObject = args[0];
            outDirectoryPath = args[1];
        } catch (Exception e) {
            throw new Exception("Wrong parameters");
        }

        // Ler o objecto
        ObjectInputStream ois = new ObjectInputStream(
                new FileInputStream(inputMailObject));
        AissMime mimeObj = (AissMime) ois.readObject();
        ois.close();


        // Checktimestamp sign
        if (mimeObj.timestampSign != null) {
            boolean result = checkTimeStampSignature(mimeObj.rawdata,
                                                     mimeObj.timestampSign);
            System.out.println("Timestamp Sign: " + result);
            // TODO mostrar o resultado
        }



        byte[] data;
        // Decifrar os dados
        if (mimeObj.ciphered) {
            System.out.println("Decipher");
            data = decipherAES(mimeObj.rawdata);
        } else {
            data = mimeObj.rawdata;
        }


        // Sacar a assinatura
        if (mimeObj.dataSignLengh != 0) {
            System.out.println("Checktimestamp");
            int signatureBegin = mimeObj.rawdata.length - mimeObj.dataSignLengh;

            byte[][] result = sliptByteArray(mimeObj.rawdata, signatureBegin);

            if (mimeObj.certificate == null) {
                throw new Exception("Mail without certificate");
            }
            boolean isSigned = checkSignature(result[DATA],
                                              result[SIGNATURE],
                                              mimeObj.certificate);
            if (isSigned) {
                System.out.println("Assinatura v‡lida");
            } else {
                System.out.println("Assinatura inv‡lida");
            }
            data = result[DATA];
        }

        File outDirectory = new File(outDirectoryPath);
        if (outDirectory.exists()) {
            throw new Exception("Output directory already exists");
        }
        outDirectory.mkdirs();

        // Split do zip e do texto de email
        File file;
        byte[][] emailAndZip = sliptByteArray(data, mimeObj.emailTextLenght);
        if (emailAndZip[EMAIL].length != 0) {
            System.out.println("Get mail");
            file = new File(outDirectory, "email.txt");
            byteArrayToFile(emailAndZip[EMAIL], file);

        }
        if (emailAndZip[ZIP].length != 0) {
            System.out.println("Get zip");
            file = new File(outDirectory, "data.zip");
            byteArrayToFile(emailAndZip[ZIP], file);
        }


        // TODO escrever o log de resultado
        /**
         * Create 2 files and 1 directory: - email.txt -> clean email text - attachments
         * -> attachments folder - status.txt -> timestampSigned? authorSigned?
         */


    }

    public static Boolean checkSignature(
            byte[] clearText,
                byte[] signature,
                X509Certificate certificate) throws Exception {
        if (!CCCertificateValidation(certificate)) {
            return false;
        }
        Signature signatureEngine = Signature.getInstance(SIGN_ALGORITHM);
        signatureEngine.initVerify(certificate.getPublicKey());
        signatureEngine.update(clearText);
        boolean result = signatureEngine.verify(signature);
        return result;
    }


    public static byte[] decipherAES(byte[] data) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        byte[] iv = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
        IvParameterSpec ivspec = new IvParameterSpec(iv);
        cipher.init(Cipher.DECRYPT_MODE, Sender.loadKey(), ivspec);
        return cipher.doFinal(data);
    }

    public static Boolean checkTimeStampSignature(byte[] data, byte[] signature) {
        // TODO
        return true;
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


    public static void byteArrayToFile(byte[] data, File destination) throws Exception {
        FileOutputStream fos = new FileOutputStream(destination);
        fos.write(data);
        fos.flush();
        fos.close();
    }

    private static Boolean CCCertificateValidation(X509Certificate cert) throws Exception {
        PublicKey key;
        switch (KEY_TYPE) {
        case Assinatura:
            key = caCertificateList[1].getPublicKey();
            break;
        case Autenticacao:
            key = caCertificateList[0].getPublicKey();
            break;
        default:
            throw new Exception("Invalid Key type");
        }
        try {
            cert.verify(key);
        } catch (Exception e) {
            return false;
        }
        return true;
    }
}
