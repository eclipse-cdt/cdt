#!/bin/sh
#
# script to update versions
#
# Usage: user_id version [branch]
#
# user_id - user id to use to clone repo
# version - new version string (e.g. "5.0.1")
# branch - alternate branch to use when updating versions
#
# Note: a "qualifier" suffix will automatically be added to the version where appropriate
#
# If version numbers are updated on a branch other than master, it is recommended to run
# the command from the master branch:
#
#	 git merge -s ours origin/branch
#
# to mark the changes as merged.
#

BRANCH=master

if [ $# -lt 3 ]; then
	echo "usage: update_versions user_id version  [branch]"
	exit 1
fi

user_id=$1
version=$2

if [ $# -gt 2 ]; then
	BRANCH=$3
fi

if [ -d fix_versions ]; then
	echo "please remove fix_versions first"
	exit 1
fi

mkdir fix_versions
cd fix_versions

git clone ssh://${user_id}@git.eclipse.org:29418/ptp/org.eclipse.remote.git
(cd org.eclipse.remote && git checkout $BRANCH)

update_feature() {
	sed -e "s/^\([ \t]*\)version=\"[0-9]\.[0-9]\.[0-9]\.qualifier\"/\1version=\"$2\.qualifier\"/" < $1/feature.xml > $1/feature.xml.tmp
	mv $1/feature.xml.tmp $1/feature.xml
}

update_manifest() {
	sed -e "s/^Bundle-Version: *[0-9]\.[0-9]\.[0-9]\.qualifier/Bundle-Version: $2.qualifier/" < $1/META-INF/MANIFEST.MF > $1/META-INF/MANIFEST.MF.tmp
	mv $1/META-INF/MANIFEST.MF.tmp $1/META-INF/MANIFEST.MF
}

for feature in org.eclipse.remote/features/*-feature; do
	echo "Updating $feature..."
	update_feature $feature $version
done

(cd org.eclipse.remote/releng/org.eclipse.remote.build && \
	mvn versions:set -DnewVersion="${version}-SNAPSHOT" && \
	mvn org.eclipse.tycho:tycho-versions-plugin:0.14.0:update-pom)

find . -name pom.xml.versionsBackup -exec rm -f {} \;

#(cd org.eclipse.remote && git commit -m "Update versions" && git push)

#
# Cleanup
#
#cd ..
#rm -rf fix_versions

exit 0
