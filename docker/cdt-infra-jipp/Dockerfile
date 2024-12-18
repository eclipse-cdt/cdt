FROM cdt-infra:latest
USER root

RUN chown -R 1000:0 ${HOME} \
  && chmod -R g+rwX ${HOME}

USER 1000

CMD ["/home/vnc/.vnc/xstartup.sh"]
