package net.typeblog.socks.util;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.FileOutputStream;
import java.util.List;

import static net.typeblog.socks.BuildConfig.DEBUG;
import static net.typeblog.socks.util.Constants.*;

public class Utility {
	private static final String TAG = Utility.class.getSimpleName();
	
	public static void extractFile(Context context) {
		String target = DIR;
		
		if (new File(target + "/tun2socks").exists()) {
			return;
		}
		
		new File(target).mkdir();
		
		// TODO: ABI detection
		String source = ABI_DEFAULT;
		
		AssetManager m = context.getAssets();
		
		String[] files = null;
		try {
			files = m.list(source);
		} catch (IOException e) {
			
		}
		
		if (files == null || files.length == 0) {
			return;
		}
		
		for (String f : files) {
			InputStream in = null;
			OutputStream out = null;
			
			try {
				in = m.open(source + "/" + f);
				out = new FileOutputStream(target + "/" + f);
				
				byte[] buf = new byte[512];
				int len;
				
				while ((len = in.read(buf)) > 0) {
					out.write(buf, 0, len);
				}
				
				in.close();
				out.flush();
				out.close();
				
				exec(String.format("chmod 755 %s/%s", target, f));
			} catch (Exception e) {
				
			}
		}
		
	}
	
	public static int exec(String cmd) {
		try {
			Process p = Runtime.getRuntime().exec(cmd);
			
			int ret = p.waitFor();
			
			
			return ret;
		} catch (Exception e) {
			return -1;
		}
	}
	
	public static void killPidFile(String f) {
		File file = new File(f);
		
		if (!file.exists()) {
			return;
		}
		
		InputStream i = null;
		try {
			i = new FileInputStream(file);
		} catch (Exception e) {
			return;
		}
		
		byte[] buf = new byte[512];
		int len;
		StringBuilder str = new StringBuilder();
		
		try {
			while ((len = i.read(buf, 0, 512)) > 0) {
				str.append(new String(buf, 0, len));
			}
			i.close();
		} catch (Exception e) {
			return;
		}
		
		try {
			int pid = Integer.parseInt(str.toString());
			Runtime.getRuntime().exec("kill " + pid).waitFor();
			file.delete();
		} catch (Exception e) {
			
		}
	}
	
	public static String join(List<String> list, String separator) {
		StringBuilder ret = new StringBuilder();
		
		for (String s : list) {
			ret.append(s).append(separator);
		}
		
		return ret.substring(0, ret.length() - separator.length());
	}
}
