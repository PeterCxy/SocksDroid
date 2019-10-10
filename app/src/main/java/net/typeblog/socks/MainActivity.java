package net.typeblog.socks;

import android.app.Activity;
import android.content.Intent;
import android.net.VpnService;
import android.os.Bundle;

import net.typeblog.socks.util.Utility;
import static net.typeblog.socks.util.Constants.*;

public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		Utility.extractFile(this);
		
		getFragmentManager().beginTransaction().replace(R.id.frame, new ProfileFragment()).commit();
	}

	/*@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
		if (resultCode == RESULT_OK) {
			Intent i = new Intent(this, SocksVpnService.class)
				.putExtra(INTENT_NAME, "test")
				.putExtra(INTENT_SERVER, "127.0.0.1")
				.putExtra(INTENT_PORT, 2352)
				
				;
			
			startService(i);
		}
	}
	
	private void startVpn() {
		Intent i = VpnService.prepare(this);
		if (i != null) {
			startActivityForResult(i, 0);
		} else {
			onActivityResult(0, RESULT_OK, null);
		}
	}*/
}
