#!/bin/bash

rm -rf jniLibs

ndk-build

for p in armeabi-v7a arm64-v8a x86 x86_64; do
	mv libs/$p/{tun2socks,libtun2socks.so}
	mv libs/$p/{pdnsd,libpdnsd.so}
done

mv libs jniLibs
