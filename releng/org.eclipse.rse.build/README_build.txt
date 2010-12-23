Instructions for building TM and RSE
------------------------------------

1. Set up the build workspace
-----------------------------
ssh build.eclipse.org
cd /shared/tools/tm/
mkdir ws2_user
cd ws2_user
ln -s `pwd` $HOME/ws2
ln -s /home/data/httpd/download.eclipse.org/tm $HOME/downloads-tm
wget -O setup.sh "http://dev.eclipse.org/viewcvs/index.cgi/org.eclipse.tm.rse/releng/org.eclipse.rse.build/setup.sh?rev=HEAD&cvsroot=Tools_Project&content-type=text/plain"
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
cd ws2/publish/S-3.1RC3*
mv package.count.orig package.count

3c) Promote an update site
--------------------------
Any build generates its update site in $HOME/downlads-tm/testUpdates
and $HOME/downloads-tm/signedUpdates . You need to manually copy these
to its target repository, then re-generate repository metadata:

cd $HOME/downloads-tm/updates/3.1milestones
rm -rf plugins features
cp -R ../../signedUpdates/plugins .
cp -R ../../signedUpdates/features .
cd bin
cvs update
./mkTestUpdates

3d) Promote an S-build to Galileo
--------------------------------
After S-build has been prepared (on signedUpdates)
On local Eclipse client, checkout Galileo projects according to
    http://wiki.eclipse.org/Galileo_Build
and edit the build contribution.

4) Convert a download to "signed" form
--------------------------------------
Normally, only the TM update site is signed whereas the downloadable ZIPs
are not. Downloadable ZIPs can be converted to signed, if the signed
update site is available and installed. See

 org.eclipse.rse.build/bin/make_signed.sh
 