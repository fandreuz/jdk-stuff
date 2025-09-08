import java.io.*;
import jdk.internal.misc.Unsafe;
import java.lang.reflect.Field;
import java.lang.management.ManagementFactory;
import javax.management.MBeanServer;
import javax.management.ObjectName;

public class Main {

    static {
        System.loadLibrary("main");
    }

    public static native void hookUnhook();

    public static void main(String[] args) throws Exception {
        Field f = Unsafe.class.getDeclaredField("theUnsafe");
        f.setAccessible(true);
        Unsafe unsafe = (Unsafe) f.get(null);

        for (int i = 0; i < 1000; ++i) {
            hookUnhook();
            unsafe.park(false, 1L);
        }

        countUnsafeParkInCodeCache();
    }

    private static void countUnsafeParkInCodeCache() throws Exception {
        MBeanServer server = ManagementFactory.getPlatformMBeanServer();
        ObjectName on = new ObjectName("com.sun.management:type=DiagnosticCommand");
        String op = "compilerCodelist"; // maps from jcmd "Compiler.codelist"
        String[] args = new String[0];
        String[] sig = new String[] { "[Ljava.lang.String;" };
        String output = (String) server.invoke(on, op, new Object[] { args }, sig);
        for (String s : output.split("\n")) {
            if (s.contains("Unsafe.park")) System.err.println(s);
        }
    }
}
