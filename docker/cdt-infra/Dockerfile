#-----------------------------------------------
# This first part of the file is derived from:
# - https://github.com/eclipse-cbi/dockerfiles/blob/74dda784b6cf294012a3bf0d66221a1af480a83c/gtk3-wm/ubuntu-metacity/Dockerfile
# Consult https://github.com/eclipse-cbi/dockerfiles for possible updates
FROM ubuntu:20.04

ENV DEBIAN_FRONTEND=noninteractive
RUN apt-get update && apt-get install -y --no-install-recommends \
      libgtk-3-0 \
      tigervnc-standalone-server \
      tigervnc-common \
      tightvncserver \
      metacity \
      x11-xserver-utils \
      libgl1-mesa-dri \
      xfonts-base \
      xfonts-scalable \
      xfonts-100dpi \
      xfonts-75dpi \
      fonts-liberation \
      fonts-liberation2 \
      fonts-freefont-ttf \
      fonts-dejavu \
      fonts-dejavu-core \
      fonts-dejavu-extra \
    && rm -rf /var/lib/apt/lists/*

ENV HOME=/home/vnc
ENV DISPLAY :0

RUN mkdir -p ${HOME}/.vnc && chmod -R 775 ${HOME} \
  && echo "123456" | vncpasswd -f > ${HOME}/.vnc/passwd \
  && chmod 600 ${HOME}/.vnc/passwd

# Create a custom vnc xstartup file
COPY scripts/xstartup_metacity.sh ${HOME}/.vnc/xstartup.sh
RUN chmod 755 ${HOME}/.vnc/xstartup.sh




RUN apt-get update && apt-get install -y --no-install-recommends \
    ca-certificates \
    curl \
    openssh-client \
    wget \
    zip \
    unzip \
    gnupg \
    locales \
    libxtst6 \
    build-essential \
    git \
    gdb \
    libgmp-dev \
    libexpat1-dev \
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
    gcc-riscv64-linux-gnu \
    g++-riscv64-linux-gnu \
    clang \
    libxml2-utils \
    gettext-base \
    xserver-xephyr \
    clang-format \
    xvfb \
    ssh-askpass \
    llvm \
    texinfo bison flex \
    meson \
  && rm -rf /var/lib/apt/lists/*

#-----------------------------------------------
# This is the end of the upstream one from eclipse-cbi
# the rest of this docker file is everything needed to
# build and test CDT on Jenkins and GitHUb
#-----------------------------------------------

RUN locale-gen en_US.UTF-8
# Need locale to be UTF-8
ENV LANG=en_US.UTF-8
ENV LANGUAGE=en_US:en
ENV LC_ALL=en_US.UTF-8


# Get release versions from Adoptium API: https://api.adoptium.net/q/swagger-ui/#/Release%20Info/getReleaseNames
# Update toolchains.xml too when updating this
ENV JDK17_VERSION=jdk-17.0.13+11
ENV JDK21_VERSION=jdk-21.0.5+11
ENV JAVA17_HOME=/usr/lib/jvm/${JDK17_VERSION}
ENV JAVA21_HOME=/usr/lib/jvm/${JDK21_VERSION}

# Get Java from adoptium + header files for win32 & mac for cross compiling JNI libraries on Java 8, 11, 17
# TODO: Move these runs into a script, the only difference between each invocation is the env variables
RUN export JVERSION=${JDK17_VERSION} \
    && export JHOME=${JAVA17_HOME} \
    && mkdir /tmp/x && mkdir -p /usr/lib/jvm \
    && curl -sL "https://api.adoptium.net/v3/binary/version/${JVERSION}/linux/x64/jdk/hotspot/normal/eclipse?project=jdk" > /tmp/x/openjdk-linux.tar.gz \
    && mkdir /tmp/x/linux && cd /tmp/x/linux && tar xvf /tmp/x/openjdk-linux.tar.gz \
    && mv /tmp/x/linux/${JVERSION} /usr/lib/jvm \
    && curl -sL "https://api.adoptium.net/v3/binary/version/${JVERSION}/windows/x64/jdk/hotspot/normal/eclipse?project=jdk" > /tmp/x/openjdk-win32.zip \
    && curl -sL "https://api.adoptium.net/v3/binary/version/${JVERSION}/mac/x64/jdk/hotspot/normal/eclipse?project=jdk" > /tmp/x/openjdk-mac.tar.gz \
    && mkdir /tmp/x/win32 && cd /tmp/x/win32 && unzip /tmp/x/openjdk-win32.zip \
    && mkdir /tmp/x/mac && cd /tmp/x/mac && tar xvf /tmp/x/openjdk-mac.tar.gz \
    && cd $JHOME/include && for i in *.h; do diff -w $i /tmp/x/win32/${JVERSION}/include/$i ; done \
    && cd $JHOME/include && for i in *.h; do diff -w $i /tmp/x/mac/${JVERSION}/Contents/Home/include/$i ; done \
    && mv /tmp/x/win32/${JVERSION}/include/win32 $JHOME/include \
    && mv /tmp/x/mac/${JVERSION}/Contents/Home/include/darwin $JHOME/include \
    && find $JHOME/include \
    && rm -rf /tmp/x
  RUN export JVERSION=${JDK21_VERSION} \
    && export JHOME=${JAVA21_HOME} \
    && mkdir /tmp/x && mkdir -p /usr/lib/jvm \
    && curl -sL "https://api.adoptium.net/v3/binary/version/${JVERSION}/linux/x64/jdk/hotspot/normal/eclipse?project=jdk" > /tmp/x/openjdk-linux.tar.gz \
    && mkdir /tmp/x/linux && cd /tmp/x/linux && tar xvf /tmp/x/openjdk-linux.tar.gz \
    && mv /tmp/x/linux/${JVERSION} /usr/lib/jvm \
    && curl -sL "https://api.adoptium.net/v3/binary/version/${JVERSION}/windows/x64/jdk/hotspot/normal/eclipse?project=jdk" > /tmp/x/openjdk-win32.zip \
    && curl -sL "https://api.adoptium.net/v3/binary/version/${JVERSION}/mac/x64/jdk/hotspot/normal/eclipse?project=jdk" > /tmp/x/openjdk-mac.tar.gz \
    && mkdir /tmp/x/win32 && cd /tmp/x/win32 && unzip /tmp/x/openjdk-win32.zip \
    && mkdir /tmp/x/mac && cd /tmp/x/mac && tar xvf /tmp/x/openjdk-mac.tar.gz \
    && cd $JHOME/include && for i in *.h; do diff -w $i /tmp/x/win32/${JVERSION}/include/$i ; done \
    && cd $JHOME/include && for i in *.h; do diff -w $i /tmp/x/mac/${JVERSION}/Contents/Home/include/$i ; done \
    && mv /tmp/x/win32/${JVERSION}/include/win32 $JHOME/include \
    && mv /tmp/x/mac/${JVERSION}/Contents/Home/include/darwin $JHOME/include \
    && find $JHOME/include \
    && rm -rf /tmp/x

# Default to JAVA21 being in use
ENV PATH="${JAVA21_HOME}/bin:${PATH}"
ENV JAVA_HOME=${JAVA21_HOME}

# Maven - Note that this is the default, but the Jenkinsfile may override it with JIPP version
ENV MAVEN_VERSION=3.9.9
RUN curl -fsSL https://archive.apache.org/dist/maven/maven-3/$MAVEN_VERSION/binaries/apache-maven-$MAVEN_VERSION-bin.tar.gz | tar xzf - -C /usr/share \
  && mv /usr/share/apache-maven-$MAVEN_VERSION /usr/share/maven \
  && ln -s /usr/share/maven/bin/mvn /usr/bin/mvn
ENV MAVEN_HOME=/usr/share/maven
RUN mkdir -p ${HOME}/.m2
COPY toolchains.xml ${HOME}/.m2

# Get pre-built MacOSX toolchain from other image (this also
# requires clang to be listed in the installs above)
COPY --from=cdt-infra-build-macos-sdk:latest /opt/osxcross/target /opt/osxcross/target
ENV PATH="/opt/osxcross/target/bin:${PATH}"

# Get an Eclipse install for running code formatting checks
RUN mkdir -p ${HOME}/buildtools && cd ${HOME}/buildtools \
  && curl -sL https://download.eclipse.org/eclipse/downloads/drops4/R-4.34-202411201800/eclipse-SDK-4.34-linux-gtk-x86_64.tar.gz | tar xvz \
  && mv eclipse eclipse-SDK-4.34

# Get WoA toolchain https://github.com/eclipse-cdt/cdt/pull/971
# Note that we remove the "fake" gcc and g++ provided here as they
# conflict with those provided by package gcc-mingw-w64-x86-64
# TODO change to install this from some future package called probably gcc-mingw-w64-aarch64
RUN curl -fsSL https://github.com/mstorsjo/llvm-mingw/releases/download/20241203/llvm-mingw-20241203-ucrt-ubuntu-20.04-x86_64.tar.xz | tar xJf - -C /opt/ \
  && mv /opt/llvm-mingw-20241203-ucrt-ubuntu-20.04-x86_64 /opt/woacross \
  && rm /opt/woacross/bin/*-gcc /opt/woacross/bin/*-g++
ENV PATH="/opt/woacross/bin:${PATH}"

#Fix permissions for OpenShift & standard k8s
RUN chown -R 1000:0 ${HOME} \
  && chmod -R g+rwX ${HOME}


ENV USER_NAME=vnc
USER 1000
WORKDIR ${HOME}

CMD ["/home/vnc/.vnc/xstartup.sh"]
