package net.typeblog.socks.util

import android.content.Context
import android.content.SharedPreferences

import java.lang.ref.WeakReference

class ProfileFactory {
	private static ProfileFactory sInstance
	
	public static final ProfileFactory getInstance(Context context, SharedPreferences pref) {
		if (sInstance == null) {
			sInstance = new ProfileFactory(context, pref)
		}
		
		sInstance
	}
	
	private Context mContext
	private SharedPreferences mPref
	private Map<String, WeakReference<Profile>> mMap = [:]
	
	private ProfileFactory(Context context, SharedPreferences pref) {
		mContext = context
		mPref = pref
	}
	
	public Profile getProfile(String name) {
		WeakReference<Profile> p = mMap[name]
		
		if (!p || !p.get()) {
			p = new WeakReference<Profile>(new Profile(mContext, mPref, name))
			mMap[name] = p
		}
		
		p.get()
	}
}
