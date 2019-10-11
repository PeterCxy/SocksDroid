package net.typeblog.socks;

import android.app.Activity;
import android.os.Bundle;

import net.typeblog.socks.util.Utility;

public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Utility.extractFile(this);

        this.getFragmentManager().beginTransaction().replace(android.R.id.content, new ProfileFragment()).commit();
    }
}
