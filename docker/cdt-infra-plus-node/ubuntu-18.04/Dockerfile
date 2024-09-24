FROM cdt-infra-eclipse-full:ubuntu-18.04
USER root

#Node
RUN curl -sL https://deb.nodesource.com/setup_14.x | bash \
    && apt-get install -y nodejs \
    && rm -rf /var/lib/apt/lists/*  \
    && npm install -g yarn

# socat needed for some cdt-gdb-adapter tests
RUN apt-get update && apt-get install -y socat && rm -rf /var/lib/apt/lists/*

#Fix permissions for OpenShift & standard k8s
RUN chown -R 1000:0 ${HOME} \
  && chmod -R g+rwX ${HOME}

ENV USER_NAME vnc
USER 1000
WORKDIR ${HOME}

CMD ["bash"]
