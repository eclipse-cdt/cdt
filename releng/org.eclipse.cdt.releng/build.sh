export JAVA_HOME=/usr/java/j2sdk1.4.2
java -classpath $JAVA_HOME/lib/tools.jar:jars/ant.jar:jars/optional.jar:jars/xalan.jar:jars/xml-apis.jar:jars/xercesImpl.jar:jars/NetComponents.jar org.apache.tools.ant.Main $* 2>&1 | tee build.log
