package aiss.receiver;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.security.Signature;
import java.security.cert.X509Certificate;

import aiss.AissMime;
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



    private CCConnection provider;

    /**
     * 1¼ Validar a assinatura temporal 2¼ Decifrar com a caixa 3¼ Validar a assinatura
     * 
     * @param: signed cipher timestamp mailFile outputEmailText <outputZipDirectory>
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        boolean sign;
        boolean encrypt;
        boolean timestamp;
        String inputMailObject;
        // Directorio onde vai guardar o email.txt, o directorio zip de anexos e o txt com
        // o resultado das validacoes
        String outDirectory;
        try {
            sign = Boolean.parseBoolean(args[0]);
            encrypt = Boolean.parseBoolean(args[1]);
            timestamp = Boolean.parseBoolean(args[2]);
            inputMailObject = args[3];
            outDirectory = args[4];
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
            data = decipherAES(mimeObj.rawdata);
        } else {
            data = mimeObj.rawdata;
        }


        // Sacar a assinatura
        if (mimeObj.dataSignLengh != 0) {
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


        // Split do zip e do texto de email
        File file;
        byte[][] emailAndZip = sliptByteArray(data, mimeObj.emailTextLenght);
        if (emailAndZip[EMAIL].length != 0) {
            file = new File(outDirectory + File.pathSeparator + "email.txt");
            byteArrayToFile(emailAndZip[EMAIL], file);

        }
        if (emailAndZip[ZIP].length != 0) {
            file = new File(outDirectory + File.pathSeparator + "data.zip");
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
        Signature signatureEngine = Signature.getInstance(SIGN_ALGORITHM);
        signatureEngine.initVerify(certificate.getPublicKey());
        signatureEngine.update(clearText);
        boolean result = signatureEngine.verify(signature);
        return result;
    }


    public static byte[] decipherAES(byte[] data) {
        // TODO
        return null;
    }

    public static Boolean checkTimeStampSignature(byte[] data, byte[] signature) {
        // TODO
        return true;
    }

    public static byte[] unzip() {
        // TODO Carito
        return null;
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
}
