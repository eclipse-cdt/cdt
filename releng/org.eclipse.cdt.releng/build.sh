# JAVA_HOME must be set to the root of your JRE directory (JDK if you want to do javadoc)
# ECLIPSE_HOME must be set to the root of you Eclipse install that you want to do the build with
#    right now, this must be a 3.1.0 install (for PDE build scripts)

die() {
	echo $*
	exit 1
}

[ -n "$JAVA_HOME" ] || die JAVA_HOME not set
[ -n "$ECLIPSE_HOME" ] || die ECLIPSE_HOME not set

export PATH="$JAVA_HOME/bin:$PATH"

cd `dirname $0`

java -cp $ECLIPSE_HOME/startup.jar org.eclipse.core.launcher.Main -application org.eclipse.ant.core.antRunner $* 2>&1 | tee build.log
