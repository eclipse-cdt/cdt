FROM ubuntu:18.04

ENV HOME=/home/vnc
RUN apt-get update
RUN apt-get install -y curl tar gzip
#Eclipse SDK & Fix permissions for OpenShift & standard k8s
RUN mkdir -p ${HOME}/buildtools && cd ${HOME}/buildtools \
  && curl -sL https://download.eclipse.org/eclipse/downloads/drops4/R-4.23-202203080310/eclipse-SDK-4.23-linux-gtk-x86_64.tar.gz | tar xvz \
  && mv eclipse eclipse-SDK-4.23 \
  && curl -sL https://download.eclipse.org/eclipse/downloads/drops4/R-4.31-202402290520/eclipse-SDK-4.31-linux-gtk-x86_64.tar.gz | tar xvz \
  && mv eclipse eclipse-SDK-4.31 \
  && chown -R 1000:0 ${HOME} \
  && chmod -R g+rwX ${HOME}

FROM cdt-infra-eclipse-full:ubuntu-18.04
USER root

COPY --from=0 ${HOME}/buildtools ${HOME}/buildtools
RUN apt-get update \
    && apt-get install -y clang-format \
    && rm -rf /var/lib/apt/lists/*
RUN apt-get update
RUN apt-get install -y ssh-askpass

USER 1000

CMD ["/home/vnc/.vnc/xstartup.sh"]
