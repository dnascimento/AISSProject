package aiss;

import java.io.ByteArrayOutputStream;

import aiss.shared.Mode;


/**
 * API between AES BOX and Java. Call JNI C file AesBox.c located in aesBox folder.
 */
public class AesBox {
    public byte[] data_out;
    public int size_out;
    public static int MAX_BUFFER_BOX = 68832;

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
        byte[][] fragments = splitDataToBox(data_in);
        byte[] buffer = new byte[MAX_BUFFER_BOX];
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        for (int i = 0; i < fragments.length; i++) {
            update(fragments[i], buffer);
            outputStream.write(buffer, 0, size_out);
        }
        return outputStream.toByteArray();
    }


    /**
     * Do fianl AES
     * 
     * @param data_in
     * @return
     */
    public byte[] doFinal(byte[] data_in) {
        byte[][] fragments = splitDataToBox(data_in);
        byte[] buffer = new byte[MAX_BUFFER_BOX];
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        int i;
        for (i = 0; i < fragments.length - 1; i++) {
            update(fragments[i], buffer);
            outputStream.write(buffer, 0, size_out);
        }
        doFinal(fragments[i], buffer);
        outputStream.write(buffer, 0, size_out);

        return outputStream.toByteArray();
    }

    private byte[][] splitDataToBox(byte[] data) {
        int numFragments = (int) Math.ceil(data.length / MAX_BUFFER_BOX);
        byte[][] out = new byte[numFragments][];
        for (int i = 0; i < numFragments; i++) {
            byte[] buffer = new byte[MAX_BUFFER_BOX];
            System.arraycopy(data, MAX_BUFFER_BOX * i, buffer, 0, MAX_BUFFER_BOX);
            out[i] = buffer;
        }
        return out;
    }



    // //////////////// Private Area - API TO BOX /////////////////////////////////
    private native char init(int mode);

    private native char update(byte[] data_in, byte[] data_out);

    // int size_out
    private native char doFinal(byte[] data_in, byte[] data_out);

    static {
        // System.out.println(System.getProperty("java.library.path"));
        // String current = System.getProperty("java.library.path");
        // System.setProperty("java.library.path", current + ":bin/libaesbox.jnilib")
        // ystem.out.println(System.getProperty("java.library.path"));

        System.loadLibrary("aesbox");
    }

    public static void main(String args[]) {

        AesBox box = new AesBox();
        byte[] plain = new byte[] { 97, 12, 32 };
        byte[] out = new byte[20];

        System.out.println("TEST INIT");
        box.init(Mode.Cipher);

        System.out.println("Cipher:");
        box.doFinal(plain, out);
        //
        // System.out.println("Test Decipher");
        // System.out.println("init: " + box.init(Mode.Decipher));
        //
        // System.out.println("Decipher:");
        // out = box.doFinal(plain);
        // System.out.println("data out" + out + "    vs    " + plain + " size: "
        // + box.size_out + "equals: " + Arrays.equals(plain, out));

    }
}
