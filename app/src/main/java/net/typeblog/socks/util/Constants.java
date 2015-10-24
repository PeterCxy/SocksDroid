package net.typeblog.socks.util;

public class Constants
{
	public static final String DIR = "/data/data/net.typeblog.socks/files";
	
	public static final String ABI_DEFAULT = "armeabi-v7a";
	
	public static final String ROUTE_ALL = "all",
							ROUTE_CHN = "chn";
	
	public static final String INTENT_PREFIX = "SOCKS",
							INTENT_NAME = INTENT_PREFIX + "NAME",
							INTENT_SERVER = INTENT_PREFIX + "SERV",
							INTENT_PORT = INTENT_PREFIX + "PORT",
							INTENT_USERNAME = INTENT_PREFIX + "UNAME",
							INTENT_PASSWORD = INTENT_PREFIX + "PASSWD",
							INTENT_ROUTE = INTENT_PREFIX + "ROUTE";
	
	public static final String PREF = "profile",
							PREF_PROFILE = "profile",
							PREF_LAST_PROFILE = "last_profile",
							PREF_SERVER_IP = "server_ip",
							PREF_SERVER_PORT = "server_port",
							PREF_AUTH_USERPW = "auth_userpw",
							PREF_AUTH_USERNAME = "auth_username",
							PREF_AUTH_PASSWORD = "auth_password",
							PREF_ADV_ROUTE = "adv_route";
}
