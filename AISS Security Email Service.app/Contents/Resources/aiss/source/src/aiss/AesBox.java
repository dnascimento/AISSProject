package aiss;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Arrays;

import aiss.shared.Mode;


/**
 * API between AES BOX and Java. Call JNI C file AesBox.c located in aesBox folder.
 */
public class AesBox {
    public byte[] data_out;
    public int size_out;
    public static int MAX_BUFFER_IN = 5000;
    public static int MAX_BUFFER_OUT = 60000;

    /*
     * Init box AES Algo
     */
    public void init(Mode mode) {
        switch (mode) {
        case Cipher:
            init(0);
            break;
        case Decipher:
            init(1);
            break;
        default:
        }
    }



    /**
     * Do fianl AES
     * 
     * @param data_in
     * @return
     * @throws Exception
     */
    public byte[] doIt(byte[] data_in) throws Exception {
        System.out.println(MAX_BUFFER_OUT);
        byte[] bufferIn = new byte[MAX_BUFFER_IN];
        byte[] bufferOut = new byte[MAX_BUFFER_OUT];
        int bytesRead = 0;
        ByteArrayInputStream inputStream = new ByteArrayInputStream(data_in);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        boolean done = false;
        while (!done) {
            System.out.println("New chunck");
            bytesRead = inputStream.read(bufferIn);
            outputStream.write(bufferOut, 0, size_out);
            if (bytesRead < MAX_BUFFER_IN) {
                done = true;
            } else {
                try {
                    System.out.println("Do update");
                    update(bufferIn, bytesRead, bufferOut);
                    System.out.println("After update");
                } catch (Exception e) {
                    System.out.println(e);
                }
            }
        }

        System.out.println("Do update");
        doFinal(bufferIn, bytesRead, bufferOut);
        System.out.println("After update");
        outputStream.write(bufferOut, 0, size_out);
        return outputStream.toByteArray();
    }


    // //////////////// Private Area - API TO BOX /////////////////////////////////
    private native char init(int mode);

    private native char update(byte[] data_in, int size_in, byte[] data_out);

    // int size_out
    private native char doFinal(byte[] data_in, int size_in, byte[] data_out);

    static {
        System.loadLibrary("aesbox");
    }

    public static void main(String args[]) throws Exception {
        AesBox box = new AesBox();
        byte[] plain = new byte[] { 97, 12, 32 };
        byte[] plainOut = new byte[MAX_BUFFER_OUT];
        byte[] out = new byte[MAX_BUFFER_OUT];
        System.out.println("TEST INIT");
        box.init(Mode.Cipher);

        System.out.println("Cipher:");
        out = box.doIt(plain);
        System.out.println(out);

        System.out.println("Test Decipher");
        box.init(Mode.Decipher);

        // System.out.println("Decipher:");
        plainOut = box.doIt(out);

        for (int i = 0; i < plain.length; i++) {
            if (plain[i] != plainOut[i]) {
                System.out.println("DIFFERENT");
            }
        }
        System.out.println("data out" + plain + "    vs    " + plainOut + " size: "
                + box.size_out + "equals: " + Arrays.equals(plain, plainOut));

    }
}
