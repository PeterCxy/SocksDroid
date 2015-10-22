#!/bin/bash

rm -rf assets
rm -rf ../../libs

ndk-build

mkdir -p assets/armeabi-v7a
cp libs/armeabi-v7a/tun2socks assets/armeabi-v7a/tun2socks
rm -rf libs/*/tun2socks
mv libs ../../
