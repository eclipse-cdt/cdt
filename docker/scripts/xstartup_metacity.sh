#!/usr/bin/env /bin/sh

[ -x /etc/vnc/xstartup ] && exec /etc/vnc/xstartup
[ -r ${HOME}/.Xresources ] && xrdb ${HOME}/.Xresources

Xvnc ${DISPLAY} -geometry 1440x900 -depth 16 -dpi 100 -PasswordFile ${HOME}/.vnc/passwd &
sleep 2
xsetroot -solid grey
vncconfig -iconic &
xhost +
metacity --replace --sm-disable --display=${DISPLAY} &