#!/usr/bin/env bash

set -eux

# Xcode_13.1.xip is needed to build the OSx cross compiler image from https://developer.apple.com/download/all/?q=xcode
# As an optimization for the rebuild times, the image is only built if the xip is present.
if [ -f Xcode_13.1.xip ]; then
    docker build --rm  --build-arg Xcodexip=Xcode_13.1.xip -f  cdt-infra-build-macos-sdk/Dockerfile -t cdt-infra-build-macos-sdk:latest .
else
    echo "MacOSX SDK & Toolchain build is being skipped"
fi 

docker build --rm -f cdt-infra/Dockerfile -t cdt-infra:latest .
docker build --rm -f cdt-infra-github/Dockerfile -t cdt-infra-github:latest .
docker build --rm -f cdt-infra-jipp/Dockerfile -t cdt-infra-jipp:latest .
