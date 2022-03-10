installdir="$HOME/.eclipsesettings"
if test ! -d $installdir; then
	mkdir $installdir
	if test ! -d $installdir; then
		echo fail:cannot create $installdir
		exit 1
	fi
fi
cat > $installdir/bootstrap.sh <<-\EOF
#!/bin/sh
installdir="$HOME/.eclipsesettings"
proxytmp=$installdir/proxy.b64
success=false

cleanup() {
	rm -f $installdir/bootstrap.sh
}

trap 'cleanup' EXIT
	
parent_is_not_orphan () {
      parent=`ps -ef|awk '$2=='$$'{print $3}'`
      let parent=$parent+0
      if [[ $parent -eq 1 ]]; then
        return 1
      fi
      return 0
}

do_check() {
	java_vers=`java -version 2>&1`
	case "$java_vers" in
	*"not found")
		echo "fail:could not find a valid java installation"
		return
		;;
	esac
	major=`expr "$java_vers" : ".* version \"\([0-9]*\)\.[0-9]*.*\""`
	minor=`expr "$java_vers" : ".* version \"[0-9]*\.\([0-9]*\).*\""`
	if test "$major" -ge 2 -o "$minor" -ge 8; then
		:
	else
		echo "fail:invalid java version $major.$minor; must be >= 1.8"
		return
	fi
	case "`uname`" in
	Linux) 
		osname="linux"; 
		osarch=`uname -m`;
		proxydir=$installdir/proxy;
		plugins=$proxydir/plugins;;
	Darwin) 
		osname="macosx"; 
		osarch=`uname -m`;
		proxydir=$installdir/Proxy.app;
		plugins=$proxydir/Contents/Eclipse/plugins;;
	*)
		echo fail:system not supported;
		return;;
	esac
	proxy=not_found
	if test -d $proxydir; then
		bundle="org.eclipse.remote.proxy.server.core_$1.jar"
		if test -f $plugins/$bundle; then
			proxy=found
		else
			mv $proxydir $proxydir.pre_$1
		fi
	fi
	echo ok:$proxy/$osname/$osarch
}

do_download() {
	dd of=$proxytmp ibs=680 count=$1
	IFS= read -r last
	echo "$last" >> $proxytmp
	base64 --decode < $proxytmp | (cd $installdir && tar zxvf -) > /dev/null 2>&1
	if test $? -eq 0; then
		echo ok
	else
		echo fail:download failed
	fi
}

#	
# Start java in background so we can clean up after connection is dropped. The only way to tell if this
# has happened is to poll if ppid has changed to 1 (i.e. we no longer have a controlling terminal)
#
start_server() {
	# enable debugoptions in order to attach a debugger
	#debugoptions="-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=1044,quiet=y"
	
	# use globbing to find launcher version
	java -cp $plugins/org.eclipse.equinox.launcher_1.*.jar \
		$debugoptions \
		org.eclipse.equinox.launcher.Main \
		-application org.eclipse.remote.proxy.server.core.application \
		-noExit 0<&0 &
		
	pid=$!
	
	trap 'kill $pid; exit' HUP INT TERM
	
	while parent_is_not_orphan; do
	  sleep 10
	done
	
	kill $pid
}

echo running

while read line arg; do
	case $line in
	check) do_check $arg;;
	download) do_download $arg;;
	start) start_server;;
	exit) break;;
	*) echo fail:unrecognized command:$line; exit 1;;
	esac
done
exit 0
EOF
chmod 755 $installdir/bootstrap.sh
exec $installdir/bootstrap.sh
