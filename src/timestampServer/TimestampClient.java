package timestampServer;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.security.cert.X509Certificate;
import java.util.Date;

public class TimestampClient extends
        Thread {

    X509Certificate certificate;
    // ler o certificado
    private Socket socket;

    public TimestampClient(Socket socket, X509Certificate certificate) {
        this.socket = socket;
        this.certificate = certificate;
    }

    @Override
    public void start() {
        byte[] data = new byte[1024];
        ByteArrayOutputStream outputArray = new ByteArrayOutputStream();
        OutputStream out = socket.getOutputStream();
        out.flush();
        InputStream in = socket.getInputStream();
        int bytesReaded;
        bytesReaded = in.read(data);
        if (bytesReaded > 1024 || bytesReaded == 0) {
            System.out.println("Error");
            return;
        }
        byte[] hash = new byte[bytesReaded];
        System.arraycopy(data, 0, hash, 0, bytesReaded);

        byte[] sign = signHash(hash);

        TimestampObject timestampSignatureObject = new TimestampObject(data, new Date(),
                sign);


        // TODO invés de return de um sign, devolver o object
        out.write(timestampSignatureObject);
        out.flush();

    }

    private byte[] signHash(byte[] hash) {
        // TODO Verificar se tem um comprimento válido (128bits salvo erro)
        // Assinar utilizando o certificate
        return null;
    }
}
