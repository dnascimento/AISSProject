package timestampServer;

import java.net.ServerSocket;
import java.net.Socket;

/**
 * Recebe os dados do utilizador e assina um timestamp com os dados recebidos e com o seu
 * certificado
 */
public class TimeStampServer {

    public static int LISTEN_PORT = 15678;
    X509Certificate certificate = //TODO Gerar um certificado aleat—rio ˆ m‹o, gravar num ficheiro. Quando o servidor arranca,
            //ler o certificado

    public TimeStampServer() {
        //TODO Read do certificado em disco  this.certificate = read
    }

    public void start() {
        ServerSocket serverSocket = new ServerSocket(LISTEN_PORT);
        while (true) {
            Socket socket = serverSocket.accept();
            TimestampClient client = new TimestampClient(socket,certificate);
            client.start();
        }
    }
}
