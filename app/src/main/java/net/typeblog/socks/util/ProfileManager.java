package net.typeblog.socks.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;

import net.typeblog.socks.R;
import static net.typeblog.socks.util.Constants.*;

public class ProfileManager {
	private static ProfileManager sInstance;
	
	public static final ProfileManager getInstance(Context context) {
		if (sInstance == null) {
			sInstance = new ProfileManager(context);
		}
		
		return sInstance;
	}
	
	private SharedPreferences mPref;
	private Context mContext;
	private ProfileFactory mFactory;
	private List<String> mProfiles = new ArrayList<>();
	
	private ProfileManager(Context context) {
		mContext = context;
		mPref = mContext.getSharedPreferences(PREF, Context.MODE_PRIVATE);
		mFactory = ProfileFactory.getInstance(mContext, mPref);
		reload();
	}
	
	public void reload() {
		mProfiles.clear();
		mProfiles.add(mContext.getString(R.string.prof_default));
		
		String[] profiles = mPref.getString(PREF_PROFILE, "").split("\n");
		
		for (String p : profiles) {
			if (!TextUtils.isEmpty(p)) {
				mProfiles.add(p);
			}
		}
	}
	
	public String[] getProfiles() {
		return mProfiles.toArray(new String[mProfiles.size()]);
	}
	
	public Profile getProfile(String name) {
		if (!mProfiles.contains(name)) {
			return null;
		} else {
			return mFactory.getProfile(name);
		}
	}
	
	public Profile getDefault() {
		return getProfile(mPref.getString(PREF_LAST_PROFILE, mProfiles.get(0)));
	}
	
	public void switchDefault(String name) {
		if (mProfiles.contains(name))
			mPref.edit().putString(PREF_LAST_PROFILE, name).commit();
	}
	
	public Profile addProfile(String name) {
		if (mProfiles.contains(name)) {
			return null;
		} else {
			mProfiles.add(name);
			mProfiles.remove(0);
			mPref.edit().putString(PREF_PROFILE, Utility.join(mProfiles, "\n"))
				.putString(PREF_LAST_PROFILE, name).commit();
			reload();
			return getDefault();
		}
	}
	
	public boolean removeProfile(String name) {
		if (name == mProfiles.get(0) || !mProfiles.contains(name)) {
			return false;
		}
		
		getProfile(name).delete();
		
		mProfiles.remove(0);
		mProfiles.remove(name);
		
		mPref.edit().putString(PREF_PROFILE, Utility.join(mProfiles, "\n"))
			.remove(PREF_LAST_PROFILE).commit();
		reload();
		
		return true;
	}
}
