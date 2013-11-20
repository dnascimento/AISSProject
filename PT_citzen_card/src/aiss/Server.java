package aiss;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.KeyStore;
import java.security.Signature;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class Server {

    private static final long TIME_LIMIT_SEC = 1000000;
    private static final int NOUCE_LENGHT = 128;
    private static final KeyType KEY_TYPE = KeyType.Autenticacao;
    private static final String ASSINATURA = "assinatura";
    private static final String AUTENTICACAO = "autenticacao";
    private static final String SIGN_ALGORITHM = "SHA1withRSA";
    private static final String KEY_STORE_INST = "JKS";

    private CCConnection provider;
    private static KeyStore keyStore;
    private final List<Nouce> nounceList = new ArrayList<Nouce>();



    private void start() throws Exception {
        ServerSocket server;
        try {
            server = new ServerSocket(8030);
            Socket clientSocket = server.accept();
            InputStream is = clientSocket.getInputStream();
            ObjectInputStream in = new ObjectInputStream(is);

            OutputStream os = clientSocket.getOutputStream();
            ObjectOutputStream out = new ObjectOutputStream(os);

            // Aguardar pedido
            DTO dto;
            Nouce nouce;
            while (true) {
                dto = (DTO) in.readObject();
                System.out.println("New request: " + dto);
                switch (dto.type) {
                case NouceRequest:
                    System.out.println("Generate nouce");
                    nouce = requestNewNouce();
                    out.writeObject(new DTO(MsgType.NewNouce, nouce, false));
                    break;
                case Verify:
                    Boolean auth = true;
                    System.out.println("Got a nouce verify");
                    // Extract nouce from List
                    if (nounceList.remove(dto.nouce)) {
                        auth = authenticationRequest(dto.nouce, dto.signed);
                    } else {
                        auth = false;
                        System.out.println("Nouce used or time-out");
                    }
                    if (auth) {
                        System.out.println("Autorizado");
                    } else {
                        System.out.println("Rejeitado");
                    }
                    out.writeObject(new DTO(MsgType.Response, dto.nouce, auth));
                    out.flush();
                    break;
                default:
                    throw new Exception("Invalid dto type");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String args[]) throws Exception {
        // Try to load keystore from File
        // SAVE or Load from disk
        keyStore = KeyStore.getInstance("JKS");
        char[] password = "dario".toCharArray();
        FileInputStream fis = null;
        try {
            fis = new FileInputStream("keyStorage");
            keyStore.load(fis, password);
        } catch (FileNotFoundException exception) {
            keyStore = null;
        } finally {
            if (fis != null) {
                fis.close();
            }
        }

        // keystore not found
        if (keyStore == null) {
            System.out.println("Keystore not found.Create new");
            keyStore = KeyStore.getInstance(KEY_STORE_INST);
            keyStore.load(null, password);
            X509Certificate certificates[] = CCConnection.getCertificate();
            keyStore.setCertificateEntry(AUTENTICACAO, certificates[0]);
            keyStore.setCertificateEntry(ASSINATURA, certificates[1]);

            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream("keyStorage");
                keyStore.store(fos, password);
            } finally {
                if (fos != null) {
                    fos.close();
                }
            }
        }
        System.out.println("Starting server");

        Server server = new Server();
        server.start();
    }

    public Nouce requestNewNouce() {
        UUID uuid = UUID.randomUUID();
        byte[] key = uuid.toString().getBytes();
        Nouce nouce = new Nouce(key);
        nounceList.add(nouce);
        return nouce;
    }

    public Boolean authenticationRequest(Nouce nouce, byte[] signed) throws Exception {
        Date now = new Date();
        if ((now.getTime() - nouce.timestamp.getTime()) > TIME_LIMIT_SEC) {
            System.out.println("Nouce is not fresh");
            return false;
        }

        byte[] message = nouce.nouce;

        Certificate certificate;
        switch (KEY_TYPE) {
        case Assinatura:
            certificate = keyStore.getCertificate(ASSINATURA);
            break;
        case Autenticacao:
            certificate = keyStore.getCertificate(AUTENTICACAO);
            break;
        default:
            throw new Exception("Invalid Key type");
        }
        Signature signatureEngine = Signature.getInstance(SIGN_ALGORITHM);
        signatureEngine.initVerify(certificate.getPublicKey());
        signatureEngine.update(message);
        boolean result = signatureEngine.verify(signed);
        System.out.println(result);
        return result;
    }

}
