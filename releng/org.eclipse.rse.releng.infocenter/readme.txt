DSDP Project Infocenter
-----------------------

This project contains scripts needed to run the Eclipse headless infocenter
application, serving latest online help from dsdp.eclipse.org.

The scripts are written to run by cron job, as user "infocenter", on dsdp.eclipse.org.

This project can be checked out with
  cvs -d :pserver:anonymous@dev.eclipse.org:/cvsroot/tools \
    co -d bin org.eclipse.tm.rse/releng/org.eclipse.tm.rse.releng.infocenter

Apache Installation:
* vi /etc/httpd/conf/httpd.conf
  ADD
  AddDefaultCharset Off
  ProxyPass /help/latest http://localhost:27127/help
  ProxyPassReverse /help/latest http://localhost:27127/help

Installation:
* Infocenter home (IHOME) at /home/infocenter/latest
  - Scripts checked out at $IHOME/bin
  - Deployable doc plugins at $IHOME/deploy
* cd $IHOME/bin
  ./setup.sh
  ./update.sh
* Cronjob created to execute doit_nightly.sh
    ssh dsdp.eclipse.org -l infocenter
    crontab -e
    #Run the doc update at 2:00 every weekday
    0 2 * * 1-5     /home/infocenter/latest/bin/doit_nightly.sh

Other infocenter administration commands:
  infocenter.sh shutdown
  infocenter.sh start &

For more documentation on infocenter, see
http://help.eclipse.org/ganymede/index.jsp?topic=/org.eclipse.platform.doc.isv/guide/ua_help_setup_infocenter.htm
http://help.eclipse.org/stable/topic/org.eclipse.platform.doc.isv/guide/ua_help_setup_infocenter.htm

-- Martin Oberhuber, 20-Jul-2006