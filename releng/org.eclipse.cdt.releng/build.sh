# Treat this is an example build script
# Please adjust paths as necessary for your build machine

export JAVA_HOME=/opt/java/j2sdk1.4.2_03
export PATH=$JAVA_HOME/bin:$PATH
export ECLIPSE_HOME=~/eclipse/3.0/eclipse

cd `dirname $0`

java -cp $ECLIPSE_HOME/startup.jar org.eclipse.core.launcher.Main -application org.eclipse.ant.core.antRunner $* 2>&1 | tee build.log
