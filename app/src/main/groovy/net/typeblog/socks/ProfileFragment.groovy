package net.typeblog.socks

import android.app.Activity
import android.app.AlertDialog
import android.content.ComponentName
import android.content.DialogInterface
import android.content.Intent
import android.content.ServiceConnection
import android.net.VpnService
import android.os.Bundle
import android.os.IBinder
import android.preference.CheckBoxPreference
import android.preference.EditTextPreference
import android.preference.Preference
import android.preference.PreferenceFragment
import android.preference.ListPreference
import android.text.InputType
import android.text.TextUtils
import android.view.Menu
import android.view.MenuItem
import android.view.MenuInflater
import android.widget.CompoundButton
import android.widget.EditText
import android.widget.Switch
import android.widget.Toast

import net.typeblog.socks.util.Profile
import net.typeblog.socks.util.ProfileManager
import net.typeblog.socks.util.Utility
import static net.typeblog.socks.util.Constants.*

public class ProfileFragment extends PreferenceFragment {
	private ProfileManager mManager
	private Profile mProfile
	
	private Switch mSwitch
	private boolean mRunning = false
	private boolean mStarting = false, mStopping = false
	private ServiceConnection mConnection = [
		onServiceConnected: { p1, binder ->
			mBinder = IVpnService.Stub.asInterface(binder)
			
			try {
				mRunning = mBinder.isRunning()
			} catch (Exception e) {
				
			}
			
			if (mRunning) {
				updateState()
			}
		},

		onServiceDisconnected: {
			mBinder = null
		}
	] as ServiceConnection
	private Runnable mStateRunnable = {
		updateState()
		mSwitch.postDelayed(mStateRunnable, 1000)
	} as Runnable
	private IVpnService mBinder
	
	private ListPreference mPrefProfile, mPrefRoutes
	private EditTextPreference mPrefServer, mPrefPort, mPrefUsername, mPrefPassword,
					mPrefDns, mPrefDnsPort, mPrefAppList, mPrefUDPGW
	private CheckBoxPreference mPrefUserpw, mPrefPerApp, mPrefAppBypass, mPrefIPv6, mPrefUDP, mPrefAuto
	
