#!/bin/bash

echo -n "[relengbuild] $0 started on: `date +%Y%m%d\ %H\:%M\:%S`";

# environment variables
PATH=.:/bin:/usr/bin:/usr/bin/X11:/usr/local/bin:/usr/X11R6/bin:`pwd`/../linux;export PATH

export USERNAME=`whoami`
echo " running as $USERNAME";
echo " currently in dir: `pwd`";

if [[ ! $JAVA_HOME ]]; then
	echo -n "[relengbuild] Get JAVA_HOME from build.cfg ... ";
	buildcfg=$PWD/../../../build.cfg;
	export JAVA_HOME=$(grep "JAVA_HOME=" $buildcfg | egrep -v "^#" | tail -1 | sed -e "s/JAVAHOME=//");
	echo "$JAVA_HOME";
fi

Xflags="";
Dflags="";

# default target to run in org.eclipse.$subprojectName.releng/builder/tests/scripts/test.xml
antTestTarget=all

# process command line arguments
while [ $# -gt 0 ]
do
	case "$1" in
		-vmExecutable) vmExecutable="$2"; shift;;
		-consolelog)   consolelog="$2";   shift;;
		-X*) Xflags=$Xflags\ $1;;
		-D*) Dflags=$Dflags\ $1;;
	esac
	shift
done

checkIfj9 ()
{
# given a series of -X flags, see if the string -Xj9 can be found
  j9=$Xflags;
  #echo "Xflags=$Xflags"
  j9=${j9/\-Xj9/} # substring replacement
  #echo "remaining: $j9"
  if [ "$j9" != "$Xflags" ]; then # found it
    j9="j9";
  else
    j9="";
  fi
}
checkIfj9;

defined=0;
checkIfDefined ()
{
	if [ -f $1 ] ; then
		defined=1;
	else
		defined=0;
	fi
}

execCmd ()
{
	echo ""; echo "[relengbuild] [`date +%H\:%M\:%S`]"; 
	echo "  $1" | perl -pe "s/ -/\n  -/g";
	if [ "x$2" != "x" ]; then
		$1 2>&1 | tee $2;
	else
		$1;
	fi
}

doFunction ()
{
	cmd=$1;
	params=$2
	for pth in "." "/bin" "/usr/bin" "/usr/bin/X11" "/usr/local/bin" "/usr/X11R6/bin" "`pwd`/../linux" ; do
		defined=0;
		checkIfDefined $pth/$cmd
		if [ $defined -eq 1 ] ; then
			$cmd $params
			sleep 3
			break;
		fi
	done
	if [ $defined -eq 0 ] ; then
		echo "$cmd is not defined (command not found)";
	fi
}

# these don't work on old build server, so not point wrapping them to say so when we can just omit
# doFunction Xvfb ":42 -screen 0 1024x768x24 -ac & "
# doFunction Xnest ":43 -display :42 -depth 24 & "
# doFunction fvwm2 "-display localhost:43.0 & "
#export DISPLAY=$HOSTNAME:43.0
#ulimit -c unlimited

