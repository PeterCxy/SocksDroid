package net.typeblog.socks;

import android.app.Notification;
import android.content.Intent;
import android.net.VpnService;
import android.net.VpnService.Builder;
import android.os.IBinder;
import android.os.ParcelFileDescriptor;
import android.text.TextUtils;
import android.util.Log;

import net.typeblog.socks.R;
import net.typeblog.socks.util.Routes;
import net.typeblog.socks.util.Utility;
import static net.typeblog.socks.util.Constants.*;
import static net.typeblog.socks.BuildConfig.DEBUG;

public class SocksVpnService extends VpnService {
	class VpnBinder extends IVpnService.Stub {
		@Override
		public boolean isRunning() {
			return mRunning;
		}
		
		@Override
		public void stop() {
			stopMe();
		}
	}
	
	private static final String TAG = SocksVpnService.class.getSimpleName();
	
	private ParcelFileDescriptor mInterface;
	private boolean mRunning = false;
	private IBinder mBinder = new VpnBinder();

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		
		if (intent == null) {
			return 0;
		}
		
		final String name = intent.getStringExtra(INTENT_NAME);
		final String server = intent.getStringExtra(INTENT_SERVER);
		final int port = intent.getIntExtra(INTENT_PORT, 1080);
		final String username = intent.getStringExtra(INTENT_USERNAME);
		final String passwd = intent.getStringExtra(INTENT_PASSWORD);
		final String route = intent.getStringExtra(INTENT_ROUTE);
		final String dns = intent.getStringExtra(INTENT_DNS);
		final int dnsPort = intent.getIntExtra(INTENT_DNS_PORT, 53);
		final boolean perApp = intent.getBooleanExtra(INTENT_PER_APP, false);
		final boolean appBypass = intent.getBooleanExtra(INTENT_APP_BYPASS, false);
		final String[] appList = intent.getStringArrayExtra(INTENT_APP_LIST);
		final boolean ipv6 = intent.getBooleanExtra(INTENT_IPV6_PROXY, false);
		final String udpgw = intent.getStringExtra(INTENT_UDP_GW);
		
		// Create the notification
		startForeground(R.drawable.ic_launcher,
			new Notification.Builder(this)
				.setContentTitle(getString(R.string.notify_title))
				.setContentText(String.format(getString(R.string.notify_msg), name))
				.setPriority(Notification.PRIORITY_MIN)
				.setSmallIcon(android.R.color.transparent)
				.build());
		
		// Create an fd.
		configure(name, route, perApp, appBypass, appList, ipv6);
		
		if (DEBUG)
			Log.d(TAG, "fd: " + mInterface.getFd());
		
		if (mInterface != null)
			start(mInterface.getFd(), server, port, username, passwd, dns, dnsPort, ipv6, udpgw);
		
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
		stopForeground(true);
		
		Utility.killPidFile(DIR + "/tun2socks.pid");
		Utility.killPidFile(DIR + "/pdnsd.pid");
		
		try {
			System.jniclose(mInterface.getFd());
			mInterface.close();
		} catch (Exception e) {
		}
		
		stopSelf();
	}
	
	private void configure(String name, String route, boolean perApp, boolean bypass, String[] apps, boolean ipv6) {
		Builder b = new Builder();
		b.setMtu(1500)
			.setSession(name)
			.addAddress("26.26.26.1", 24)
			.addDnsServer("8.8.8.8");
		
		if (ipv6) {
			// Route all IPv6 traffic
			b.addAddress("fdfe:dcba:9876::1", 126)
				.addRoute("::", 0);
		}
			
		Routes.addRoutes(this, b, route);
			
		// Add the default DNS
		// Note that this DNS is just a stub.
		// Actual DNS requests will be redirected through pdnsd.
		b.addRoute("8.8.8.8", 32);
		
		// Do app routing
		if (!perApp) {
			// Just bypass myself
			try {
				b.addDisallowedApplication("net.typeblog.socks");
			} catch (Exception e) {
				
			}
		} else {
			if (bypass) {
				// First, bypass myself
				try {
					b.addDisallowedApplication("net.typeblog.socks");
				} catch (Exception e) {

				}
				
				for (String p : apps) {
					if (TextUtils.isEmpty(p))
						continue;
					
					try {
						b.addDisallowedApplication(p.trim());
					} catch (Exception e) {
						
					}
				}
			} else {
				for (String p : apps) {
					if (TextUtils.isEmpty(p) || p.trim().equals("net.typeblog.socks")) {
						continue;
					}
					
					try {
						b.addAllowedApplication(p.trim());
					} catch (Exception e) {
						
					}
				}
			}
		}
			
		mInterface = b.establish();
	}

	private void start(int fd, String server, int port, String user, String passwd, String dns, int dnsPort, boolean ipv6, String udpgw) {
		// Start DNS daemon first
		Utility.makePdnsdConf(this, dns, dnsPort);
		
		Utility.exec(String.format("%s/pdnsd -c %s/pdnsd.conf", DIR, DIR));
		
		String command = String.format(
			"%s/tun2socks --netif-ipaddr 26.26.26.2"
			+ " --netif-netmask 255.255.255.0"
			+ " --socks-server-addr %s:%d"
			+ " --tunfd %d"
			+ " --tunmtu 1500"
			+ " --loglevel 5"
			+ " --pid %s/tun2socks.pid"
		, DIR, server, port, fd, DIR);
		
		if (user != null) {
			command += " --username " + user;
			command += " --password " + passwd;
		}
		
		if (ipv6) {
			command += " --netif-ip6addr fdfe:dcba:9876::2";
		}
		
		command += " --dnsgw 26.26.26.1:8091";
		
		if (udpgw != null) {
			command += " --udpgw-remote-server-addr " + udpgw;
		}
		
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
