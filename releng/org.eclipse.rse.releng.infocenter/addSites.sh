#!/bin/sh
#
# Add new Eclipse "extsion locations" to the infocenter
#
IHOME=/home/infocenter/latest
$IHOME/bin/infocenter.sh addSite -from $IHOME/plugins/rse/eclipse
$IHOME/bin/infocenter.sh addSite -from $IHOME/plugins/dd.dsf/eclipse
$IHOME/bin/infocenter.sh apply
