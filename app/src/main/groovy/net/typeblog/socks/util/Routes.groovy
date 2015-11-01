package net.typeblog.socks.util

import android.content.Context
import android.net.VpnService

import net.typeblog.socks.R
import static net.typeblog.socks.util.Constants.*

class Routes {
	static void addRoutes(Context context, VpnService.Builder builder, String name) {
		String[] routes = null;
		switch (name) {
			case ROUTE_ALL:
				routes = ["0.0.0.0/0"]
				break
			case ROUTE_CHN:
				routes = context.resources.getStringArray(R.array.simple_route)
				break
			default:
				routes = ["0.0.0.0/0"]
				break
		}
		
		routes.each {
			String[] cidr = it.split("/");
			
			// Cannot handle 127.0.0.0/8
			if (cidr.length == 2 && !cidr[0].startsWith("127")) {
				builder.addRoute(cidr[0], cidr[1].toInteger());
			}
		}
	}
}
