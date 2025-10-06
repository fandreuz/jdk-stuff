import java.io.*;

public class Main {

    static {
        System.loadLibrary("main");
    }

    public static native void bug();

    public static native void hookUnhook();

    public static void main(String[] args) throws Exception {
        for (int i = 0; i < 10000000; ++i) {
            hookUnhook();
            bug();
        }
	    System.in.read();
    }
}
