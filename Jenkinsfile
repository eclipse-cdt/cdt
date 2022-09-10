pipeline {
  agent {
    kubernetes {
      yamlFile 'jenkins/pod-templates/cdt-full-pod-plus-eclipse-install.yaml'
    }
  }
  options {
    timestamps()
  }
  stages {
    stage('Code Formatting Checks') {
      steps {
        container('cdt') {
          timeout(activity: true, time: 30) {
            withEnv(['MAVEN_OPTS=-XX:MaxRAMPercentage=60.0']) {
              sh 'MVN="/usr/share/maven/bin/mvn -Dmaven.repo.local=/home/jenkins/.m2/repository \
                        --settings /home/jenkins/.m2/settings.xml" ./releng/scripts/check_code_cleanliness.sh'
            }
          }
        }
      }
    }
    stage('Build and verify') {
      steps {
        container('cdt') {
          timeout(activity: true, time: 20) {
            withEnv(['MAVEN_OPTS=-XX:MaxRAMPercentage=60.0']) {
                sh "/usr/share/maven/bin/mvn \
                      clean verify -B -V \
                      -Dmaven.test.failure.ignore=true \
                      -DexcludedGroups=flakyTest,slowTest \
                      -P baseline-compare-and-replace \
                      -Ddsf.gdb.tests.timeout.multiplier=50 \
                      -Dindexer.timeout=300 \
                      -P production \
                      -Dmaven.repo.local=/home/jenkins/.m2/repository \
                      --settings /home/jenkins/.m2/settings.xml \
                      "
            }
          }
        }
      }
    }
  }
  post {
    always {
      container('cdt') {
        archiveArtifacts '*.log,native/org.eclipse.cdt.native.serial/**,core/org.eclipse.cdt.core.*/**,*/*/target/surefire-reports/**,terminal/plugins/org.eclipse.tm.terminal.test/target/surefire-reports/**,**/target/work/data/.metadata/.log,releng/org.eclipse.cdt.repo/target/org.eclipse.cdt.repo.zip,releng/org.eclipse.cdt.repo/target/repository/**,releng/org.eclipse.cdt.testing.repo/target/org.eclipse.cdt.testing.repo.zip,releng/org.eclipse.cdt.testing.repo/target/repository/**,debug/org.eclipse.cdt.debug.application.product/target/product/*.tar.gz,debug/org.eclipse.cdt.debug.application.product/target/products/*.zip,debug/org.eclipse.cdt.debug.application.product/target/products/*.tar.gz,debug/org.eclipse.cdt.debug.application.product/target/repository/**,lsp4e-cpp/org.eclipse.lsp4e.cpp.site/target/repository/**,lsp4e-cpp/org.eclipse.lsp4e.cpp.site/target/org.eclipse.lsp4e.cpp.repo.zip'
        junit '*/*/target/surefire-reports/*.xml,terminal/plugins/org.eclipse.tm.terminal.test/target/surefire-reports/*.xml'
      }
    }
  }
}
