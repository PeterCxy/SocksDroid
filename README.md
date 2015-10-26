SocksDroid
---
SOCKS5 client for Android 5.0+ making use of the `VpnService` API and `tun2socks` so that it works without root permission (unlike ProxyDroid).

Most of the JNI code are imported from `shadowsocks-android` project because they have already done most of the work.

### THIS IS NOT A SHADOWSOCKS CLIENT! SOCKS5 IS NOT SHADOWSOCKS!

UDP Forwarding
---
As `tun2socks` does not support UDP associate but has its own implementation of UDP forwarding `badvpn-udpgw`, so it is needed that the udpgw daemon run on remote server to use UDP forwarding.

On remote server

```
badvpn-udpgw --listen-addr 127.0.0.1:7300
```

And set `UDP Gateway` in this app to `127.0.0.1:7300`

DNS
---
If the server does not run `udpgw`, DNS lookups can also be processed in this app.

It makes use of the TCP DNS feature of `pdnsd`. You just set a DNS server that supports TCP DNS in this app, and all DNS requests will be transformed into TCP queries.

Routing
---
The app has an embedded list of non-Chinese IPs. Chinese users can make use of it for best experience in bypassing GFW.

GFW
---
Note that SOCKS5 is currently blocked by the GFW, which means Chinese users cannot connect to any SOCKS5 servers outside China.

But there are still solutions. For example, use `stunnel` to wrap the SOCKS5 connection with SSL. See my project stunnel-android for usage on Android.

License
---
This project is licensed under GNU General Public License Version 3 or later.
