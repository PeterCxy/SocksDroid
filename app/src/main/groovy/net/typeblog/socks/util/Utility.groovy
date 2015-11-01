package net.typeblog.socks.util

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.AssetManager
import android.util.Log

import java.util.List;

import net.typeblog.socks.R
import net.typeblog.socks.SocksVpnService
import net.typeblog.socks.System
import static net.typeblog.socks.BuildConfig.DEBUG
import static net.typeblog.socks.util.Constants.*

public class Utility {
	private static final String TAG = 'Utility'
	
	static void extractFile(Context context) {
		// Check app version
		def pref = context.getSharedPreferences("ver", Context.MODE_PRIVATE)
		
		int ver = 0;
		try {
			ver = context.packageManager.getPackageInfo("net.typeblog.socks", 0).versionCode
		} catch (any) {
			
		}
		
		if (pref.getInt("ver", -1) == ver) {
			return
		}
		
		def target = DIR
		
		if (DEBUG) {
			Log.d(TAG, "target = ${target}");
		}
		
		//if (new File("${target}/tun2socks").exists()) {
		//	new File("${target}/tun2socks").delete()
		//}
		
		//if (new File("${target}/pdnsd").exists()) {
		//	new File("${target}/pdnsd").delete();
		//}
		
		new File(target).mkdir();
		
		String source = System.ABI
		
		AssetManager m = context.assets
		
		String[] files = null;
		try {
			files = m.list(source)
		} catch (IOException e) {
			
		}
		
		if (!files) {
			return;
		}
		
		files.each { f ->
			InputStream input = null;
			
			try {
				input = m.open("${source}/${f}")
				
				def file = ["${target}/${f}"] as File

				if (file.exists()) {
					file.delete()
				}

				file.withOutputStream { out -> out << input }

				input.close()

				exec("chmod 755 ${target}/${f}")
			} catch (any) {
				
			}
		}
		
		pref.edit().putInt("ver", ver).commit()
		
	}
	
	static int exec(String cmd) {
		try {
			def p = Runtime.runtime.exec(cmd)
			
			p.waitFor()
		} catch (any) {
			-1
		}
	}
	
	static void killPidFile(String f) {
		File file = new File(f)
		
		if (!file.exists()) {
			return
		}
		
		try {
			int pid = Integer.parseInt(file.text.trim().replace("\n", ""))
			Runtime.runtime.exec("kill ${pid}").waitFor()
			file.delete()
		} catch (any) {
			
		}
	}
	
	public static void makePdnsdConf(Context context, String dns, int port) {
		String conf = String.format(context.getString(R.string.pdnsd_conf), dns, port)
		
		File f = new File("${DIR}/pdnsd.conf")
		
		if (f.exists()) {
			f.delete()
		}

		f.withWriter { out -> out.print conf }
		
		File cache = new File("${DIR}/pdnsd.cache");
		
		if (!cache.exists()) {
			try {
				cache.createNewFile()
			} catch (any) {
				
			}
		}
	}
	
	static void startVpn(Context context, Profile profile) {
		Intent i = new Intent(context, SocksVpnService.class)
		
		i.with {
			putExtra INTENT_NAME, profile.name
			putExtra INTENT_SERVER, profile.server
			putExtra INTENT_PORT, profile.port
			putExtra INTENT_ROUTE, profile.route
			putExtra INTENT_DNS, profile.dns
			putExtra INTENT_DNS_PORT, profile.dnsPort
			putExtra INTENT_PER_APP, profile.perApp
			putExtra INTENT_IPV6_PROXY, profile.hasIPv6
		
			if (profile.userPw) {
				putExtra INTENT_USERNAME, profile.username
				putExtra INTENT_PASSWORD, profile.password
			}

			if (profile.perApp) {
				putExtra INTENT_APP_BYPASS, profile.bypassApp
				putExtra INTENT_APP_LIST, profile.appList.split("\n")
			}

			if (profile.hasUDP) {
				putExtra INTENT_UDP_GW, profile.UDPGW
			}
		}
		
		context.startService(i)
	}
}
