#!/bin/bash

rm -rf assets
rm -rf ../../libs

ndk-build

mkdir -p assets/armeabi-v7a
cp libs/armeabi-v7a/{tun2socks,pdnsd} assets/armeabi-v7a/
rm -rf libs/*/{tun2socks,pdnsd}
mv libs ../../
