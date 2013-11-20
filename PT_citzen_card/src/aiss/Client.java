package aiss;

import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;

public class Client {
    // Qual o tipo de assinatura: Autenticacao ou assinatura?
    private static final KeyType KEY_TYPE = KeyType.Autenticacao;


    public static void main(String args[]) throws Exception {
        Client client = new Client();
    }

    public Client() throws Exception {
        Socket socket = new Socket("127.0.0.1", 8030);
        System.out.println("Connected");


        OutputStream os = socket.getOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(os);

        System.out.println("Send request");
        // Send nouceRequest
        out.writeObject(new DTO(MsgType.NouceRequest, null, false));
        out.flush();
        System.out.println("Get response");

        InputStream is = socket.getInputStream();
        ObjectInputStream in = new ObjectInputStream(is);


        while (true) {


            // Receive and sign
            DTO response = (DTO) in.readObject();
            System.out.println("Parse response");
            switch (response.type) {
            case NewNouce:
                byte[] signed = CCConnection.SignData(response.nouce.nouce, KEY_TYPE);

                System.out.println("Nouce Signed");
                DTO signedData = new DTO(MsgType.Verify, response.nouce, null);
                signedData.signed = signed;
                out.writeObject(signedData);
                break;
            case Response:
                if (response.auth) {
                    System.out.println("Autenticado");
                } else {
                    System.out.println("Rejeitado");
                }

            default:
                break;
            }
        }
    }
}
