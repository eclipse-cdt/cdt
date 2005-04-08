cd `dirname $0`

mkdir -p tools
cd tools
cvs -d:pserver:anonymous@dev.eclipse.org:/home/eclipse \
	checkout -rv20050405 org.eclipse.releng.basebuilder
cd ..

java -cp tools/org.eclipse.releng.basebuilder/startup.jar org.eclipse.core.launcher.Main \
	-ws gtk -application org.eclipse.ant.core.antRunner $* 2>&1 | tee build.log
