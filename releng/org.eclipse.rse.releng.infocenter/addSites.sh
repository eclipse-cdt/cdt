#!/bin/sh
#
# Add new Eclipse "extsion locations" to the infocenter
#
IHOME=/home/infocenter/latest
$IHOME/infocenter.sh addSite -from $IHOME/plugins/rse/eclipse
$IHOME/infocenter.sh apply

