public class AesBox {
    public native char init(int mode);

    public native char update(byte[] data_in, int size, byte[] data_out, int size_out);

    public native char doFinal(byte[] data_out, int size_out);

    public native char doFinal(byte[] data_in, int size, byte[] data_out, int size_out);

    static {
        System.loadLibrary("aesbox");
    }

    // AesBox box = new AesBox();
    // box.init(0);


}
