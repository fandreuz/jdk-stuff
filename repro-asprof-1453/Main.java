import java.io.*;

public class Main {

    static {
        System.loadLibrary("main");
    }

    public static native void bug();

    public static native void callRegisterNatives(int idx);

    public static void main(String[] args) throws Exception {
        for (int i = 0; i < 10000000; ++i) {
            callRegisterNatives(i % 2);
            bug();
        }
	    System.in.read();
    }
}
