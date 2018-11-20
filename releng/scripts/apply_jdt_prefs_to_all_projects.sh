#!/bin/bash

for i in `find . -name .project`; do 
    d=`dirname $i`; 
    if test ! -e $d/feature.xml; then
        mkdir -p $d/.settings
        cp core/org.eclipse.cdt.core/.settings/org.eclipse.jdt.* core/org.eclipse.cdt.core/.settings/org.eclipse.pde.* $d/.settings
        # For test plug-ins, don't warn on missing NLS
        if echo $i | grep '\.tests[/\.]' > /dev/null; then
            sed -i '-es@org.eclipse.jdt.core.compiler.problem.nonExternalizedStringLiteral=warning@org.eclipse.jdt.core.compiler.problem.nonExternalizedStringLiteral=ignore@' $d/.settings/org.eclipse.jdt.core.prefs
        fi
    fi
done

