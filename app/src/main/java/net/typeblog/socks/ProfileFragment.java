package net.typeblog.socks;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.VpnService;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.ListPreference;
import android.text.InputType;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuInflater;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import net.typeblog.socks.util.Profile;
import net.typeblog.socks.util.ProfileManager;
import static net.typeblog.socks.util.Constants.*;

public class ProfileFragment extends PreferenceFragment implements Preference.OnPreferenceClickListener, Preference.OnPreferenceChangeListener,
						CompoundButton.OnCheckedChangeListener {
	private ProfileManager mManager;
	private Profile mProfile;
	
	private Switch mSwitch;
	private boolean mRunning = false;
	private ServiceConnection mConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName p1, IBinder binder) {
			mRunning = ((SocksVpnService.VpnBinder) binder).isRunning();
			mBinder = (SocksVpnService.VpnBinder) binder;
			updateState();
		}

		@Override
		public void onServiceDisconnected(ComponentName p1) {
			mBinder = null;
		}
	};
	private Runnable mStateRunnable = new Runnable() {
		@Override
		public void run() {
			updateState();
		}
	};
	private SocksVpnService.VpnBinder mBinder;
	
	private ListPreference mPrefProfile, mPrefRoutes;
	private EditTextPreference mPrefServer, mPrefPort, mPrefUsername, mPrefPassword,
					mPrefDns, mPrefDnsPort, mPrefAppList, mPrefUDPGW;
	private CheckBoxPreference mPrefUserpw, mPrefPerApp, mPrefAppBypass, mPrefIPv6, mPrefUDP;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.settings);
		setHasOptionsMenu(true);
		mManager = ProfileManager.getInstance(getActivity().getApplicationContext());
		initPreferences();
		reload();
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.main, menu);
		
		MenuItem s = menu.findItem(R.id.switch_main);
		mSwitch = (Switch) s.getActionView().findViewById(R.id.switch_action_button);
		mSwitch.setOnCheckedChangeListener(this);
		checkState();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.prof_add:
				addProfile();
				return true;
			case R.id.prof_del:
				removeProfile();
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public boolean onPreferenceClick(Preference p) {
		// TODO: Implement this method
		return false;
	}

	@Override
	public boolean onPreferenceChange(Preference p, Object newValue) {
		if (p == mPrefProfile) {
			String name = newValue.toString();
			mProfile = mManager.getProfile(name);
			mManager.switchDefault(name);
			reload();
			return true;
		} else if (p == mPrefServer) {
			mProfile.setServer(newValue.toString());
			resetTextN(mPrefServer, newValue);
			return true;
		} else if (p == mPrefPort) {
			if (TextUtils.isEmpty(newValue.toString()))
				return false;
			
			mProfile.setPort(Integer.parseInt(newValue.toString()));
			resetTextN(mPrefPort, newValue);
			return true;
		} else if (p == mPrefUserpw) {
			mProfile.setIsUserpw(Boolean.parseBoolean(newValue.toString()));
			return true;
		} else if (p == mPrefUsername) {
			mProfile.setUsername(newValue.toString());
			resetTextN(mPrefUsername, newValue);
			return true;
		} else if (p == mPrefPassword) {
			mProfile.setPassword(newValue.toString());
			resetTextN(mPrefPassword, newValue);
			return true;
		} else if (p == mPrefRoutes) {
			mProfile.setRoute(newValue.toString());
			resetListN(mPrefRoutes, newValue);
			return true;
		} else if (p == mPrefDns) {
			mProfile.setDns(newValue.toString());
			resetTextN(mPrefDns, newValue);
			return true;
		} else if (p == mPrefDnsPort) {
			if (TextUtils.isEmpty(newValue.toString()))
				return false;
			
			mProfile.setDnsPort(Integer.valueOf(newValue.toString()));
			resetTextN(mPrefDnsPort, newValue);
			return true;
		} else if (p == mPrefPerApp) {
			mProfile.setIsPerApp(Boolean.parseBoolean(newValue.toString()));
			return true;
		} else if (p == mPrefAppBypass) {
			mProfile.setIsBypassApp(Boolean.parseBoolean(newValue.toString()));
			return true;
		} else if (p == mPrefAppList) {
			mProfile.setAppList(newValue.toString());
			return true;
		} else if (p == mPrefIPv6) {
			mProfile.setHasIPv6(Boolean.parseBoolean(newValue.toString()));
			return true;
		} else if (p == mPrefUDP) {
			mProfile.setHasUDP(Boolean.parseBoolean(newValue.toString()));
			return true;
		} else if (p == mPrefUDPGW) {
			mProfile.setUDPGW(newValue.toString());
			resetTextN(mPrefUDPGW, newValue);
			return true;
		} else {
			return false;
		}
	}

	@Override
	public void onCheckedChanged(CompoundButton p1, boolean checked) {
		if (checked) {
			startVpn();
		} else {
			stopVpn();
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
		if (resultCode == Activity.RESULT_OK) {
			Intent i = new Intent(getActivity(), SocksVpnService.class)
				.putExtra(INTENT_NAME, mProfile.getName())
				.putExtra(INTENT_SERVER, mProfile.getServer())
				.putExtra(INTENT_PORT, mProfile.getPort())
				.putExtra(INTENT_ROUTE, mProfile.getRoute())
				.putExtra(INTENT_DNS, mProfile.getDns())
				.putExtra(INTENT_DNS_PORT, mProfile.getDnsPort())
				.putExtra(INTENT_PER_APP, mProfile.isPerApp())
				.putExtra(INTENT_IPV6_PROXY, mProfile.hasIPv6());
			
			if (mProfile.isUserPw()) {
				i.putExtra(INTENT_USERNAME, mProfile.getUsername())
					.putExtra(INTENT_PASSWORD, mProfile.getPassword());
			}
			
			if (mProfile.isPerApp()) {
				i.putExtra(INTENT_APP_BYPASS, mProfile.isBypassApp())
					.putExtra(INTENT_APP_LIST, mProfile.getAppList().split("\n"));
			}
			
			if (mProfile.hasUDP()) {
				i.putExtra(INTENT_UDP_GW, mProfile.getUDPGW());
			}
			
			getActivity().startService(i);
			
			checkState();
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
		
		mPrefProfile.setOnPreferenceChangeListener(this);
		mPrefServer.setOnPreferenceChangeListener(this);
		mPrefPort.setOnPreferenceChangeListener(this);
		mPrefUserpw.setOnPreferenceChangeListener(this);
		mPrefUsername.setOnPreferenceChangeListener(this);
		mPrefPassword.setOnPreferenceChangeListener(this);
		mPrefRoutes.setOnPreferenceChangeListener(this);
		mPrefDns.setOnPreferenceChangeListener(this);
		mPrefDnsPort.setOnPreferenceChangeListener(this);
		mPrefPerApp.setOnPreferenceChangeListener(this);
		mPrefAppBypass.setOnPreferenceChangeListener(this);
		mPrefAppList.setOnPreferenceChangeListener(this);
		mPrefIPv6.setOnPreferenceChangeListener(this);
		mPrefUDP.setOnPreferenceChangeListener(this);
		mPrefUDPGW.setOnPreferenceChangeListener(this);
	}
	
	private void reload() {
		if (mProfile == null) {
			mProfile = mManager.getDefault();
		}
		
		mPrefProfile.setEntries(mManager.getProfiles());
		mPrefProfile.setEntryValues(mManager.getProfiles());
		mPrefProfile.setValue(mProfile.getName());
		mPrefRoutes.setValue(mProfile.getRoute());
		resetList(mPrefProfile, mPrefRoutes);
		
		mPrefUserpw.setChecked(mProfile.isUserPw());
		mPrefPerApp.setChecked(mProfile.isPerApp());
		mPrefAppBypass.setChecked(mProfile.isBypassApp());
		mPrefIPv6.setChecked(mProfile.hasIPv6());
		mPrefUDP.setChecked(mProfile.hasUDP());
		
		mPrefServer.setText(mProfile.getServer());
		mPrefPort.setText(String.valueOf(mProfile.getPort()));
		mPrefUsername.setText(mProfile.getUsername());
		mPrefPassword.setText(mProfile.getPassword());
		mPrefDns.setText(mProfile.getDns());
		mPrefDnsPort.setText(String.valueOf(mProfile.getDnsPort()));
		mPrefUDPGW.setText(mProfile.getUDPGW());
		resetText(mPrefServer, mPrefPort, mPrefUsername, mPrefPassword, mPrefDns, mPrefDnsPort, mPrefUDPGW);
		
		mPrefAppList.setText(mProfile.getAppList());
	}
	
	private void resetList(ListPreference... pref) {
		for (ListPreference p : pref)
			p.setSummary(p.getEntry());
	}
	
	private void resetListN(ListPreference pref, Object newValue) {
		pref.setSummary(newValue.toString());
	}
	
	private void resetText(EditTextPreference... pref) {
		for (EditTextPreference p : pref) {
			if ((p.getEditText().getInputType() & InputType.TYPE_TEXT_VARIATION_PASSWORD) != InputType.TYPE_TEXT_VARIATION_PASSWORD) {
				p.setSummary(p.getText());
			} else {
				if (p.getText().length() > 0)
					p.setSummary(String.format(String.format("%%0%dd", p.getText().length()), 0).replace("0", "*"));
				else
					p.setSummary("");
			}
		}
	}
	
	private void resetTextN(EditTextPreference pref, Object newValue) {
		if ((pref.getEditText().getInputType() & InputType.TYPE_TEXT_VARIATION_PASSWORD) != InputType.TYPE_TEXT_VARIATION_PASSWORD) {
			pref.setSummary(newValue.toString());
		} else {
			String text = newValue.toString();
			if (text.length() > 0)
				pref.setSummary(String.format(String.format("%%0%dd", text.length()), 0).replace("0", "*"));
			else
				pref.setSummary("");
		}
	}
	
	private void addProfile() {
		final EditText e = new EditText(getActivity());
		e.setSingleLine(true);
		
		new AlertDialog.Builder(getActivity())
			.setTitle(R.string.prof_add)
			.setView(e)
			.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface d, int which) {
					String name = e.getText().toString().trim();
					
					if (!TextUtils.isEmpty(name)) {
						Profile p = mManager.addProfile(name);
						
						if (p != null) {
							mProfile = p;
							reload();
							return;
						}
					}
					
					Toast.makeText(getActivity(), 
						String.format(getString(R.string.err_add_prof), name),
						Toast.LENGTH_SHORT).show();
				}
			})
			.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface d, int which) {
					
				}
			})
			.create().show();
	}
	
	private void removeProfile() {
		new AlertDialog.Builder(getActivity())
			.setTitle(R.string.prof_del)
			.setMessage(String.format(getString(R.string.prof_del_confirm), mProfile.getName()))
			.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface d, int which) {
					if (!mManager.removeProfile(mProfile.getName())) {
						Toast.makeText(getActivity(),
							getString(R.string.err_del_prof, mProfile.getName()),
							Toast.LENGTH_SHORT).show();
					} else {
						mProfile = mManager.getDefault();
						reload();
					}
				}
			})
			.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface d, int which) {

				}
			})
			.create().show();
	}
	
	private void checkState() {
		mRunning = false;
		mSwitch.setEnabled(false);
		mSwitch.setOnCheckedChangeListener(null);
		getActivity().bindService(new Intent(getActivity(), SocksVpnService.class), mConnection, 0);
		
		// Wait for 3 secs
		mSwitch.postDelayed(mStateRunnable, 3000);
	}
	
	private void updateState() {
		mSwitch.setChecked(mRunning);
		mSwitch.setEnabled(true);
		mSwitch.setOnCheckedChangeListener(ProfileFragment.this);
		mSwitch.removeCallbacks(mStateRunnable);
	}
	
	private void startVpn() {
		Intent i = VpnService.prepare(getActivity());
		
		if (i != null) {
			startActivityForResult(i, 0);
		} else {
			onActivityResult(0, Activity.RESULT_OK, null);
		}
	}
	
	private void stopVpn() {
		if (mBinder == null)
			return;
		
		mBinder.stop();
		getActivity().unbindService(mConnection);
		checkState();
	}
}
