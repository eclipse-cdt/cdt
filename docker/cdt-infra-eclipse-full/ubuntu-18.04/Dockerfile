FROM cdt-infra-base:ubuntu-18.04

USER root

RUN apt-get update && apt-get install -y --no-install-recommends \
      build-essential \
      git \
      gdb \
      libgmp-dev \
      libexpat1-dev \
      libipt1 \
      libmpfr-dev \
      libncurses-dev \
      ninja-build \
      pkg-config \
      python3-all-dev python3-pip python3-setuptools \
      zlib1g-dev \
      gdbserver \
      gcc-mingw-w64-x86-64 \
      g++-mingw-w64-x86-64 \
      gcc-aarch64-linux-gnu \
      g++-aarch64-linux-gnu \
      gcc-powerpc64le-linux-gnu \
      g++-powerpc64le-linux-gnu \
      clang \
      libxml2-utils \
      gettext-base \
      xserver-xephyr \
    && apt-get install -y llvm \
    && apt-get install -y --no-install-recommends texinfo bison flex \
    && rm -rf /var/lib/apt/lists/* \
    && pip3 install --upgrade pip

#Specifc CDT deps

#Meson
RUN pip3 install meson

# GDB pre-built in a different image
COPY --from=cdt-infra-all-gdbs:ubuntu-18.04 /shared/common/gdb/gdb-all /shared/common/gdb/gdb-all
ENV PATH="/shared/common/gdb/gdb-all/bin:${PATH}"

# Get pre-built MacOSX toolchain from other image (this also
# requires clang to be listed in the installs above)
COPY --from=cdt-infra-build-macos-sdk:ubuntu-18.04 /opt/osxcross/target /opt/osxcross/target
ENV PATH="/opt/osxcross/target/bin:${PATH}"

#Fix permissions for OpenShift & standard k8s
RUN chown -R 1000:0 ${HOME} \
  && chmod -R g+rwX ${HOME}


ENV USER_NAME vnc
USER 1000
WORKDIR ${HOME}

CMD ["/home/vnc/.vnc/xstartup.sh"]
