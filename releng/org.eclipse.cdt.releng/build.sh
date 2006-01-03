umask 0022

cd `dirname $0`

mkdir -p tools
cd tools
cvs -d:pserver:anonymous@dev.eclipse.org:/home/eclipse \
	checkout org.eclipse.releng.basebuilder
cd ..

java -jar tools/org.eclipse.releng.basebuilder/startup.jar \
	-ws gtk -application org.eclipse.ant.core.antRunner $*
