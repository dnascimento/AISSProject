package aiss.sender;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipException;

import org.apache.commons.io.FileUtils;

import aiss.AissMime;
import aiss.shared.CCConnection;
import aiss.shared.KeyType;
import aiss.shared.Zipper;

/**
 * Ler o certificado do cart‹o de cidad‹o Compressao da mensagem Cifrar com a box Associar
 * um timestamp seguro Return do texto como header+email+anexo+certificado
 */
public class Sender {
    private static final String ZIP_TEMP_FILE = "temp.zip";
    // Define the CC certificate
    static KeyType KEY_TYPE = KeyType.Autenticacao;

    /**
     * Main method
     * 
     * @param signed cipher timestamp fileEmailText attachment1 attachment2...
     *            outputZipFile outputMailFile ex: false true false c://dario.txt
     *            c://goncalo.carito.img c://out.zip
     * @throws Exception
     */
    public static void Main(String args[]) throws Exception {
        boolean sign;
        boolean encrypt;
        boolean timestamp;
        String emailTextFilename;
        try {
            sign = Boolean.parseBoolean(args[0]);
            encrypt = Boolean.parseBoolean(args[1]);
            timestamp = Boolean.parseBoolean(args[2]);
            emailTextFilename = args[3];
        } catch (Exception e) {
            throw new Exception(
                    "Wrong parameters: signed cipher timestamp data  \n ex: false true false jokinaIsMyBestBuddy");
        }

        List<String> filesToZip = new ArrayList<String>();
        for (int i = 4; i < args.length - 2; i++) {
            filesToZip.add(args[i]);
        }

        String outputFile;
        File arquivoZip = null;
        outputFile = args[args.length - 1];


        if (filesToZip.size() != 0) {
            arquivoZip = new File(ZIP_TEMP_FILE);
            zipfiles(filesToZip, arquivoZip);
        }

        AissMime mimeObject = new AissMime();

        File emailTextFile = new File(emailTextFilename);
        byte[] data = FileUtils.readFileToByteArray(emailTextFile);
        mimeObject.emailTextLenght = data.length;

        if (arquivoZip != null) {
            byte[] zip = FileUtils.readFileToByteArray(arquivoZip);
            byte[] dataTemp = data;
            data = new byte[dataTemp.length + zip.length];
            System.arraycopy(dataTemp, 0, data, 0, dataTemp.length);
            System.arraycopy(zip, 0, data, dataTemp.length, zip.length);
        }

        // Assinar
        if (sign) {
            mimeObject.datasign = signDataUsingCC(data);
            mimeObject.certificate = getCCCertificate();
        }

        // Cifrar com a caixa
        if (encrypt) {
            mimeObject.datasign = cipherWithBox(data);
            data = mimeObject.datasign;
        } else {
            mimeObject.data = data;
        }

        if (timestamp) {
            mimeObject.timestampSign = getSecureTimeStamp(data);
        }

        // Serializar e guardar no ficheiro de saida
        ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(outputFile));
        oos.writeObject(mimeObject);
        oos.flush();
        oos.close();
    }

    private static void zipfiles(List<String> filesToZip, File outputZip) throws ZipException,
            IOException {
        Zipper zipzip = new Zipper();
        File[] arquivos = new File[filesToZip.size()];
        zipzip.criarZip(outputZip, arquivos);
    }


    private static byte[] signDataUsingCC(byte[] data) throws Exception {
        return CCConnection.SignData(data, KEY_TYPE);
    }

    private static byte[] cipherWithBox(byte[] data) {
        // TODO cipher data
        return data;
    }


    private static byte[] getSecureTimeStamp(byte[] hash) {
        // TODO
        // http: // www.itconsult.co.uk/stamper/stampinf.htm
        return null;
    }

    public static X509Certificate getCCCertificate() throws Exception {
        X509Certificate certificates[] = CCConnection.getCertificate();
        switch (KEY_TYPE) {
        case Assinatura:
            return certificates[1];

        case Autenticacao:
            return certificates[0];
        default:
            throw new Exception("Invalid Key type");
        }
    }
}
