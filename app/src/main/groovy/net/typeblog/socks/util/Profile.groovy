package net.typeblog.socks.util

import android.content.Context
import android.content.SharedPreferences

import static net.typeblog.socks.util.Constants.*

public class Profile {
	private Context mContext;
	private SharedPreferences mPref;
	private String mName;
	private String mPrefix;
	
	Profile(Context context, SharedPreferences pref, String name) {
		mContext = context
		mPref = pref
		mName = name
		mPrefix = prefPrefix(name)
	}
	
	String getName() {
		mName
	}
	
	String getServer() {
		mPref.getString(key("server"), "127.0.0.1")
	}
	
	void setServer(String server) {
		mPref.edit().putString(key("server"), server).commit()
	}
	
	int getPort() {
		mPref.getInt(key("port"), 1080)
	}
	
	void setPort(int port) {
		mPref.edit().putInt(key("port"), port).commit()
	}
	
	boolean getUserPw() {
		mPref.getBoolean(key("userpw"), false)
	}
	
	void setUserPw(boolean is) {
		mPref.edit().putBoolean(key("userpw"), is).commit()
	}
	
	String getUsername() {
		mPref.getString(key("username"), "")
	}
	
	void setUsername(String username) {
		mPref.edit().putString(key("username"), username).commit()
	}
	
	String getPassword() {
		mPref.getString(key("password"), "")
	}
	
	void setPassword(String password) {
		mPref.edit().putString(key("password"), password).commit()
	}
	
	String getRoute() {
		mPref.getString(key("route"), ROUTE_ALL)
	}
	
	void setRoute(String route) {
		mPref.edit().putString(key("route"), route).commit()
	}
	
	String getDns() {
		mPref.getString(key("dns"), "8.8.8.8")
	}
	
	void setDns(String dns) {
		mPref.edit().putString(key("dns"), dns).commit()
	}
	
	int getDnsPort() {
		mPref.getInt(key("dns_port"), 53)
	}
	
	void setDnsPort(int port) {
		mPref.edit().putInt(key("dns_port"), port).commit()
	}
	
	boolean getPerApp() {
		mPref.getBoolean(key("perapp"), false)
	}
	
	void setPerApp(boolean is) {
		mPref.edit().putBoolean(key("perapp"), is).commit()
	}
	
	boolean getBypassApp() {
		mPref.getBoolean(key("appbypass"), false)
	}
	
	void setBypassApp(boolean is) {
		mPref.edit().putBoolean(key("appbypass"), is).commit()
	}
	
	String getAppList() {
		mPref.getString(key("applist"), "")
	}
	
	void setAppList(String list) {
		mPref.edit().putString(key("applist"), list).commit()
	}
	
	boolean getHasIPv6() {
		mPref.getBoolean(key("ipv6"), false)
	}
	
	void setHasIPv6(boolean has) {
		mPref.edit().putBoolean(key("ipv6"), has).commit()
	}
	
	boolean getHasUDP() {
		mPref.getBoolean(key("udp"), false)
	}
	
	void setHasUDP(boolean has) {
		mPref.edit().putBoolean(key("udp"), has).commit()
	}
	
	String getUDPGW() {
		mPref.getString(key("udpgw"), "127.0.0.1:7300")
	}
	
	void setUDPGW(String gw) {
		mPref.edit().putString(key("udpgw"), gw).commit()
	}
	
	boolean getAutoConnect() {
		mPref.getBoolean(key("auto"), false)
	}
	
	void setAutoConnect(boolean a) {
		mPref.edit().putBoolean(key("auto"), a).commit();
	}

	void delete() {
		mPref.edit().with {
			remove key("server")
			remove key("port")
			remove key("userpw")
			remove key("username")
			remove key("password")
			remove key("route")
			remove key("dns")
			remove key("dns_port")
			remove key("perapp")
			remove key("appbypass")
			remove key("applist")
			remove key("ipv6")
			remove key("udp")
			remove key("udpgw")
			remove key("auto")
			commit()
		}
	}
	
	private String key(String k) {
		mPrefix + k
	}
	
	private static String prefPrefix(String name) {
		name.replace("_", "__").replace(" ", "_")
	}
}
