package net.typeblog.socks.util

import android.content.Context
import android.content.SharedPreferences
import android.text.TextUtils

import net.typeblog.socks.R
import static net.typeblog.socks.util.Constants.*

public class ProfileManager {
	private static ProfileManager sInstance;
	
	public static final ProfileManager getInstance(Context context) {
		if (!sInstance) {
			sInstance = new ProfileManager(context)
		}
		
		return sInstance
	}
	
	private SharedPreferences mPref;
	private Context mContext;
	private ProfileFactory mFactory;
	private List<String> mProfiles = [];
	
	private ProfileManager(Context context) {
		mContext = context
		mPref = mContext.getSharedPreferences(PREF, Context.MODE_PRIVATE)
		mFactory = ProfileFactory.getInstance(mContext, mPref)
		reload()
	}
	
	public void reload() {
		mProfiles.clear()
		mProfiles << mContext.getString(R.string.prof_default)
		
		mPref.getString(PREF_PROFILE, "").split("\n").each {
			if (!TextUtils.isEmpty(it)) {
				mProfiles.add(it)
			}
		}
	}
	
	public String[] getProfiles() {
		mProfiles
	}
	
	public Profile getProfile(String name) {
		if (!mProfiles.contains(name)) {
			null
		} else {
			mFactory.getProfile(name)
		}
	}
	
	public Profile getDefault() {
		getProfile(mPref.getString(PREF_LAST_PROFILE, mProfiles[0]));
	}
	
	public void switchDefault(String name) {
		if (mProfiles.contains(name))
			mPref.edit().putString(PREF_LAST_PROFILE, name).commit()
	}
	
	public Profile addProfile(String name) {
		if (mProfiles.contains(name)) {
			return null
		} else {
			mProfiles << name
			mProfiles.remove(0);
			mPref.edit().putString(PREF_PROFILE, mProfiles.join('\n'))
				.putString(PREF_LAST_PROFILE, name).commit()
			reload()
			getDefault()
		}
	}
	
	public boolean removeProfile(String name) {
		if (name == mProfiles[0] || !mProfiles.contains(name)) {
			return false
		}
		
		getProfile(name).delete()
		
		mProfiles.with {
			remove 0
			remove name
		}
		
		mPref.edit().putString(PREF_PROFILE, mProfiles.join("\n"))
			.remove(PREF_LAST_PROFILE).commit()
		reload()
		
		true
	}
}
