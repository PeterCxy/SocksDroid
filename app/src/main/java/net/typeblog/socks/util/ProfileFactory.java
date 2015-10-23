package net.typeblog.socks.util;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.HashMap;
import java.util.Map;
import java.lang.ref.WeakReference;

class ProfileFactory
{
	private static ProfileFactory sInstance;
	
	public static final ProfileFactory getInstance(Context context, SharedPreferences pref) {
		if (sInstance == null) {
			sInstance = new ProfileFactory(context, pref);
		}
		
		return sInstance;
	}
	
	private Context mContext;
	private SharedPreferences mPref;
	private Map<String, WeakReference<Profile>> mMap = new HashMap<>();
	
	private ProfileFactory(Context context, SharedPreferences pref) {
		mContext = context;
		mPref = pref;
	}
	
	public Profile getProfile(String name) {
		WeakReference<Profile> p = mMap.get(name);
		
		if (p == null || p.get() == null) {
			p = new WeakReference<Profile>(new Profile(mContext, mPref, name));
			mMap.put(name, p);
		}
		
		return p.get();
	}
}
