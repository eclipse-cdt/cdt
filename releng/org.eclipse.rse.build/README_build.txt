Instructions for building TM and RSE
------------------------------------

1. Set up the build workspace
-----------------------------
ssh build.eclipse.org
cd /shared/dsdp/tm/
mkdir ws2_user
cd ws2_user
ln -s `pws` $HOME/ws2
ln -s /home/data/httpd/download.eclipse.org/dsdp/tm $HOME/downloads-tm
cp -R ../ws2/IBMJava2-ppc-142 .
wget -O setup.sh "http://dev.eclipse.org/viewcvs/index.cgi/org.eclipse.tm.rse/releng/org.eclipse.rse.build/setup.sh?rev=HEAD&cvsroot=DSDP_Project&content-type=text/plain"
chmod a+x setup.sh
./setup.sh


2. Do an N-build
----------------
cd $HOME/ws2
./doit_nightly.sh


3. Do an I-build
----------------
On a local client PC, install Eclipse Platform Releng.Tools
Have all TM plugins in the workspace (import tm-all-committer.psf)
Synchronize CVS Workspace to HEAD
Review all incoming changes
Right-click > Team > Release...
Select Mapfile "org.eclipse.rse.build"
Press next ... tag and commit the Mapfiles
ssh build.eclipse.org
cd ws2
./doit_irsbuild.sh I
When build worked ok, tag org.eclipse.rse.build: e.g. I20070605-1200

3a) Do an S-build
-----------------
Just like I-build, but also update 
   org.eclipse.rse.build/template/buildNotes.php
ssh build.eclipse.org
cd ws2
./doit_irsbuild.sh S 2.0RC3

3b) Promote an S-build to official
----------------------------------
After testing the "invisible" S-build:
ssh build.eclipse.org
cd ws2/publish/S-2.0RC3*
mv package.count.orig package.count

3c) Promote an S-build to Europa
--------------------------------
After S-build has been prepared (on signedUpdates)
On local Eclipse client, checkout Europa projects according to
    http://wiki.eclipse.org/Europa_Build
Open file 
    org.eclipse.europa.tools/build-home/features-dsdp-tm.xml
ssh build.eclipse.org
cd downloads-tm/updates/milestones
rm -rf features.prev plugins.prev
mv features features.prev
mv plugins plugins.prev
cp -R ../../signedUpdates/features .
cp -R ../../signedUpdates/plugins .
cd bin
./mkTestUpdates.sh
From the shell where the build is ongoing, copy & Paste the 
    version numbers of the features listed in feature-dsdp-tm.xml
    into features-dsdp-tm.xml
Commit features-dsdp-tm.xml


ssh build.eclipse.org



