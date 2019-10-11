#!/bin/bash

rm -rf assets
rm -rf jniLibs

ndk-build

for p in armeabi-v7a arm64-v8a x86 x86_64; do
	mkdir -p assets/$p
	cp libs/$p/{tun2socks,pdnsd} assets/$p/
done

rm -rf libs/*/{tun2socks,pdnsd}
mv libs jniLibs
