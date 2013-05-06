import java.util.Arrays;

public class AesBox {
    public byte[] data_out;
    public int size_out;


    private native char init(int mode);

    private native char update(byte[] data_in, int size, byte[] data_out);

    // int size_out
    private native char doFinal(byte[] data_in, int size_in, byte[] data_out);

    static {
        System.loadLibrary("aesbox");
    }

    public static void main(String args[]) {
        AesBox box = new AesBox();
        byte[] out = new byte[3];
        byte[] goal = new byte[] { 97, 12, 32 };

        System.out.println("TEST INIT");
        System.out.println("init: " + box.init(0));
        System.out.println(" ");

        System.out.println("TEST INIT");
        System.out.println("Update:" + box.update(goal, goal.length, out));
        System.out.println("data out" + out + "    vs    " + goal + " size: "
                + box.size_out + "equals: " + Arrays.equals(goal, out));

        System.out.println("TEST DO FINAL:");
        System.out.println("FINAL:" + box.doFinal(goal, goal.length, out));
        System.out.println("data out" + out + "    vs    " + goal + " size: "
                + box.size_out + "equals: " + Arrays.equals(goal, out));

    }
}
