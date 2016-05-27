#!/bin/sh
#
# This script will recursively update all plugins starting
# at $root_dir from java version $old_ver to $new_ver

# Modify the version based on the change you need
old_ver=1.7
new_ver=1.8
java_name=JavaSE

root_dir=.

# The below variable values should not need any changes
classpath_file=.classpath
manifest_file=MANIFEST.MF
jdt_pref_file=org.eclipse.jdt.core.prefs
jdt_pref1=org.eclipse.jdt.core.compiler.codegen.targetPlatform
jdt_pref2=org.eclipse.jdt.core.compiler.compliance
jdt_pref3=org.eclipse.jdt.core.compiler.source

fix_classpath_file()
{
    find $root_dir -type f -name ${classpath_file} \
        -exec sed -i s,${java_name}-${old_ver},${java_name}-${new_ver}, '{}' \;
}

fix_manifest_file()
{
    find $root_dir -type f -name ${manifest_file} \
        -exec sed -i s,${java_name}-${old_ver},${java_name}-${new_ver}, '{}' \;
}

fix_jdt_prefs_file()
{
    find $root_dir -type f -name ${jdt_pref_file} \
        -exec sed -i s,${jdt_pref1}=$old_ver,${jdt_pref1}=$new_ver, '{}' \;
    find $root_dir -type f -name ${jdt_pref_file} \
        -exec sed -i s,${jdt_pref2}=$old_ver,${jdt_pref2}=$new_ver, '{}' \;
    find $root_dir -type f -name ${jdt_pref_file} \
        -exec sed -i s,${jdt_pref3}=$old_ver,${jdt_pref3}=$new_ver, '{}' \;
}

fix_classpath_file
fix_manifest_file
fix_jdt_prefs_file
