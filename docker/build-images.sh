#!/usr/bin/env bash

set -eux

# Xcode_13.1.xip is needed to build the OSx cross compiler image from https://developer.apple.com/download/all/?q=xcode
# As an optimization for the rebuild times, the image is only built if the xip is present.
if [ -f Xcode_13.1.xip ]; then
    docker build --rm  --build-arg Xcodexip=Xcode_13.1.xip -f  cdt-infra-build-macos-sdk/ubuntu-18.04/Dockerfile -t cdt-infra-build-macos-sdk:ubuntu-18.04 .
else
    echo "MacOSX SDK & Toolchain build is being skipped"
fi 

docker build --rm -f cdt-infra-base/ubuntu-18.04/Dockerfile -t cdt-infra-base:ubuntu-18.04 .
docker build --rm -f cdt-infra-all-gdbs/ubuntu-18.04/Dockerfile -t cdt-infra-all-gdbs:ubuntu-18.04 .
docker build --rm -f cdt-infra-eclipse-full/ubuntu-18.04/Dockerfile -t cdt-infra-eclipse-full:ubuntu-18.04 .
docker build --rm -f cdt-infra-plus-eclipse-install/ubuntu-18.04/Dockerfile -t cdt-infra-plus-eclipse-install:ubuntu-18.04 .
docker build --rm -f cdt-infra-plus-eclipse-install-github/ubuntu-18.04/Dockerfile -t cdt-infra-plus-eclipse-install-github:ubuntu-18.04 .
