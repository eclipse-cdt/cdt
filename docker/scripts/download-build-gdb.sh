#!/usr/bin/env bash

# * Copyright (c) 2015, 2021 Ericsson and others.
# * This program and the accompanying materials
# * are made available under the terms of the Eclipse Public License 2.0
# * which accompanies this distribution, and is available at
# * https://www.eclipse.org/legal/epl-2.0/
# *
# * SPDX-License-Identifier: EPL-2.0
# *
# * Contributors:
# *     Simon Marchi (Ericsson) - Initial implementation

# Stop the script if any command fails
set -o errexit

# Consider using an unset variable as an error
set -o nounset

# Make sure getopt is the command and not the bash built-in
if [[ $(getopt --version) != *"getopt"* ]]; then
  echo "getopt command not found."
  exit 1
fi

# Our work directory
default_base_dir="$HOME/gdb-all"
base_dir="${default_base_dir}"

# Passed to make's -j
default_jlevel="12"
jlevel="${default_jlevel}"

# Supported versions
# Note starting in GDB 9.x the .x is the patch release, so for example we have 9.2 in this list, but not 9.1.
old_version="9.2 8.3.1 8.2.1 8.1.1 8.0.1 7.12.1 7.11.1 7.10.1 7.9.1 7.8.2 7.7.1 7.6.2 7.5.1 7.4.1 7.3.1 7.2 7.1 7.0.1 6.8 6.7.1 6.6"
default_versions="16.1 15.2 14.2 13.2 12.1 11.2 10.2"

# Is set to "echo" if we are doing a dry-run.
dryrun=""

# Is set to "yes" to download only
download_only="no"

# Print help and exit with the specified exit code.
#
# $1: The value to pass to exit
function help_and_exit() {
  echo "Usage:"
  echo "  download-build-gdb.sh [OPTIONS] [VERSIONS|all]"

  echo ""
  echo "Description:"
  echo "  This script downloads, builds and installs the given versions of gdb."
  echo "  Passing \"all\" to the script is the same as passing all the supported versions."
  echo ""
  echo "Options:"
  echo "  -b, --base-dir PATH  Set the base directory for downloading, building and "
  echo "                       installing the gdbs (default: ${default_base_dir})."
  echo "  -d, --dry-run        Make a dry-run: print the commands instead of executing"
  echo "                       them."
  echo "      --download       Download, but do not build."
  echo "  -h, --help           Print this help message and exit."
  echo "  -j, --jobs N         Number of parallel jobs while making. N is passed"
  echo "                       directly to make's -j (default: ${default_jlevel})."
  echo ""
  echo "Supported versions:"
  echo "  ${default_versions}"
  echo "Older versions:"
  echo "  ${old_version}"
  echo ""
  echo "Examples:"
  echo "  Build versions 7.7.1 and 7.8.2:"
  echo "    $ $0 7.7.1 7.8.2"
  echo "  Build all supported versions:"
  echo "    $ $0 all"
  echo ""

  exit "$1"
}

# Output a visible header
#
# $1: Text to display
function echo_header() {
  echo -e "\e[1m\e[7m>>> $1\e[0m"
}


# Check that the version passed is supported by the script.
#
# $1: version number
function check_supported() {
  local supported_pattern="@(${default_versions// /|})"
  local old_pattern="@(${old_version// /|})"
  local version="$1"

  shopt -s extglob
  case "$version" in
    ${supported_pattern})
      # Supported, do nothing.
      ;;
    ${old_pattern})
      # Supported, do nothing.
      ;;
    *)
      echo "Error: version ${version} is not supported by this script."
      echo ""
      help_and_exit 1
      ;;
  esac
}


# Download the tarball of the given release of gdb.
#
# $1: version number
function download_gdb() {
  local baseaddr="https://ftp.gnu.org/gnu/gdb"
  local version="$1"

  case "$version" in
    "6.6"|"6.7.1"|"6.8"|"7.0.1"|"7.1"|"7.2")
      version="${version}a"
      ;;
  esac

  echo_header "Downloading gdb $version to ${download_dir}"

  ${dryrun} wget --timestamping --directory-prefix "${download_dir}" "${baseaddr}/gdb-${version}.tar.gz"
}


# Extract the gdb tarball.
#
# $1: version number
function extract_gdb() {
  local version="$1"

  case "$version" in
    "6.6"|"6.7.1"|"6.8"|"7.0.1"|"7.1"|"7.2")
      version="${version}a"
      ;;
  esac

  local archive="${download_dir}/gdb-${version}.tar.gz"

  echo_header "Extracting ${archive} to ${src_dir}"

  ${dryrun} mkdir -p "${src_dir}"

  ${dryrun} tar -xf "${archive}" -C "${src_dir}"
}

