# This file is derived from:
# - https://github.com/eclipse-cbi/dockerfiles/blob/bf1f1a18f4910d007de67765f8df083102150a7c/distros/Dockerfile
# - https://github.com/eclipse-cbi/dockerfiles/blob/bf1f1a18f4910d007de67765f8df083102150a7c/gtk3-wm/ubuntu-metacity/18.04/Dockerfile
# Consult https://github.com/eclipse-cbi/dockerfiles for possible updates
# differences from source are commented
FROM ubuntu:18.04

### user name recognition at runtime w/ an arbitrary uid - for OpenShift deployments
COPY scripts/uid_entrypoint /usr/local/bin/uid_entrypoint
RUN chmod u+x /usr/local/bin/uid_entrypoint && \
    chgrp 0 /usr/local/bin/uid_entrypoint && \
    chmod g=u /usr/local/bin/uid_entrypoint /etc/passwd

# Add dumb-init entry point: https://github.com/eclipse-cdt/cdt-infra/pull/26
RUN apt-get update && apt-get install -y --no-install-recommends \
      dumb-init \
    && rm -rf /var/lib/apt/lists/*

ENTRYPOINT [ "uid_entrypoint", "dumb-init" ]

ENV DEBIAN_FRONTEND noninteractive
RUN apt-get update && apt-get install -y --no-install-recommends \
      libgtk-3-0 \
      tigervnc-standalone-server \
      tigervnc-common \
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

# In addition to metacity in upstream version, provide icewm and mutter manager scripts
# and use icewm as default (experiments showed that on 16.04 metacity was more reliable,
# but on 18.04 icewm is more reliable when running tests)
COPY scripts/xstartup_*.sh ${HOME}/.vnc/
COPY scripts/xstartup_metacity.sh ${HOME}/.vnc/xstartup.sh
RUN chmod 755 ${HOME}/.vnc/xstartup*.sh

####
# From this point forward is the extra standard tools for CDT

# git and other tools

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
    && rm -rf /var/lib/apt/lists/*

RUN locale-gen en_US.UTF-8
# Need locale to be UTF-8
ENV LANG en_US.UTF-8
ENV LANGUAGE en_US:en
ENV LC_ALL en_US.UTF-8


# Get release versions from Adoptium API: https://api.adoptium.net/q/swagger-ui/#/Release%20Info/getReleaseNames
ENV JDK17_VERSION jdk-17.0.11+9
ENV JDK21_VERSION jdk-21.0.3+9
ENV JAVA17_HOME /usr/lib/jvm/${JDK17_VERSION}
ENV JAVA21_HOME /usr/lib/jvm/${JDK21_VERSION}

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
ENV JAVA_HOME ${JAVA21_HOME}

# Maven - Note that this is the default, but the Jenkinsfile may override it with JIPP version
ENV MAVEN_VERSION 3.9.6
RUN curl -fsSL https://archive.apache.org/dist/maven/maven-3/$MAVEN_VERSION/binaries/apache-maven-$MAVEN_VERSION-bin.tar.gz | tar xzf - -C /usr/share \
  && mv /usr/share/apache-maven-$MAVEN_VERSION /usr/share/maven \
  && ln -s /usr/share/maven/bin/mvn /usr/bin/mvn
ENV MAVEN_HOME /usr/share/maven

RUN mkdir -p ${HOME}/.m2
COPY toolchains.xml ${HOME}/.m2

#Fix permissions for OpenShift & standard k8s
RUN chown -R 1000:0 ${HOME} \
  && chmod -R g+rwX ${HOME}

ENV USER_NAME vnc
USER 1000
WORKDIR ${HOME}
