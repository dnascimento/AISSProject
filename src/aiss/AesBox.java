package aiss;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;

import aiss.shared.Mode;


/**
 * API between AES BOX and Java. Call JNI C file AesBox.c located in aesBox folder.
 */
public class AesBox {
    public byte[] data_out;
    public int size_out;
    public static int MAX_BUFFER_IN = 68832;
    public static int MAX_BUFFER_OUT = 68864;

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
     * Update AES
     * 
     * @param data_in
     * @return data cyphered
     */
    public byte[] update(byte[] data_in) {
        byte[] bufferIn = new byte[MAX_BUFFER_IN];
        byte[] bufferOut = new byte[MAX_BUFFER_OUT];
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        int lenDataLeft = data_in.length;
        int i = 0;
        while (lenDataLeft > MAX_BUFFER_IN) {
            System.arraycopy(data_in, i * MAX_BUFFER_IN, bufferIn, 0, MAX_BUFFER_IN);
            update(bufferIn, MAX_BUFFER_IN, bufferOut);
            outputStream.write(bufferOut, 0, size_out);
            lenDataLeft = lenDataLeft - MAX_BUFFER_IN;
        }
        System.arraycopy(data_in, i * MAX_BUFFER_IN, bufferIn, 0, lenDataLeft);
        System.out.println("Do update");
        update(bufferIn, bufferIn.length, bufferOut);
        System.out.println("Do update:" + size_out);
        outputStream.write(bufferOut, 0, size_out);
        return outputStream.toByteArray();
    }


    /**
     * Do fianl AES
     * 
     * @param data_in
     * @return
     */
    public byte[] doFinal(byte[] data_in) {
        byte[] bufferIn = new byte[MAX_BUFFER_IN];
        byte[] bufferOut = new byte[MAX_BUFFER_OUT];
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        int lenDataLeft = data_in.length;
        int i = 0;
        while (lenDataLeft > MAX_BUFFER_IN) {
            System.arraycopy(data_in, i * MAX_BUFFER_IN, bufferIn, 0, MAX_BUFFER_IN);
            update(bufferIn, MAX_BUFFER_IN, bufferOut);
            outputStream.write(bufferOut, 0, size_out);
            lenDataLeft = lenDataLeft - MAX_BUFFER_IN;
        }
        System.arraycopy(data_in, i * MAX_BUFFER_IN, bufferIn, 0, lenDataLeft);
        System.out.println("Do final");
        doFinal(bufferIn, lenDataLeft, bufferOut);
        System.out.println("Do final: " + bufferOut);
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

    public static void main(String args[]) {
        AesBox box = new AesBox();
        byte[] plain = new byte[] { 97, 12, 32 };
        byte[] plainOut = new byte[MAX_BUFFER_OUT];
        byte[] out = new byte[MAX_BUFFER_OUT];
        System.out.println("TEST INIT");
        box.init(Mode.Cipher);

        System.out.println("Cipher:");
        out = box.doFinal(plain);
        System.out.println(out);

        System.out.println("Test Decipher");
        box.init(Mode.Decipher);

        // System.out.println("Decipher:");
        plainOut = box.doFinal(out);

        for (int i = 0; i < plain.length; i++) {
            if (plain[i] != plainOut[i]) {
                System.out.println("DIFFERENT");
            }
        }
        System.out.println("data out" + plain + "    vs    " + plainOut + " size: "
                + box.size_out + "equals: " + Arrays.equals(plain, plainOut));

    }
}
