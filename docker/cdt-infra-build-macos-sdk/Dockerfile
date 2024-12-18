# This dockerfile is used to build the MacOSX SDK
FROM ubuntu:20.04

ENV DEBIAN_FRONTEND=noninteractive
RUN apt update
RUN apt install -y clang make build-essential libssl-dev lzma-dev liblzma-dev libxml2-dev git patch python3 cpio bzip2 xz-utils libbz2-dev curl

RUN curl -fsSL https://github.com/Kitware/CMake/releases/download/v3.29.2/cmake-3.29.2-linux-x86_64.tar.gz | tar xzf - -C /usr/share
ENV PATH="/usr/share/cmake-3.29.2-linux-x86_64/bin:${PATH}"

# Xcode_13.1.xip needs to be downloaded from
# https://developer.apple.com/download/all/?q=xcode - specically:
ARG Xcodexip=Xcode_13.1.xip
### Building osxcross is somewhat problematic because the versions
### are not fixed and reproducibility of making this docker file
### can be problematic. For example new versions of cmake, etc
### make be needed. TODO Is there a way to fix the version of 
### osxcross so that we can rebuild reliably?
WORKDIR /opt
COPY ${Xcodexip} /opt/${Xcodexip}
RUN git clone https://github.com/tpoechtrager/osxcross.git
RUN cd /opt/osxcross && ./tools/gen_sdk_package_pbzx.sh /opt/${Xcodexip}
RUN mv /opt/osxcross/*.xz /opt/osxcross/tarballs
ENV UNATTENDED=1 
RUN cd /opt/osxcross && ./build.sh

# Start next stage build
FROM ubuntu:20.04
COPY --from=0 /opt/osxcross/target /opt/osxcross/target
ENV PATH="/opt/osxcross/target/bin:${PATH}"
# At this point the osxcross toolchain is not usable,
# it needs to be copied into another container and that
# container needs clang & llvm installed too.
