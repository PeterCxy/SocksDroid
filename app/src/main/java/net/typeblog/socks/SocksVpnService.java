package net.typeblog.socks;

import android.content.Intent;
import android.net.VpnService;
import android.net.VpnService.Builder;
import android.os.Binder;
import android.os.IBinder;
import android.os.ParcelFileDescriptor;
import android.util.Log;

import net.typeblog.socks.util.Routes;
import net.typeblog.socks.util.Utility;
import static net.typeblog.socks.util.Constants.*;
import static net.typeblog.socks.BuildConfig.DEBUG;

public class SocksVpnService extends VpnService {
	class VpnBinder extends Binder {
		public boolean isRunning() {
			return mRunning;
		}
		
		public void stop() {
			stopMe();
		}
	}
	
	private static final String TAG = SocksVpnService.class.getSimpleName();
	
	private ParcelFileDescriptor mInterface;
	private boolean mRunning = false;
	private Binder mBinder = new VpnBinder();

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		
		final String name = intent.getStringExtra(INTENT_NAME);
		final String server = intent.getStringExtra(INTENT_SERVER);
		final int port = intent.getIntExtra(INTENT_PORT, 1080);
		final String username = intent.getStringExtra(INTENT_USERNAME);
		final String passwd = intent.getStringExtra(INTENT_PASSWORD);
		final String route = intent.getStringExtra(INTENT_ROUTE);
		
		// Create an fd.
		configure(name, route);
		
		if (DEBUG)
			Log.d(TAG, "fd: " + mInterface.getFd());
		
		if (mInterface != null)
			start(mInterface.getFd(), server, port, username, passwd, "8.8.8.8");
		
		return START_STICKY;
	}
	
	@Override
	public void onRevoke() {
		super.onRevoke();
		stopMe();
	}

	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		
		stopMe();
	}
	
	private void stopMe() {
		Utility.killPidFile(DIR + "/tun2socks.pid");
		Utility.killPidFile(DIR + "/pdnsd.pid");
		
		try {
			System.jniclose(mInterface.getFd());
			mInterface.close();
		} catch (Exception e) {
		}
		
		stopSelf();
	}
	
	private void configure(String name, String route) {
		Builder b = new Builder();
		try {
			b.setMtu(1500)
			 .setSession(name)
			 .addAddress("26.26.26.1", 24)
			 .addDnsServer("8.8.8.8")
			 .addDisallowedApplication("net.typeblog.socks")
			 .addDisallowedApplication("net.typeblog.stunnel");
			 
			Routes.addRoutes(this, b, route);
			
			// Add the default DNS
			// Note that this DNS is just a stub.
			// Actual DNS requests will be redirected through pdnsd.
			b.addRoute("8.8.8.8", 32);
			
			mInterface = b.establish();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private void start(int fd, String server, int port, String user, String passwd, String dns) {
		// Start DNS daemon first
		Utility.makePdnsdConf(this, dns);
		
		Utility.exec(String.format("%s/pdnsd -c %s/pdnsd.conf", DIR, DIR));
		
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
		
		command += " --dnsgw 26.26.26.1:8091";
		
		if (DEBUG) {
			Log.d(TAG, command);
		}
		
		if (Utility.exec(command) != 0) {
			stopMe();
			return;
		}
		
		// Try to send the Fd through socket.
		int i = 0;
		while (i < 5) {
			if (System.sendfd(fd) != -1) {
				mRunning = true;
				return;
			}
			
			i++;
			
			try {
				Thread.sleep(1000 * i);
			} catch (Exception e) {
				
			}
		}
		
		// Should not get here. Must be a failure.
		stopMe();
	}
}
