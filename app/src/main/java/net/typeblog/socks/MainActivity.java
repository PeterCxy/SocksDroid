package net.typeblog.socks;

import android.app.Activity;
import android.os.Bundle;

public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.getFragmentManager().beginTransaction().replace(android.R.id.content, new ProfileFragment()).commit();
    }
}
