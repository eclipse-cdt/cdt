#!/bin/bash

for i in `find . -name .project`; do 
    d=`dirname $i`; 
    if test ! -e $d/feature.xml; then 
        mkdir -p $d/.settings
        cp core/org.eclipse.cdt.core/.settings/org.eclipse.jdt.* core/org.eclipse.cdt.core/.settings/org.eclipse.pde.* $d/.settings
        find $d/.settings/org.eclipse.jdt.* $d/.settings/org.eclipse.pde.*
    fi
done

