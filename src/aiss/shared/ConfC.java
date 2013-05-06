package aiss.shared;

public class ConfC {
    public static final String RSA_ALGORITHM = "RSA";
    public static final String SIGN_ALGO_TS = "sha256WithRSA";
    public static final String SIGN_ALGO_CC = "SHA1withRSA";
    public static final String SHARED_SECRET = "SHARED_SECRET";
    public static final String TIMESTAMP_CERT = "TIMESTAMP_CERT";
    public static final String TIMESTAMP_PRIVATE = "TIMESTAMP_PRIVATE";
    public static final String AES_CIPHER_TYPE = "AES/CBC/PKCS5Padding";

    public static final String PROGRAM_STORE_LOCATION = "keys/aiss.store";
    public static final String TS_STORE = "keys/timestampServer.store";
    public static final String EMAIL_FILENAME = "email.txt";
    public static String KEY_STORE_INST = "JCEKS";
    public static char[] PASSWORD = "aiss".toCharArray();

    public static KeyType KEY_TYPE = KeyType.Autenticacao;

    public static byte[] IV = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };

    public static int TS_SERVER_PORT = 5678;
    public static String TS_SERVER_HOST = "localhost";

}