	@Override void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.settings)
		hasOptionsMenu = true
		mManager = ProfileManager.getInstance(activity.applicationContext)
		initPreferences()
		reload()
	}

	@Override void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater)
		inflater.inflate(R.menu.main, menu)
		
		MenuItem s = menu.findItem(R.id.switch_main);
		mSwitch = (Switch) s.getActionView().findViewById(R.id.switch_action_button)

		mSwitch.postDelayed(mStateRunnable, 1000)
		checkState();
	}

	@Override boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.prof_add:
				addProfile()
				return true
			case R.id.prof_del:
				removeProfile()
				return true
			default:
				return super.onOptionsItemSelected(item)
		}
	}

	boolean prefChange(Preference p, Object newValue) {
		switch (p) {
			case mPrefProfile:
				String name = newValue.toString()
				mProfile = mManager.getProfile(name)
				mManager.switchDefault(name)
				reload()
				return true
			case mPrefServer:
				mProfile.server = newValue.toString()
				resetTextN(mPrefServer, newValue)
				return true
			case mPrefPort:
				if (TextUtils.isEmpty(newValue.toString()))
					return false
			
				mProfile.port = newValue as int
				resetTextN(mPrefPort, newValue)
				return true
			case mPrefUserpw:
				mProfile.userPw = newValue as boolean
				return true
			case mPrefUsername:
				mProfile.username = newValue.toString()
				resetTextN(mPrefUsername, newValue)
				return true
			case mPrefPassword:
				mProfile.password = newValue.toString()
				resetTextN(mPrefPassword, newValue)
				return true
			case mPrefRoutes:
				mProfile.route = newValue.toString()
				resetListN(mPrefRoutes, newValue)
				return true
			case mPrefDns:
				mProfile.dns = newValue.toString()
				resetTextN(mPrefDns, newValue)
				return true
			case mPrefDnsPort:
				if (TextUtils.isEmpty(newValue.toString()))
					return false
			
				mProfile.dnsPort = newValue as int
				resetTextN(mPrefDnsPort, newValue)
				return true
			case mPrefPerApp:
				mProfile.perApp = newValue as boolean
				return true
			case mPrefAppBypass:
				mProfile.bypassApp = newValue as boolean
				return true
			case mPrefAppList:
				mProfile.appList = newValue.toString()
				return true
			case mPrefIPv6:
				mProfile.hasIPv6 = newValue as boolean
				return true
			case mPrefUDP:
				mProfile.hasUDP = newValue.toString()
				return true
			case mPrefUDPGW:
				mProfile.UDPGW = newValue.toString()
				resetTextN(mPrefUDPGW, newValue)
				return true
			case mPrefAuto:
				mProfile.autoConnect = newValue as boolean
				return true
			default:
				return false
		}
	}

	def checkedChanged(CompoundButton p1, boolean checked) {
		if (checked) {
			startVpn();
		} else {
			stopVpn();
		}
	}

	@Override void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data)
		
		if (resultCode == Activity.RESULT_OK) {
			Utility.startVpn(getActivity(), mProfile)
			checkState()
		}
	}
	
	private void initPreferences() {
		mPrefProfile = (ListPreference) findPreference(PREF_PROFILE);
		mPrefServer = (EditTextPreference) findPreference(PREF_SERVER_IP);
		mPrefPort = (EditTextPreference) findPreference(PREF_SERVER_PORT);
		mPrefUserpw = (CheckBoxPreference) findPreference(PREF_AUTH_USERPW);
		mPrefUsername = (EditTextPreference) findPreference(PREF_AUTH_USERNAME);
		mPrefPassword = (EditTextPreference) findPreference(PREF_AUTH_PASSWORD);
		mPrefRoutes = (ListPreference) findPreference(PREF_ADV_ROUTE);
		mPrefDns = (EditTextPreference) findPreference(PREF_ADV_DNS);
		mPrefDnsPort = (EditTextPreference) findPreference(PREF_ADV_DNS_PORT);
		mPrefPerApp = (CheckBoxPreference) findPreference(PREF_ADV_PER_APP);
		mPrefAppBypass = (CheckBoxPreference) findPreference(PREF_ADV_APP_BYPASS);
		mPrefAppList = (EditTextPreference) findPreference(PREF_ADV_APP_LIST);
		mPrefIPv6 = (CheckBoxPreference) findPreference(PREF_IPV6_PROXY);
		mPrefUDP = (CheckBoxPreference) findPreference(PREF_UDP_PROXY);
		mPrefUDPGW = (EditTextPreference) findPreference(PREF_UDP_GW);
		mPrefAuto = (CheckBoxPreference) findPreference(PREF_ADV_AUTO_CONNECT);
		
		[mPrefProfile, mPrefServer, mPrefPort, mPrefUserpw,
		mPrefUsername, mPrefPassword, mPrefRoutes, mPrefDns,
		mPrefDnsPort, mPrefPerApp, mPrefAppBypass, mPrefAppList,
		mPrefIPv6, mPrefUDP, mPrefUDPGW,
		mPrefAuto]*.onPreferenceChangeListener = [
			onPreferenceClick: { false },
			onPreferenceChange: this.&prefChange
		] as Preference.OnPreferenceChangeListener
	}
	
	private void reload() {
		if (mProfile == null) {
			mProfile = mManager.default
		}
		
		mPrefProfile.entries = mManager.profiles
		mPrefProfile.entryValues = mManager.profiles
		mPrefProfile.value = mProfile.name
		mPrefRoutes.value = mProfile.route
		resetList(mPrefProfile, mPrefRoutes)
		
		mPrefUserpw.checked = mProfile.userPw
		mPrefPerApp.checked = mProfile.perApp
		mPrefAppBypass.checked = mProfile.bypassApp
		mPrefIPv6.checked = mProfile.hasIPv6
		mPrefUDP.checked = mProfile.hasUDP
		mPrefAuto.checked = mProfile.autoConnect
		
		mPrefServer.text = mProfile.server
		mPrefPort.text = mProfile.port as String
		mPrefUsername.text = mProfile.username
		mPrefPassword.text = mProfile.password
		mPrefDns.text = mProfile.dns
		mPrefDnsPort.text = mProfile.dnsPort as String
		mPrefUDPGW.text = mProfile.UDPGW
		resetText(mPrefServer, mPrefPort, mPrefUsername, mPrefPassword, mPrefDns, mPrefDnsPort, mPrefUDPGW);
		
		mPrefAppList.text = mProfile.appList
	}
	
	private void resetList(ListPreference... pref) {
		pref.each {
			it.summary = it.entry
		}
	}
	
	private void resetListN(ListPreference pref, Object newValue) {
		pref.summary = newValue.toString()
	}
	
	private void resetText(EditTextPreference... pref) {
		pref.each {
			if ((it.editText.inputType & InputType.TYPE_TEXT_VARIATION_PASSWORD) != InputType.TYPE_TEXT_VARIATION_PASSWORD) {
				it.summary = it.text
			} else {
				if (it.text.length() > 0)
					it.summary = '*' * it.text.length()
				else
					it.summary = ""
			}
		}
	}
	
	private void resetTextN(EditTextPreference pref, Object newValue) {
		if ((pref.editText.inputType & InputType.TYPE_TEXT_VARIATION_PASSWORD) != InputType.TYPE_TEXT_VARIATION_PASSWORD) {
			pref.summary = newValue.toString()
		} else {
			String text = newValue.toString()
			if (text.length() > 0)
				pref.summary = '*' * text.length()
			else
				pref.summary = ""
		}
	}
	
	private void addProfile() {
		final EditText e = new EditText(activity)
		e.singleLine = true
		
		new AlertDialog.Builder(activity).with {
			title = R.string.prof_add
			view = e
			setPositiveButton android.R.string.ok, { d, which ->
				String name = e.text.toString().trim()

				if (!TextUtils.isEmpty(name)) {
					Profile p = mManager.addProfile(name)

					if (p) {
						mProfile = p
						reload()
						return
					}
				}

				Toast.makeText(getActivity(), 
					String.format(getString(R.string.err_add_prof), name),
					Toast.LENGTH_SHORT).show()
			}
			setNegativeButton android.R.string.cancel, { d, which ->

			}
			show()
		}
	}
	
	private void removeProfile() {
		new AlertDialog.Builder(activity).with {
			title = R.string.prof_del
			message = String.format(getString(R.string.prof_del_confirm), mProfile.name)
			setPositiveButton android.R.string.ok, { d, which ->
				if (!mManager.removeProfile(mProfile.name)) {
					Toast.makeText(activity,
						getString(R.string.err_del_prof, mProfile.name),
						Toast.LENGTH_SHORT).show()
				} else {
					mProfile = mManager.default
					reload()
				}
			}
			setNegativeButton android.R.string.cancel, { d, which ->
				
			}
			show()
		}
	}
	
	private void checkState() {
		mRunning = false;
		mSwitch.enabled = false
		mSwitch.onCheckedChangeListener = null
		
		if (mBinder == null) {
			activity.bindService(new Intent(activity, SocksVpnService.class), mConnection, 0);
		}
	}
	
	private void updateState() {
		if (mBinder == null) {
			mRunning = false;
		} else {
			try {
				mRunning = mBinder.isRunning()
			} catch (Exception e) {
				mRunning = false
			}
		}
		
		mSwitch.checked = mRunning
		
		if ((!mStarting && !mStopping) || (mStarting && mRunning) || (mStopping && !mRunning)) {
			mSwitch.enabled = true
		}
		
		if (mStarting && mRunning) {
			mStarting = false
		}
		
		if (mStopping && !mRunning) {
			mStopping = false
		}
		
		mSwitch.onCheckedChangeListener = this.&checkedChanged
	}
	
	private void startVpn() {
		mStarting = true
		Intent i = VpnService.prepare(activity)
		
		if (i != null) {
			startActivityForResult(i, 0)
		} else {
			onActivityResult(0, Activity.RESULT_OK, null)
		}
	}
	
	private void stopVpn() {
		if (mBinder == null)
			return
		
		mStopping = true
			
		try {
			mBinder.stop()
		} catch (any) {
			
		}
		
		mBinder = null
		
		activity.unbindService(mConnection)
		checkState()
	}
}
