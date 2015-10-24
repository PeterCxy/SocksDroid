package net.typeblog.socks.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.FileOutputStream;
import java.util.List;

import net.typeblog.socks.R;
import static net.typeblog.socks.BuildConfig.DEBUG;
import static net.typeblog.socks.util.Constants.*;

public class Utility {
	private static final String TAG = Utility.class.getSimpleName();
	
	public static void extractFile(Context context) {
		// Check app version
		SharedPreferences pref = context.getSharedPreferences("ver", Context.MODE_PRIVATE);
		
		int ver = 0;
		try {
			ver = context.getPackageManager().getPackageInfo("net.typeblog.socks", 0).versionCode;
		} catch (Exception e) {
			throw new RuntimeException(e);
			//return;
		}
		
		if (pref.getInt("ver", -1) == ver) {
			return;
		}
		
		String target = DIR;
		
		if (new File(target + "/tun2socks").exists()) {
			new File(target + "/tun2socks").delete();
		}
		
		if (new File(target + "/pdnsd").exists()) {
			new File(target + "/pdnsd").delete();
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
		
		pref.edit().putInt("ver", ver).commit();
		
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
			int pid = Integer.parseInt(str.toString().trim().replace("\n", ""));
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
	
	public static void makePdnsdConf(Context context, String dns, int port) {
		String conf = String.format(context.getString(R.string.pdnsd_conf), dns, port);
		
		File f = new File(DIR + "/pdnsd.conf");
		
		if (f.exists()) {
			f.delete();
		}
		
		try {
			OutputStream out = new FileOutputStream(f);
			out.write(conf.getBytes());
			out.flush();
			out.close();
		} catch (Exception e) {
			
		}
		
		File cache = new File(DIR + "/pdnsd.cache");
		
		if (!cache.exists()) {
			try {
				cache.createNewFile();
			} catch (Exception e) {
				
			}
		}
	}
}
