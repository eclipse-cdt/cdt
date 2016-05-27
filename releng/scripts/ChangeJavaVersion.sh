#!/bin/sh

set -x
root_dir=.
old_ver=1.7
new_ver=1.8

git_clean_msg='working\ directory\ clean'

classpath_file=.classpath
manifest_file=MANIFEST.MF

java_package_name=JavaSE
jdt_pref_file=org.eclipse.jdt.core.prefs
jdt_pref1=org.eclipse.jdt.core.compiler.codegen.targetPlatform
jdt_pref2=org.eclipse.jdt.core.compiler.compliance
jdt_pref3=org.eclipse.jdt.core.compiler.source

check_git_clean()
{
    if [ "`echo working directory clean | grep ${git_clean_msg}`" = "" ]; then
#    if [ "`git status | grep ${git_clean_msg}`" = "" ]; then
        echo not clean
    else
        echo clean
    fi
}

fix_classpath()
{
    find $root_dir -type f -name ${classpath_file} -exec sed -i s,${java_package_name}-${old_ver},${java_package_name}-${new_ver}, '{}' \;
}

fix_manifest()
{
    find $root_dir -type f -name ${manifest_file} -exec sed -i s,${java_package_name}-${old_ver},${java_package_name}-${new_ver}, '{}' \;
}

fix_jdt_prefs()
{
    find $root_dir -type f -name ${jdt_pref_file} -exec sed -i s,${jdt_pref1}=$old_ver,${jdt_pref1}=$new_ver, '{}' \;
    find $root_dir -type f -name ${jdt_pref_file} -exec sed -i s,${jdt_pref2}=$old_ver,${jdt_pref2}=$new_ver, '{}' \;
    find $root_dir -type f -name ${jdt_pref_file} -exec sed -i s,${jdt_pref3}=$old_ver,${jdt_pref3}=$new_ver, '{}' \;
}

check_git_clean
#fix_classpath
#fix_manifest
#fix_jdt_prefs
