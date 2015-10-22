package net.typeblog.socks;

public class System
{
	static {
		java.lang.System.loadLibrary("system");
	}

	public static native void exec(String cmd);
	public static native String getABI();
	public static native int sendfd(int fd);
	public static native void jniclose(int fd);
}
