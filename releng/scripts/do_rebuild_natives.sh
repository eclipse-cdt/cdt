#!/bin/bash

set -e

##
# Make sure that natives are up to date
##
for p in native/org.eclipse.cdt.native.serial core/org.eclipse.cdt.core.native; do
    echo "Rebuilding $p JNI headers to make sure they match source"
    logfile=jni-headers-${p//\//-}.log
    if ! ${MVN:-mvn} -B -V process-resources -DuseSimrelRepo -P jniheaders -f $p >${logfile} 2>&1; then
        echo "Rebuilding of $p JNI headers failed. The log (${logfile}) is part of the artifacts of the build"
        exit 1
    fi

    echo "Rebuilding $p natives to make sure they match source"
    logfile=make-natives-${p//\//-}.log
    if ! make -C $p/native_src rebuild >${logfile} 2>&1; then
        echo "Rebuilding of $p natives failed. The log (${logfile}) is part of the artifacts of the build"
        exit 1
    fi
done

##
# Mark Windows binaries as executable
##
echo "Marking Windows binaries as executable"
git ls-files -- \*.exe \*.dll | while read line; do
    chmod +x "$line"
done
