#!/bin/bash

# * Copyright (c) 2014 Ericsson and others.
# * All rights reserved. This program and the accompanying materials
# * are made available under the terms of the Eclipse Public License v1.0
# * which accompanies this distribution, and is available at
# * http://www.eclipse.org/legal/epl-v10.html
# *
# * Contributors:
# *     Simon Marchi (Ericsson) - Initial implementation

# Stop the script if any command fails
set -e

# Usage: ./download-build-gdb.sh [VERSIONS | all]
#
# Examples:
#   ./download-build-gdb.sh 7.6.2 7.7.1 7.8.1  # Download and build those versions
#   ./download-build-gdb.sh all  # Download all versions supported by the CDT tests
#
# Settings that you might want to change:
# Our work directory
base_dir="$HOME/gdb-all"

# Where we download the tarballs
download_dir="${base_dir}/download"

# Where we extract the tarballs and build
build_dir="${base_dir}/build"

# Where we make install to
install_dir="${base_dir}/install"

# Where we will create symlinks to all gdb versions (in the form gdb.x.y)
# (Hint: this is so you can add this directory to your PATH and have all
#  versions available quickly.)
symlinks_dir="${base_dir}/bin"

# Passed to make's -j
jlevel="4"

# End of what you might want to change.

# Default versions (for when "all" is used)
default_versions="6.6 6.7.1 6.8 7.0.1 7.1 7.2 7.3.1 7.4.1 7.5.1 7.6.2 7.7.1 7.8.1"

if [ $# -eq 0 ]; then
  echo "Please specify at least one gdb version or 'all' for all supported gdb."
  exit 1
fi

versions=$*

if [ "$versions" = "all" ]; then
  versions="${default_versions}"
fi

# Output a visible header
function echo_header() {
  echo -e "\e[1m\e[7m>>> $1\e[0m"
}


# Check that the version passed is supported by the script.
#
# $1: version number
function check_supported() {
  case "$version" in
    "6.6"|"6.7.1"|"6.8"|"7.0.1"|"7.1"|"7.2"|"7.3.1"|"7.4.1"|"7.5.1"|"7.6.2"|"7.7.1"|"7.8.1")
      # Supported, do nothing.
      ;;
    *)
      echo "Version ${version} is not supported, sorry."
      exit 1
      ;;
  esac
}


# Download the tarball of the given release of gdb.
#
# $1: version number
function download_gdb() {
  local baseaddr="http://ftp.gnu.org/gnu/gdb"
  local version="$1"

  case "$version" in
    "6.6"|"6.7.1"|"6.8"|"7.0.1"|"7.1"|"7.2")
      version="${version}a"
      ;;
  esac

  echo_header "Downloading gdb $version to ${download_dir}"

  wget --timestamping --directory-prefix "${download_dir}" "${baseaddr}/gdb-${version}.tar.gz"
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

  echo_header "Extracting ${archive} to ${build_dir}"

  mkdir -p "${build_dir}"

  tar -xf "${archive}" -C "${build_dir}"
}

# Make necessary fixes to build an "old" release on a "modern" system.
#
# $1: version number
function fixup_gdb() {
  local version="$1"
  local build="${build_dir}/gdb-${version}"

  echo_header "Fixing up gdb ${version}"

  # Disable building of the doc, which fails anyway with older gdbs and
  # newer makeinfos.
  find ${build} -name '*.texinfo' -exec cp "/dev/null" "{}" \;

  # This file tries to include a non-existent file.
  if [ "$version" = "7.2" ]; then
    cp "/dev/null" "${build}/etc/standards.texi"
  fi

  # glibc or the kernel changed the signal API at some point
  case "$version" in
    "6.6"|"6.7.1"|"6.8"|"7.0.1"|"7.1"|"7.2"|"7.3.1"|"7.4.1")
      find "${build}" -type f -exec sed -i -e 's/struct siginfo;/#include <signal.h>/g' {} \;
      find "${build}" -type f -exec sed -i -e 's/struct siginfo/siginfo_t/g' {} \;
      ;;
  esac
}


# Run ./configure.
#
# $1: version number
function configure_gdb() {
  local version="$1"

  local build="${build_dir}/gdb-${version}"
  local cflags="-Wno-error -g -O0"

  echo_header "Configuring in ${build}"

  pushd "${build}"

  case "${version}" in
    "6.7.1"|"6.8")
      cflags="$cflags -Wno-error=enum-compare"
      ;;
  esac

  CFLAGS="${cflags}" ./configure --prefix="${install_dir}/gdb-${version}"

  popd
}


# Build gdb.
#
# $1: version number
function make_gdb() {
  local version="$1"

  local build="${build_dir}/gdb-${version}"

  echo_header "Making in ${build}"

  pushd "${build}"

  make -j "${jlevel}"

  popd
}


# Run make install.
#
# $1: version number
function make_install_gdb() {
  local version="$1"

  # Only install gdb, not the whole binutils-gdb
  local install="${build_dir}/gdb-${version}/gdb"

  echo_header "Make installing in ${install}"

  pushd "${install}"

  make install

  popd
}


# Create symlinks in "bin" directory.
#
# $1: version number
function symlink_gdb() {
  local version="$1"

  echo_header "Creating symlinks for gdb ${version} in ${symlinks_dir}"

  mkdir -p "${symlinks_dir}"
  ln -sf "${install_dir}/gdb-${version}/bin/gdb" "${symlinks_dir}/gdb.${version}"
  ln -sf "${install_dir}/gdb-${version}/bin/gdbserver" "${symlinks_dir}/gdbserver.${version}"

  # If the version is a triplet (x.y.z), also create a symlink with just
  # the first two numbers (x.y).
  if [[ "$version" =~ [0-9]\.[0-9]\.[0-9] ]]; then
    local short_version="${version%.[0-9]}"
    ln -sf "${install_dir}/gdb-${version}/bin/gdb" "${symlinks_dir}/gdb.${short_version}"
    ln -sf "${install_dir}/gdb-${version}/bin/gdbserver" "${symlinks_dir}/gdbserver.${short_version}"
  fi
}


for version in $versions; do
  check_supported "$version"
done

for version in $versions; do
  download_gdb "$version"
  extract_gdb "$version"
  fixup_gdb "$version"
  configure_gdb "$version"
  make_gdb "$version"
  make_install_gdb "$version"
  symlink_gdb "$version"
done
