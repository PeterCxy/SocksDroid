package net.typeblog.socks.util;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.FileOutputStream;
import java.util.List;

import net.typeblog.socks.R;
import net.typeblog.socks.SocksVpnService;
import net.typeblog.socks.System;
import static net.typeblog.socks.BuildConfig.DEBUG;
import static net.typeblog.socks.util.Constants.*;

public class Utility {
    private static final String TAG = Utility.class.getSimpleName();

    public static void extractFile(Context context) {
        // Check app version
        SharedPreferences pref = context.getSharedPreferences("ver", Context.MODE_PRIVATE);

        int ver;
        try {
            ver = context.getPackageManager().getPackageInfo("net.typeblog.socks", 0).versionCode;
        } catch (Exception e) {
            throw new RuntimeException(e);
            //return;
        }

        if (pref.getInt("ver", -1) == ver) {
            return;
        }

        String target = context.getFilesDir().toString();

        if (DEBUG) {
            Log.d(TAG, "target = " + target);
        }

        if (new File(target + "/tun2socks").exists()) {
            if(!new File(target + "/tun2socks").delete())
                    Log.w(TAG, "failed to delete tun2socks");
        }

        if (new File(target + "/pdnsd").exists()) {
            if(!new File(target + "/pdnsd").delete())
                    Log.w(TAG, "failed to delete pdnsd");
        }

        if(!new File(target).mkdir())
            Log.w(TAG, "failed to create directory");

        String source = System.getABI();

        AssetManager m = context.getAssets();

        String[] files = null;
        try {
            files = m.list(source);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (files == null || files.length == 0) {
            return;
        }

        for (String f : files) {
            InputStream in;
            OutputStream out;

            try {
                in = m.open(source + "/" + f);
                out = new FileOutputStream(target + "/" + f);

                byte[] buf = new byte[512];
                int len;

                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }

                in.close();
                out.flush();
                out.close();

                exec(String.format("chmod 755 %s/%s", target, f));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        pref.edit().putInt("ver", ver).apply();

    }

    public static int exec(String cmd) {
        try {
            Process p = Runtime.getRuntime().exec(cmd);

            return p.waitFor();
        } catch (Exception e) {
            return -1;
        }
    }

    public static void killPidFile(String f) {
        File file = new File(f);

        if (!file.exists()) {
            return;
        }

        InputStream i;
        try {
            i = new FileInputStream(file);
        } catch (Exception e) {
            return;
        }

        byte[] buf = new byte[512];
        int len;
        StringBuilder str = new StringBuilder();

        try {
            while ((len = i.read(buf, 0, 512)) > 0) {
                str.append(new String(buf, 0, len));
            }
            i.close();
        } catch (Exception e) {
            return;
        }

        try {
            int pid = Integer.parseInt(str.toString().trim().replace("\n", ""));
            Runtime.getRuntime().exec("kill " + pid).waitFor();
            if(!file.delete())
                Log.w(TAG, "failed to delete pidfile");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String join(List<String> list, String separator) {
        if (list.isEmpty()) return "";

        StringBuilder ret = new StringBuilder();

        for (String s : list) {
            ret.append(s).append(separator);
        }

        return ret.substring(0, ret.length() - separator.length());
    }

    public static void makePdnsdConf(Context context, String dns, int port) {
        String conf = context.getString(R.string.pdnsd_conf)
                .replace("{IP}", dns).replace("{PORT}", Integer.toString(port));

        File f = new File(context.getFilesDir() + "/pdnsd.conf");

        if (f.exists()) {
            if(!f.delete())
                Log.w(TAG, "failed to delete pdnsd.conf");
        }

        try {
            OutputStream out = new FileOutputStream(f);
            out.write(conf.getBytes());
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        File cache = new File(context.getFilesDir() + "/pdnsd.cache");

        if (!cache.exists()) {
            try {
                if(!cache.createNewFile())
                    Log.w(TAG, "failed to create pdnsd.cache");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void startVpn(Context context, Profile profile) {
        Intent i = new Intent(context, SocksVpnService.class)
                .putExtra(INTENT_NAME, profile.getName())
                .putExtra(INTENT_SERVER, profile.getServer())
                .putExtra(INTENT_PORT, profile.getPort())
                .putExtra(INTENT_ROUTE, profile.getRoute())
                .putExtra(INTENT_DNS, profile.getDns())
                .putExtra(INTENT_DNS_PORT, profile.getDnsPort())
                .putExtra(INTENT_PER_APP, profile.isPerApp())
                .putExtra(INTENT_IPV6_PROXY, profile.hasIPv6());

        if (profile.isUserPw()) {
            i.putExtra(INTENT_USERNAME, profile.getUsername())
                    .putExtra(INTENT_PASSWORD, profile.getPassword());
        }

        if (profile.isPerApp()) {
            i.putExtra(INTENT_APP_BYPASS, profile.isBypassApp())
                    .putExtra(INTENT_APP_LIST, profile.getAppList().split("\n"));
        }

        if (profile.hasUDP()) {
            i.putExtra(INTENT_UDP_GW, profile.getUDPGW());
        }

        context.startService(i);
    }
}