# Make necessary fixes to build an "old" release on a "modern" system.
#
# $1: version number
function fixup_gdb() {
  local version="$1"
  local build="${src_dir}/gdb-${version}"

  echo_header "Fixing up gdb ${version}"

  case "$version" in
    # glibc or the kernel changed the signal API at some point
    "7.4.1"|"7.3.1"|"7.2"|"7.1"|"7.0.1"|"6.8"|"6.7.1"|"6.6")
      ${dryrun} find "${build}/gdb" -type f -exec sed -i -e 's/struct siginfo;/#include <signal.h>/g' {} \;
      ${dryrun} find "${build}/gdb" -type f -exec sed -i -e 's/struct siginfo/siginfo_t/g' {} \;
      ;;
  esac

  case "$version" in
    # paddr_t was a typo a long time ago that slowly got fixed/removed from sources/headers
    "6.6")
      ${dryrun} find "${build}/gdb" -type f -exec sed -i -e 's/paddr_t/psaddr_t/g' {} \;
      ;;
  esac

  case "$version" in
    # glibc or the kernel changed the proc-service API at some point (original GDB fix: https://sourceware.org/ml/gdb-patches/2015-02/msg00210.html)
    "7.9.1"|"7.8.2"|"7.7.1"|"7.6.2"|"7.5.1"|"7.4.1"|"7.3.1"|"7.2"|"7.1"|"7.0.1"|"6.8"|"6.7.1"|"6.6")
      ${dryrun} sed -i -e 's/ps_lgetfpregs (gdb_ps_prochandle_t ph, lwpid_t lwpid, void \*fpregset)/ps_lgetfpregs (gdb_ps_prochandle_t ph, lwpid_t lwpid, prfpregset_t *fpregset)/g' "${build}/gdb/gdbserver/proc-service.c"
      ${dryrun} sed -i -e 's/ps_lsetfpregs (gdb_ps_prochandle_t ph, lwpid_t lwpid, void \*fpregset)/ps_lsetfpregs (gdb_ps_prochandle_t ph, lwpid_t lwpid, const prfpregset_t *fpregset)/g' "${build}/gdb/gdbserver/proc-service.c"
      ;;
  esac

    # gdb commit 5a6c3296a7a90694ad4042f6256f3da6d4fa4ee8 - Fix ia64 defining TRAP_HWBKPT before including gdb_wait.h
  case "$version" in
    "8.1.1"|"8.0.1")
      ${dryrun} patch --directory=${build} --strip 1 <<END
diff --git a/gdb/nat/linux-ptrace.c b/gdb/nat/linux-ptrace.c
index 5c4ddc95909..1f21ef03a39 100644
--- a/gdb/nat/linux-ptrace.c
+++ b/gdb/nat/linux-ptrace.c
@@ -21,8 +21,6 @@
 #include "linux-procfs.h"
 #include "linux-waitpid.h"
 #include "buffer.h"
-#include "gdb_wait.h"
-#include "gdb_ptrace.h"
 #ifdef HAVE_SYS_PROCFS_H
 #include <sys/procfs.h>
 #endif
diff --git a/gdb/nat/linux-ptrace.h b/gdb/nat/linux-ptrace.h
index 60967a3b6aa..dc180fbf82a 100644
--- a/gdb/nat/linux-ptrace.h
+++ b/gdb/nat/linux-ptrace.h
@@ -21,6 +21,7 @@
 struct buffer;
 
 #include "nat/gdb_ptrace.h"
+#include "gdb_wait.h"
 
 #ifdef __UCLIBC__
 #if !(defined(__UCLIBC_HAS_MMU__) || defined(__ARCH_HAS_MMU__))
END
      ;;

    "7.12.1")
      ${dryrun} patch --directory=${build} --strip 1 <<END
diff --git a/gdb/nat/linux-ptrace.c b/gdb/nat/linux-ptrace.c
index 3447e0716c1..dd3310eecbf 100644
--- a/gdb/nat/linux-ptrace.c
+++ b/gdb/nat/linux-ptrace.c
@@ -21,8 +21,6 @@
 #include "linux-procfs.h"
 #include "linux-waitpid.h"
 #include "buffer.h"
