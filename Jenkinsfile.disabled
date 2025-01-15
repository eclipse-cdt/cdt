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
    stage('initialize PGP') {
      steps {
        container('cdt') {
          withCredentials([file(credentialsId: 'secret-subkeys.asc', variable: 'KEYRING')]) {
            sh 'gpg --batch --import "${KEYRING}"'
            sh 'for fpr in $(gpg --list-keys --with-colons  | awk -F: \'/fpr:/ {print $10}\' | sort -u); do echo -e "5\ny\n" |  gpg --batch --command-fd 0 --expert --edit-key ${fpr} trust; done'
          }
        }
      }
    }
    stage('Code Formatting Checks') {
      steps {
        container('cdt') {
          timeout(activity: true, time: 30) {
            withEnv(['MAVEN_OPTS=-XX:MaxRAMPercentage=50.0 -XX:+PrintFlagsFinal']) {
              sh 'MVN="/jipp/tools/apache-maven/latest/bin/mvn -Dmaven.repo.local=/home/jenkins/.m2/repository \
                        --settings /home/jenkins/.m2/settings.xml" ./releng/scripts/check_code_cleanliness_only.sh'
            }
          }
        }
      }
    }
    stage('Build and verify') {
      steps {
        container('cdt') {
          timeout(activity: true, time: 20) {
            withEnv(['MAVEN_OPTS=-XX:MaxRAMPercentage=50.0 -XX:+PrintFlagsFinal']) {
              withCredentials([string(credentialsId: 'gpg-passphrase', variable: 'KEYRING_PASSPHRASE')]) {
                sh '''/jipp/tools/apache-maven/latest/bin/mvn \
                      clean verify -B -V \
                      -Dgpg.passphrase="${KEYRING_PASSPHRASE}"  \
                      -Dmaven.test.failure.ignore=true \
                      -DexcludedGroups=flakyTest,slowTest \
                      -P baseline-compare-and-replace \
                      -P api-baseline-check \
                      -Ddsf.gdb.tests.timeout.multiplier=50 \
                      -Dindexer.timeout=300 \
                      -P production \
                      -P build-standalone-debugger-rcp \
                      -Ddsf.gdb.tests.gdbPath=/shared/common/gdb/gdb-all/bin \
                      -Dcdt.tests.dsf.gdb.versions=gdb.10,gdbserver.10 \
                      -Dmaven.repo.local=/home/jenkins/.m2/repository \
                      --settings /home/jenkins/.m2/settings.xml \
                      '''
              }
            }
          }
        }
      }
    }
  }
  post {
    always {
      container('cdt') {
        archiveArtifacts '*.log,native/org.eclipse.cdt.native.serial/**,core/org.eclipse.cdt.core.*/**,*/*/target/surefire-reports/**,terminal/plugins/org.eclipse.tm.terminal.test/target/surefire-reports/**,**/target/work/data/.metadata/.log,releng/org.eclipse.cdt.repo/target/org.eclipse.cdt.repo.zip,releng/org.eclipse.cdt.repo/target/repository/**,releng/org.eclipse.cdt.testing.repo/target/org.eclipse.cdt.testing.repo.zip,releng/org.eclipse.cdt.testing.repo/target/repository/**,debug/org.eclipse.cdt.debug.application.product/target/product/*.tar.gz,debug/org.eclipse.cdt.debug.application.product/target/products/*.zip,debug/org.eclipse.cdt.debug.application.product/target/products/*.tar.gz,debug/org.eclipse.cdt.debug.application.product/target/repository/**'
        junit '*/*/target/surefire-reports/*.xml,terminal/plugins/org.eclipse.tm.terminal.test/target/surefire-reports/*.xml'
      }
    }
  }
}
