package net.typeblog.socks.util;

import android.content.Context;
import android.content.SharedPreferences;

import static net.typeblog.socks.util.Constants.*;

public class Profile {
	private Context mContext;
	private SharedPreferences mPref;
	private String mName;
	private String mPrefix;
	
	Profile(Context context, SharedPreferences pref, String name) {
		mContext = context;
		mPref = pref;
		mName = name;
		mPrefix = prefPrefix(name);
	}
	
	public String getName() {
		return mName;
	}
	
	public String getServer() {
		return mPref.getString(key("server"), "127.0.0.1");
	}
	
	public void setServer(String server) {
		mPref.edit().putString(key("server"), server).commit();
	}
	
	public int getPort() {
		return mPref.getInt(key("port"), 1080);
	}
	
	public void setPort(int port) {
		mPref.edit().putInt(key("port"), port).commit();
	}
	
	public boolean isUserPw() {
		return mPref.getBoolean(key("userpw"), false);
	}
	
	public void setIsUserpw(boolean is) {
		mPref.edit().putBoolean(key("userpw"), is).commit();
	}
	
	public String getUsername() {
		return mPref.getString(key("username"), "");
	}
	
	public void setUsername(String username) {
		mPref.edit().putString(key("username"), username).commit();
	}
	
	public String getPassword() {
		return mPref.getString(key("password"), "");
	}
	
	public void setPassword(String password) {
		mPref.edit().putString(key("password"), password).commit();
	}
	
	public String getRoute() {
		return mPref.getString(key("route"), ROUTE_ALL);
	}
	
	public void setRoute(String route) {
		mPref.edit().putString(key("route"), route).commit();
	}
	
	public String getDns() {
		return mPref.getString(key("dns"), "8.8.8.8");
	}
	
	public void setDns(String dns) {
		mPref.edit().putString(key("dns"), dns).commit();
	}
	
	public int getDnsPort() {
		return mPref.getInt(key("dns_port"), 53);
	}
	
	public void setDnsPort(int port) {
		mPref.edit().putInt(key("dns_port"), port).commit();
	}
	
	public boolean isPerApp() {
		return mPref.getBoolean(key("perapp"), false);
	}
	
	public void setIsPerApp(boolean is) {
		mPref.edit().putBoolean(key("perapp"), is).commit();
	}
	
	public boolean isBypassApp() {
		return mPref.getBoolean(key("appbypass"), false);
	}
	
	public void setIsBypassApp(boolean is) {
		mPref.edit().putBoolean(key("appbypass"), is).commit();
	}
	
	public String getAppList() {
		return mPref.getString(key("applist"), "");
	}
	
	public void setAppList(String list) {
		mPref.edit().putString(key("applist"), list).commit();
	}
	
	void delete() {
		mPref.edit()
			.remove(key("server"))
			.remove(key("port"))
			.remove(key("userpw"))
			.remove(key("username"))
			.remove(key("password"))
			.remove(key("route"))
			.remove(key("dns"))
			.remove(key("dns_port"))
			.remove(key("perapp"))
			.remove(key("appbypass"))
			.remove(key("applist"))
			.commit();
	}
	
	private String key(String k) {
		return mPrefix + k;
	}
	
	private static String prefPrefix(String name) {
		return name.replace("_", "__").replace(" ", "_");
	}
}
