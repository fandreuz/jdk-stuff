import java.io.*;
import jdk.internal.misc.Unsafe;
import java.lang.reflect.Field;

public class Main {

    static {
        System.loadLibrary("main");
    }

    public static native void hookUnhook();

    public static void main(String[] args) throws Exception {
        Field f = Unsafe.class.getDeclaredField("theUnsafe");
        f.setAccessible(true);
        Unsafe unsafe = (Unsafe) f.get(null);

        for (int i = 0; i < 10000000; ++i) {
            hookUnhook();
            unsafe.park(false, 1L);
        }
	System.in.read();
    }
}
