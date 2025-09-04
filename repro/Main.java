public class Main {

    static {
        System.loadLibrary("main");
    }

    public static native void foo();
    public static native void init();
    public static native void doStuff();
    public static native void doHook();
    public static native void doUnhook();
    public static native void doHookSimple();
    public static native void doUnhookSimple();

    public static void main(String[] args) throws Exception {
        init();
        for (int i = 0; i <= 128; ++i) {
            System.err.println(i);
            doHookSimple();
            doUnhookSimple();
        }
    }
}