getBuildID()
{	# given $PWD: /home/www-data/build/dsdp/$projectName/$subprojectName/downloads/drops/1.1.0/N200702112049/testing/N200702112049/testing
	# return N200702110400
	buildID=$1; #echo "buildID=$buildID";
	buildID=${buildID##*drops\/}; # trim up to drops/ (from start) (substring notation)
	buildID=${buildID%%\/test*}; # trim off /test (to end) (substring notation)
	buildID=${buildID##*\/}; # trim up to / (from start) (substring notation)
}
buildID=""; getBuildID $PWD; #echo buildID=$buildID;

getBranch()
{	# given $PWD: /home/www-data/build/dsdp/$projectName/$subprojectName/downloads/drops/1.1.0/N200702112049/testing/N200702112049/testing
	# return 1.1.0
	branch=$1; #echo "branch=$branch";
	branch=${branch##*drops\/}; # trim up to drops/ (from start) (substring notation)
	branch=${branch%%\/*}; # trim off / (to end) (substring notation)
}
branch=""; getBranch $PWD; #echo branch=$branch;

############################# BEGIN RUN TESTS #############################  


# operating system, windowing system and architecture variables
# for *nix systems, os, ws and arch values must be specified
Dflags=$Dflags" "-Dplatform=linux.gtk
os=linux
ws=gtk
arch=x86

# default value to determine if eclipse should be reinstalled between running of tests
installmode="clean"

#this value must be set when using rsh to execute this script, otherwise the script will execute from the user's home directory
dir=.
cd $dir
workspaceDir="$dir/eclipse/workspace"

# Replace the boot eclipse (The eclipse used to run the main test.xml, this will start another eclipse later)
if [ -d $dir/eclipse ] ; then
	rm -rf $dir/eclipse
fi
if [ -d $workspaceDir ] ; then
	rm -rf $dir/workspace
fi

echo "[runtests] Currently in `pwd`:"
# need conditional processing here: M3.0.2 = zip, I3.1.0 = tar.gz
sdks=`find $dir -name "eclipse-SDK-*"`
# get extension from file(s)
for sdk in $sdks; do
	sdk="eclipse"${sdk##*eclipse}; # trim up to eclipse (substring notation)
	#echo -n "[runtests] Eclipse SDK $sdk is a";
	ext=${sdk%%\.zip}; # trim off .zip (substring notation)
	if [ "$ext" != "$sdk" ]; then # it's a zip
		#echo " zip. Unpacking...";
		unzip -qq -o $sdk
	else
		ext=${sdk%%\.tar\.gz}; # trim off .tar.gz (substring notation)
		if [ "$ext" != "$sdk" ]; then # it's a tar.gz
			#echo " tar.gz. Unpacking...";
			tar -xzf $sdk
		else
			ext=${sdk%%\.tar\.Z}; # trim off .tar.Z (substring notation)
			if [ "$ext" != "$sdk" ]; then # it's a tar.Z
				#echo " tar.Z. Unpacking...";
				tar -xZf $sdk
			else
				echo "[runtests] ERROR: Eclipse SDK $sdk is an UNKNOWN file type. Failure.";
				exit 2
			fi
		fi
	fi
done

J2SE15flags="";
# TODO: if a 1.5 JDK and want source/target = 1.5, leave these in
# TODO: if source/target = 1.4, remove these!
if [ ${JAVA_HOME##*1.5*}"" = "" -o ${JAVA_HOME##*15*}"" = "" -o ${JAVA_HOME##*5.0*}"" = "" -o ${JAVA_HOME##*50*}"" = "" ]; then
	# set J2SE-1.5 properties (-Dflags)
	bootclasspath="."`find $JAVA_HOME/jre/lib -name "*.jar" -printf ":%p"`;
	J2SE15flags=$J2SE15flags" -DJ2SE-1.5=$bootclasspath"
	J2SE15flags=$J2SE15flags" -DbundleBootClasspath=$bootclasspath"
	J2SE15flags=$J2SE15flags" -DjavacSource=1.5"
	J2SE15flags=$J2SE15flags" -DjavacTarget=1.5"
	J2SE15flags=$J2SE15flags" -DbundleJavacSource=1.5"
	J2SE15flags=$J2SE15flags" -DbundleJavacTarget=1.5"
fi

# different ways to get the launcher and Main class
if [[ -f eclipse/startup.jar ]]; then 
  cpAndMain="eclipse/startup.jar org.eclipse.core.launcher.Main"; # up to M4_33
elif [[ -f eclipse/plugins/org.eclipse.equinox.launcher.jar ]]; then
  cpAndMain="eclipse/plugins/org.eclipse.equinox.launcher.jar org.eclipse.equinox.launcher.Main"; # M5_33
else
  cpAndMain=`find eclipse/ -name "org.eclipse.equinox.launcher_*.jar" | sort | head -1`" org.eclipse.equinox.launcher.Main"; 
fi

# run tests
echo "[runtests] [`date +%H\:%M\:%S`] Launching Eclipse (installmode = $installmode with -enableassertions turned on) ..."
execCmd "$JAVA_HOME/bin/java $Xflags -enableassertions -cp $cpAndMain -ws $ws -os $os -arch $arch \
-application org.eclipse.ant.core.antRunner -data $workspaceDir -file test.xml $antTestTarget \
$Dflags -Dws=$ws -Dos=$os -Darch=$arch -D$installmode=true $J2SE15flags \
$properties -logger org.apache.tools.ant.DefaultLogger" $consolelog;
echo "[runtests] [`date +%H\:%M\:%S`] Eclipse test run completed. "

############################# END RUN TESTS #############################  

# supress errors by checking for the file first
if [ -r /tmp/.X43-lock ] ; then
	kill `cat /tmp/.X43-lock`
fi
if [ -r /tmp/.X42-lock ] ; then
	kill `cat /tmp/.X42-lock`
fi

if [[ ! -d $PWD/results ]]; then
	echo "[relengbuild] No test results found in $PWD/results!";
	echo "[relengbuild] Creating 'noclean' file to prevent cleanup after build completes."
	echo "1" > $PWD/../../../noclean;
else
# if the build failed for some reason, don't clean up!
xmls=`find $PWD/results/xml -name "*.xml"`;
testsFailed=1;
for xml in $xmls; do
	if [ $testsFailed -eq 1 ]; then
		testsFailed=`cat $xml | grep -c "<testsuite errors=\"0\" failures=\"0\""`
		if [ $testsFailed -lt 1 ]; then
			echo "[relengbuild] Found test failure(s) in $xml!";
			echo "[relengbuild] Creating 'noclean' file to prevent cleanup after build completes."
			echo "1" > $PWD/../../../noclean;
			break;
		fi
	fi
done
fi;

echo "[relengbuild] relengbuildgtk.sh completed on: `date +%Y%m%d\ %H\:%M\:%S`"