-#include "gdb_wait.h"
-#include "gdb_ptrace.h"
 #include <sys/procfs.h>
 
 /* Stores the ptrace options supported by the running kernel.
diff --git a/gdb/nat/linux-ptrace.h b/gdb/nat/linux-ptrace.h
index 59549452c09..6faa89b22a0 100644
--- a/gdb/nat/linux-ptrace.h
+++ b/gdb/nat/linux-ptrace.h
@@ -21,6 +21,7 @@
 struct buffer;
 
 #include "nat/gdb_ptrace.h"
+#include "gdb_wait.h"
 
 #ifdef __UCLIBC__
 #if !(defined(__UCLIBC_HAS_MMU__) || defined(__ARCH_HAS_MMU__))
END
      ;;

    "7.11.1")
      ${dryrun} patch --directory=${build} --strip 1 <<END
diff --git a/gdb/nat/linux-ptrace.c b/gdb/nat/linux-ptrace.c
index 0eaf9a30ff4..446d5ba94b5 100644
--- a/gdb/nat/linux-ptrace.c
+++ b/gdb/nat/linux-ptrace.c
@@ -21,8 +21,6 @@
 #include "linux-procfs.h"
 #include "linux-waitpid.h"
 #include "buffer.h"
-#include "gdb_wait.h"
-#include "gdb_ptrace.h"
 
 /* Stores the ptrace options supported by the running kernel.
    A value of -1 means we did not check for features yet.  A value
diff --git a/gdb/nat/linux-ptrace.h b/gdb/nat/linux-ptrace.h
index 0a23bcb0fc4..d84114b4881 100644
--- a/gdb/nat/linux-ptrace.h
+++ b/gdb/nat/linux-ptrace.h
@@ -21,6 +21,7 @@
 struct buffer;
 
 #include "nat/gdb_ptrace.h"
+#include "gdb_wait.h"
 
 #ifdef __UCLIBC__
 #if !(defined(__UCLIBC_HAS_MMU__) || defined(__ARCH_HAS_MMU__))
END
      ;;

    "7.10.1")
      ${dryrun} patch --directory=${build} --strip 1 <<END
diff --git a/gdb/nat/linux-ptrace.c b/gdb/nat/linux-ptrace.c
index 1a926f93156..43d5fbfc731 100644
--- a/gdb/nat/linux-ptrace.c
+++ b/gdb/nat/linux-ptrace.c
@@ -21,7 +21,6 @@
 #include "linux-procfs.h"
 #include "linux-waitpid.h"
 #include "buffer.h"
-#include "gdb_wait.h"
 
 /* Stores the ptrace options supported by the running kernel.
    A value of -1 means we did not check for features yet.  A value
diff --git a/gdb/nat/linux-ptrace.h b/gdb/nat/linux-ptrace.h
index be6c39528c9..681b1663f62 100644
--- a/gdb/nat/linux-ptrace.h
+++ b/gdb/nat/linux-ptrace.h
@@ -21,6 +21,7 @@
 struct buffer;
 
 #include <sys/ptrace.h>
+#include "gdb_wait.h"
 
 #ifdef __UCLIBC__
 #if !(defined(__UCLIBC_HAS_MMU__) || defined(__ARCH_HAS_MMU__))
END
      ;;
  esac

  # Fix wrong include on Mac
  ${dryrun} find "${build}" -name "darwin-nat.c" -type f -exec sed -i -e "s/machine\/setjmp.h/setjmp.h/g" {} \;

  # Fix change in const: https://sourceware.org/bugzilla/show_bug.cgi?id=20491
  ${dryrun} find "${build}/gdb" -type f '(' -name '*.c' -or -name '*.h' ')' -exec sed -i -e 's/ps_get_thread_area (const struct ps_prochandle/ps_get_thread_area (struct ps_prochandle/g' {} \;
}

# Run ./configure.
#
# $1: version number
function configure_gdb() {
  local version="$1"

  local src="${src_dir}/gdb-${version}"
  local build="${build_dir}/gdb-${version}"
  local cflags="-Wno-error -g3 -O0"
  local cxxflags="-Wno-error -g3 -O0"

  echo_header "Configuring ${src} in ${build}"

  ${dryrun} mkdir -p "${build}"
  ${dryrun} pushd "${build}"

  case "${version}" in
    "6.7.1"|"6.8")
      cflags="${cflags} -Wno-error=enum-compare"
      ;;
  esac

  # If there is already some CFLAGS/CXXFLAGS in the environment, add them to the mix.
  cflags="${cflags} ${CFLAGS:-}"
  cxxflags="${cxxflags} ${CXXFLAGS:-}"

  # Need to use eval to allow the ${dryrun} trick to work with the env var command at the start.
  eval ${dryrun} 'CFLAGS="${cflags}" CXXFLAGS="${cxxflags}" ${src}/configure --prefix="${install_dir}/gdb-${version}" --enable-werror=no'

  ${dryrun} popd
}


# Build gdb.
#
# $1: version number
function make_gdb() {
  local version="$1"

  local build="${build_dir}/gdb-${version}"

  echo_header "Making in ${build}"

  ${dryrun} pushd "${build}"

  ${dryrun} make -j "${jlevel}"

  ${dryrun} popd
}


# Run make install.
#
# $1: version number
function make_install_gdb() {
  local version="$1"

  # Only install gdb and gdbserver (if present for GDB 10.1+), not the whole binutils-gdb
  local install_gdb="${build_dir}/gdb-${version}/gdb"
  local install_gdbserver="${build_dir}/gdb-${version}/gdbserver"

  echo_header "Make installing in ${install_gdb}"

  ${dryrun} pushd "${install_gdb}"

  # Disable building of the doc, which fails anyway with older gdbs and
  # newer makeinfos.
  ${dryrun} make install MAKEINFO=true

  ${dryrun} popd

  # XX this does not dryrun properly as the directory won't exist until it is built
  if [ -e ${install_gdbserver} ]; then
    echo_header "Make installing in ${install_gdbserver}"

    ${dryrun} pushd "${install_gdbserver}"

    # Disable building of the doc, which fails anyway with older gdbs and
    # newer makeinfos.
    ${dryrun} make install MAKEINFO=true

    ${dryrun} popd
  fi
}


# Create symlinks in "bin" directory.
#
# $1: version number
function symlink_gdb() {
  local version="$1"

  echo_header "Creating symlinks for gdb ${version} in ${symlinks_dir}"

  ${dryrun} mkdir -p "${symlinks_dir}"
  ${dryrun} ln -sf "${install_dir}/gdb-${version}/bin/gdb" "${symlinks_dir}/gdb.${version}"
  ${dryrun} ln -sf "${install_dir}/gdb-${version}/bin/gdbserver" "${symlinks_dir}/gdbserver.${version}"

  # If the version is a triplet (x.y.z), also create a symlink with just
  # the first two numbers (x.y).
  if [[ "$version" =~ [0-9]+\.[0-9]+\.[0-9]+ ]]; then
    local short_version="${version%.[0-9]}"
    ${dryrun} ln -sf "${install_dir}/gdb-${version}/bin/gdb" "${symlinks_dir}/gdb.${short_version}"
    ${dryrun} ln -sf "${install_dir}/gdb-${version}/bin/gdbserver" "${symlinks_dir}/gdbserver.${short_version}"
  fi

  # If the version is > 9.x, then make a symlink based on GDB new numbering scheme which is MAJOR.PATCH
  local maybe_major_version="${version%.[0-9]}"
  local major_version="${maybe_major_version%.[0-9]}"
  if (( "$major_version" >= 9 )) ; then
    ${dryrun} ln -sf "${install_dir}/gdb-${version}/bin/gdb" "${symlinks_dir}/gdb.${major_version}"
    ${dryrun} ln -sf "${install_dir}/gdb-${version}/bin/gdbserver" "${symlinks_dir}/gdbserver.${major_version}"
  fi
}

# Start argument parsing.  The script will exit (thanks to errexit) if bad arguments are passed.
args=$(getopt -o b:dhj: -l "base-dir:,dry-run,help,jobs" -n "$0" -- "$@");

eval set -- "$args"

while true; do
  case "$1" in
  -b|--base-dir)
    shift
    base_dir="$1"
    shift
    ;;
  -d|--dry-run)
    dryrun="echo"
    shift
    ;;
  --download)
    download_only="yes"
    shift
    ;;
  -h|--help)
    help_and_exit 0
    break
    ;;
  -j|--jobs)
    shift
    jlevel="$1"
    shift
    ;;
  --)
    shift;
    break;
    ;;
  esac
done

abs_base_dir=$(readlink -f "${base_dir}")

# Where we download the tarballs
download_dir="${base_dir}/download"

# Where we extract the tarballs and build
src_dir="${base_dir}/src"

# Where we build
build_dir="${base_dir}/build"

# Where we make install to
install_dir="${abs_base_dir}/install"

# Where we will create symlinks to all gdb versions (in the form gdb.x.y)
# (Hint: this is so you can add this directory to your PATH and have all
#  versions available quickly.)
symlinks_dir="${base_dir}/bin"

if [ $# -eq 0 ]; then
  echo "Error: you need to specify at least one gdb version or \"all\"."
  echo ""
  help_and_exit 1
fi

versions=$*

if [ "$versions" = "all" ]; then
  versions="${default_versions}"
fi

# End argument parsing

for version in $versions; do
  check_supported "$version"
done

for version in $versions; do
  download_gdb "$version"
  if [ "$download_only" = "no" ]; then
    extract_gdb "$version"
    fixup_gdb "$version"
    configure_gdb "$version"
    make_gdb "$version"
    make_install_gdb "$version"
    symlink_gdb "$version"
  fi
done

echo_header "Done!"
echo ""
if [ "$download_only" = "no" ]; then
  echo "gdb versions built:"
  echo "  ${versions}"
  echo ""
  echo "Symbolic links to binaries have been created in:"
  echo "  ${symlinks_dir}"
  echo ""
  echo "You can add this path to your \$PATH to access them easily."
  echo ""
fi
