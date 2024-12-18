FROM cdt-infra:latest
USER root

RUN chown -R 1001:0 ${HOME} \
  && chmod -R g+rwX ${HOME}

USER 1001

CMD ["/home/vnc/.vnc/xstartup.sh"]
