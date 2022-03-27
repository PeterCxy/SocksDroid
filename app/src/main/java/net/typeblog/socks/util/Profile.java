//package net.typeblog.socks.util;
//
//import android.content.Context;
//import android.content.SharedPreferences;
//
//public class Profile {
//    private Context mContext;
//    private SharedPreferences mPref;
//    private String mName;
//    private String mPrefix;
//
//    Profile(Context context, SharedPreferences pref, String name) {
//        mContext = context;
//        mPref = pref;
//        mName = name;
//        mPrefix = prefPrefix(name);
//    }
//
//    String getName() {
//        return mName;
//    }
//
//    String getServer() {
//        return   mPref.getString(key("server"), "127.0.0.1");
//    }
//
//    void setServer(String server) {
//        mPref.edit().putString(key("server"), server).commit();
//    }
//
//    int getPort() {
//        return mPref.getInt(key("port"), 1080);
//    }
//
//    void setPort(int port) {
//        mPref.edit().putInt(key("port"), port).commit();
//    }
//
//    boolean getUserPw() {
//        return mPref.getBoolean(key("userpw"), false);
//    }
//
//    void setUserPw(boolean is) {
//        mPref.edit().putBoolean(key("userpw"), is).commit();
//    }
//
//    String getUsername() {
//        return mPref.getString(key("username"), "");
//    }
//
//    void setUsername(String username) {
//        mPref.edit().putString(key("username"), username).commit();
//    }
//
//    String getPassword() {
//        return mPref.getString(key("password"), "");
//    }
//
//    void setPassword(String password) {
//         mPref.edit().putString(key("password"), password).commit();
//    }
//
//    String getRoute() {
//        return mPref.getString(key("route"), ROUTE_ALL);
//    }
//
//    void setRoute(String route) {
//        mPref.edit().putString(key("route"), route).commit();
//    }
//
//    String getDns() {
//        return mPref.getString(key("dns"), "8.8.8.8");
//    }
//
//    void setDns(String dns) {
//        mPref.edit().putString(key("dns"), dns).commit();
//    }
//
//    int getDnsPort() {
//        return mPref.getInt(key("dns_port"), 53);
//    }
//
//    void setDnsPort(int port) {
//        mPref.edit().putInt(key("dns_port"), port).commit();
//    }
//
//    boolean getPerApp() {
//        return mPref.getBoolean(key("perapp"), false);
//    }
//
//    void setPerApp(boolean is) {
//        mPref.edit().putBoolean(key("perapp"), is).commit();
//    }
//
//    boolean getBypassApp() {
//        return mPref.getBoolean(key("appbypass"), false);
//    }
//
//    void setBypassApp(boolean is) {
//        mPref.edit().putBoolean(key("appbypass"), is).commit();
//    }
//
//    String getAppList() {
//        return mPref.getString(key("applist"), "");
//    }
//
//    void setAppList(String list) {
//        mPref.edit().putString(key("applist"), list).commit();
//    }
//
//    boolean getHasIPv6() {
//        return mPref.getBoolean(key("ipv6"), false);
//    }
//
//    void setHasIPv6(boolean has) {
//        mPref.edit().putBoolean(key("ipv6"), has).commit();
//    }
//
//    boolean getHasUDP() {
//        return  mPref.getBoolean(key("udp"), false);
//    }
//
//    void setHasUDP(boolean has) {
//        mPref.edit().putBoolean(key("udp"), has).commit();
//    }
//
//    String getUDPGW() {
//        return  mPref.getString(key("udpgw"), "127.0.0.1:7300");
//    }
//
//    void setUDPGW(String gw) {
//        mPref.edit().putString(key("udpgw"), gw).commit();
//    }
//
//    boolean getAutoConnect() {
//        return  mPref.getBoolean(key("auto"), false);
//    }
//
//    void setAutoConnect(boolean a) {
//        mPref.edit().putBoolean(key("auto"), a).commit();
//    }
//
//    void delete() {
//        mPref.edit().remove("server").apply();
//        mPref.edit().remove("port").apply();
//        mPref.edit().remove("userpw").apply();
//        mPref.edit().remove("username").apply();
//        mPref.edit().remove("password").apply();
//        mPref.edit().remove("route").apply();
//        mPref.edit().remove("dns").apply();
//        mPref.edit().remove("dns_port").apply();
//        mPref.edit().remove("perapp").apply();
//        mPref.edit().remove("appbypass").apply();
//        mPref.edit().remove("applist").apply();
//        mPref.edit().remove("ipv6").apply();
//        mPref.edit().remove("udp").apply();
//        mPref.edit().remove("udpgw").apply();
//        mPref.edit().remove("auto").apply();
//    }
//
//    private String key(String k) {
//        return mPrefix + k;
//    }
//
//    private static String prefPrefix(String name) {
//        return name.replace("_", "__").replace(" ", "_");
//    }
//}