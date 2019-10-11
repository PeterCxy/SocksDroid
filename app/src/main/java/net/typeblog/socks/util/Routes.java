package net.typeblog.socks.util;

import android.content.Context;
import android.net.VpnService;

import net.typeblog.socks.R;
import static net.typeblog.socks.util.Constants.*;

public class Routes {
    public static void addRoutes(Context context, VpnService.Builder builder, String name) {
        String[] routes;
        if(ROUTE_CHN.equals(name)) {
            routes = context.getResources().getStringArray(R.array.simple_route);
        } else {
            routes = new String[]{"0.0.0.0/0"};
        }

        for (String r : routes) {
            String[] cidr = r.split("/");

            // Cannot handle 127.0.0.0/8
            if (cidr.length == 2 && !cidr[0].startsWith("127")) {
                builder.addRoute(cidr[0], Integer.parseInt(cidr[1]));
            }
        }
    }
}
