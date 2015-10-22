package net.typeblog.socks;

import android.content.Intent;
import android.net.VpnService;
import android.net.VpnService.Builder;
import android.os.ParcelFileDescriptor;
import android.util.Log;

import net.typeblog.socks.util.Utility;
import static net.typeblog.socks.util.Constants.*;
import static net.typeblog.socks.BuildConfig.DEBUG;

public class SocksVpnService extends VpnService {
	private static final String TAG = SocksVpnService.class.getSimpleName();
	
	private ParcelFileDescriptor mInterface;

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		
		final String name = intent.getStringExtra(INTENT_NAME);
		final String server = intent.getStringExtra(INTENT_SERVER);
		final int port = intent.getIntExtra(INTENT_PORT, 1080);
		final String username = intent.getStringExtra(INTENT_USERNAME);
		final String passwd = intent.getStringExtra(INTENT_PASSWORD);
		
		// Create an fd.
		configure(name);
		
		if (DEBUG)
			Log.d(TAG, "fd: " + mInterface.getFd());
		
		if (mInterface != null)
			new Thread(new Runnable() {
				@Override
				public void run() {
					start(mInterface.getFd(), server, port, username, passwd);
				}
			}).start();
		
		return START_STICKY;
	}
	
	@Override
	public void onRevoke() {
		super.onRevoke();
	}
	
	private void configure(String name) {
		Builder b = new Builder();
		try {
			mInterface = b.setMtu(1500)
			 .setSession(name)
			 .addAddress("26.26.26.1", 24)
			 .addRoute("0.0.0.0", 0)
			 .addDnsServer("8.8.8.8")
			 .addDisallowedApplication("net.typeblog.stunnel")
			 .establish();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private void start(int fd, String server, int port, String user, String passwd) {
		String command = String.format(
			"%s/tun2socks --netif-ipaddr 26.26.26.2"
			+ " --netif-netmask 255.255.255.0"
			+ " --socks-server-addr %s:%d"
			+ " --tunfd %d"
			+ " --tunmtu 1500"
			+ " --loglevel 3"
			+ " --pid %s/tun2socks.pid"
		, DIR, server, port, fd, DIR);
		
		if (user != null) {
			command += " --username " + user;
			command += " --password " + passwd;
		}
		
		if (DEBUG) {
			Log.d(TAG, command);
		}
		
		Log.d(TAG, "" + Utility.exec(command));
		
		int i = 0;
		while (i < 5) {
			if (System.sendfd(fd) != -1) {
				break;
			}
			
			i++;
		}
	}
	
	
	
}
