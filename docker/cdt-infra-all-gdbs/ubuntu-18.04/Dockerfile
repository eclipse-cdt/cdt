FROM ubuntu:18.04

USER root


RUN apt-get update && apt-get install -y --no-install-recommends \
      ca-certificates \
      curl \
      git \
      gnupg \
      openssh-client \
      pkg-config \
      wget \
      zip \
      locales \
      build-essential \
      bison \
      flex \
      libgmp-dev \
      libexpat1-dev \
      libmpfr-dev \
      libncurses-dev \
      pkg-config \
      zlib1g-dev \
      texinfo \
    && rm -rf /var/lib/apt/lists/* \
    && locale-gen en_US.UTF-8

# Need locale to be UTF-8
ENV LANG en_US.UTF-8
ENV LANGUAGE en_US:en
ENV LC_ALL en_US.UTF-8

# Build GDB
COPY scripts/download-build-gdb.sh /opt/scripts/download-build-gdb.sh
ENV GDB_BASE /shared/common/gdb/gdb-all
ENV GDB_VERSIONS all
# build without debug info to reduce size
ENV CFLAGS -g0
ENV CXXFLAGS -g0
RUN chmod u+x /opt/scripts/download-build-gdb.sh && mkdir -p /shared/common/gdb/gdb-all  && /opt/scripts/download-build-gdb.sh -b $GDB_BASE $GDB_VERSIONS \
    && rm -rf $GDB_BASE/download && rm -rf $GDB_BASE/build
