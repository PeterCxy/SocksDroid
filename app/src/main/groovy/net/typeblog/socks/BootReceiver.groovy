package net.typeblog.socks

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.VpnService
import android.util.Log

import net.typeblog.socks.util.Profile
import net.typeblog.socks.util.ProfileManager
import net.typeblog.socks.util.Utility
import static net.typeblog.socks.BuildConfig.DEBUG

public class BootReceiver extends BroadcastReceiver {
	private static final String TAG = 'BootReceiver'
	
	@Override void onReceive(Context context, Intent intent) {
		def p = ProfileManager.getInstance(context).getDefault()
		
		if (p.autoConnect() && !VpnService.prepare(context)) {
			
			if (DEBUG) {
				Log.d(TAG, "starting VPN service on boot")
			}
			
			Utility.startVpn(context, p)
		}
	}
}
