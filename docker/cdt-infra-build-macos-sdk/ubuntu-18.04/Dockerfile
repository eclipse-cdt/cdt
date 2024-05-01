# This dockerfile is used to build the MacOSX SDK
FROM ubuntu:18.04

RUN apt-get update
RUN apt-get install -y clang make build-essential
RUN apt-get install -y libssl-dev lzma-dev liblzma-dev libxml2-dev git patch python cpio bzip2 xz-utils libbz2-dev

# cmake in Ubuntu is too old to build osxcross, therefore
# use this version
RUN apt-get install -y curl
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
FROM ubuntu:18.04
COPY --from=0 /opt/osxcross/target /opt/osxcross/target
ENV PATH="/opt/osxcross/target/bin:${PATH}"
# At this point the osxcross toolchain is not usable,
# it needs to be copied into another container and that
# container needs clang & llvm installed too.
