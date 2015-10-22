package net.typeblog.socks.util;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.FileOutputStream;

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
}
