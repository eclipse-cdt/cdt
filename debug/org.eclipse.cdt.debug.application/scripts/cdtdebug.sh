#!/bin/sh
###############################################################################
# Copyright (c) 2014, 2015 Red Hat, Inc. and others
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html
#
# Contributors:
#    Red Hat Inc. - initial API and implementation
#    Marc Khouzam (Ericsson) - Update for remote debugging support (bug 450080)
###############################################################################

SCRIPT_DIR=`dirname $0`

usage="\
Usage: $0 [ECLIPSE_OPTIONS] [-b BUILD_LOG] [TARGET_OPTION]

Debug an executable, core-file, or an existing process using the Eclipse
C/C++ Stand-alone Debugger.  Eclipse command-line options may be passed
except for -vmargs which is being used to start up the Eclipse Debugger.

Operation modes:
  -h, --help                print this help, then exit

Indexing assist options:
  -b BUILD_LOG              build log to use for compiler includes/flags

Target options:
  -a [pid]                  attach using the optional pid or prompt for a pid 
  -c COREFILE               debug core-file (should also specify executable)
  -e EXECUTABLE [ARGS...]   debug given executable (passing ARGS to main)
  -r ADDRESS:PORT           debug toward the specified remote server. Can be
                            combined with the -a option.

The -e option must be used last as subsequent options are passed to main.

Specifying insufficient arguments for a particular target will result in a
dialog displayed to enter the required values for that target.  Specifying
no target option brings up a dialog for debugging an executable with the
executable path, program arguments, and build log filled in from the last -e
invocation, if one exists.

Wiki page: <http://wiki.eclipse.org/CDT/StandaloneDebugger>"

exit_missing_arg='
  echo $0": error: option [$1] requires an argument"; exit 1'

# Parse command line.
options=
while test $# -gt 0 ; do
  case $1 in
    --help | -h )
       echo "$usage"; exit ;;
    -vmargs )
       echo $0": error: -vmargs option is prohibited"; exit 1;;
    -e )
       test $# = 1 && eval "$exit_missing_arg"
       options="$options $1 $2"
       shift; shift;
       # Get all options after -e and protect them from being
       # processed by Eclipse as Eclipse options
       while test $# -gt 0; do
          options="$options \"$1\""
          shift;
       done ;;
    -c | -r )
       test $# = 1 && eval "$exit_missing_arg"
       options="$options $1 $2"
       shift; shift ;;
    * )
       options="$options $1"; shift ;;
  esac
done

# Make sure local directory exists and has contents initialized
if [ ! -d "$HOME/cdtdebugger" ]; then
  /bin/sh "$SCRIPT_DIR/install.sh" || exit
fi

# Calculate platform-specific jar file names
ECLIPSE_HOME=$(cd "$SCRIPT_DIR/../../.." && pwd)  # install.sh will modify this line.  DO NOT REMOVE THE FOLLOWING MARKER: @#@#
ECLIPSE_EXEC="$ECLIPSE_HOME/eclipse"

# On macOS, the application layout is a bit different (Eclipse.app)
case $ECLIPSE_HOME in
  *MacOS) ECLIPSE_HOME="$ECLIPSE_HOME/../Eclipse" ;;
esac

PLUGIN_DIR="$ECLIPSE_HOME/plugins"
OSGI_JAR=`find "$PLUGIN_DIR" -maxdepth 1 -name 'org.eclipse.osgi_*.jar' -not -name '*source*' -exec basename {} \; | tail -1`

# Run eclipse with the Stand-alone Debugger product specified
"$ECLIPSE_EXEC" -clean -product org.eclipse.cdt.debug.application.product \
                        -data "$HOME/workspace-cdtdebug" -configuration file\:"$HOME/cdtdebugger" \
                        -dev file\:"$HOME/cdtdebugger/dev.properties" $options \
                        -vmargs -Dosgi.jar=$OSGI_JAR -Declipse.home="$ECLIPSE_HOME"

