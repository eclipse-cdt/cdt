# Treat this is an example build script
# Please adjust paths as necessary for your build machine

export JAVA_HOME=/opt/java/j2sdk1.4.2_03
export PATH=$JAVA_HOME/bin:$PATH

cd `dirname $0`

rm -fr results

java -cp eclipse/startup.jar org.eclipse.core.launcher.Main -application org.eclipse.ant.core.antRunner $* \
	-DbaseLocation=$PWD/eclipse \
	-DbuildDirectory=$PWD/results
