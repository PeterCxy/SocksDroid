package net.typeblog.socks

import android.app.Activity
import android.content.Intent
import android.net.VpnService
import android.os.Bundle

import net.typeblog.socks.util.Utility
import static net.typeblog.socks.util.Constants.*

public class MainActivity extends Activity {
    @Override void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		contentView = R.layout.main
		
		Utility.extractFile(this)
		
		fragmentManager.beginTransaction().replace(R.id.frame, new ProfileFragment()).commit()
	}
}
