package aiss.receiver;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.Signature;
import java.security.cert.X509Certificate;

import aiss.AissMime;
import aiss.shared.CCConnection;
import aiss.shared.KeyType;

public class Receiver {
    private static final KeyType KEY_TYPE = KeyType.Autenticacao;
    private static final long TIME_LIMIT_SEC = 1000000;
    private static final int NOUCE_LENGHT = 128;
    private static final String ASSINATURA = "assinatura";
    private static final String AUTENTICACAO = "autenticacao";
    private static final String SIGN_ALGORITHM = "SHA1withRSA";
    private static final String KEY_STORE_INST = "JKS";


    private CCConnection provider;

    /**
     * 1¼ Validar a assinatura temporal 2¼ Decifrar com a caixa 3¼ Validar a assinatura
     * 
     * @param: signed cipher timestamp mailFile outputEmailText <outputZipDirectory>
     * @throws Exception
     */
    public static void Main(String[] args) throws Exception {
        boolean sign;
        boolean encrypt;
        boolean timestamp;
        String inputMailFile;
        String outMailTextFile;
        String outZipDirectory;
        try {
            sign = Boolean.parseBoolean(args[0]);
            encrypt = Boolean.parseBoolean(args[1]);
            timestamp = Boolean.parseBoolean(args[2);
            inputMailFile = args[3];
            outMailTextFile = args[4];
        } catch (Exception e) {
            throw new Exception(
                    "Wrong parameters: signed cipher timestamp data  \n ex: false true false jokinaIsMyBestBuddy");
        }

        String outputZipDirectory = null;
        if (args.length == 6) {
            outZipDirectory = args[5];
        }

        // Ler o objecto
        ObjectInputStream ois = new ObjectInputStream(new FileInputStream(inputMailFile));
        AissMime mimeObj = (AissMime) ois.readObject();
        ois.close();
        
        //TODO Carito:
        //Processar a leitura do objecto, verificar tudo e escrever nos ficheiros

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


    public static byte[] unzip() {
        // TODO Carito
        return null;
    }
}
