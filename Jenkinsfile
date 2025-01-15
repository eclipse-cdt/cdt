pipeline {
  agent {
    kubernetes {
      yamlFile 'jenkins/pod-templates/cdt-full-pod-plus-eclipse-install.yaml'
    }
  }
  options {
    timestamps()
    buildDiscarder(logRotator(numToKeepStr: '10', artifactNumToKeepStr: '2'))
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
                // XXX: Issue 684 means that dsf-gdb tests are skipped
                sh '''/jipp/tools/apache-maven/latest/bin/mvn \
                      clean verify -B -V \
                      -Ddsf-gdb.skip.tests=true \
                      -Dgpg.passphrase="${KEYRING_PASSPHRASE}"  \
                      -Dmaven.test.failure.ignore=true \
                      -DexcludedGroups=flakyTest,slowTest \
                      -P baseline-compare-and-replace \
                      -P api-baseline-check \
                      -Ddsf.gdb.tests.timeout.multiplier=50 \
                      -Dindexer.timeout=300 \
                      -P production \
                      -Ddsf.gdb.tests.gdbPath=/shared/common/gdb/gdb-all/bin \
                      -Dcdt.tests.dsf.gdb.versions=gdb.10,gdbserver.10 \
                      -Dmaven.repo.local=/home/jenkins/.m2/repository \
                      --settings /home/jenkins/.m2/settings.xml \
                      '''
                sh '''
                  echo "TIMESTAMP: $(date)" > releng/org.eclipse.cdt.repo/target/repository/ci-and-git-info.txt
                  echo "CI URL: ${BUILD_URL}" >> releng/org.eclipse.cdt.repo/target/repository/ci-and-git-info.txt
                  echo "Most recent git commits: (output of  git log --graph --pretty='tformat:%h [%ci] - %s' -20)"  >> releng/org.eclipse.cdt.repo/target/repository/ci-and-git-info.txt
                  git log --graph --pretty='tformat:%h [%ci] - %s' -20 | tee -a releng/org.eclipse.cdt.repo/target/repository/ci-and-git-info.txt
                '''
              }
            }
          }
        }
      }
    }
    stage('Deploy Snapshot') {
      steps {
        container('jnlp') {
          timeout(activity: true, time: 20) {
            sshagent ( ['projects-storage.eclipse.org-bot-ssh']) {
              sh '''
                  SSHUSER="genie.cdt@projects-storage.eclipse.org"
                  SSH="ssh ${SSHUSER}"
                  SCP="scp"


                  DOWNLOAD=download.eclipse.org/tools/cdt/builds/cdt/$BRANCH_NAME
                  DOWNLOAD_MOUNT=/home/data/httpd/$DOWNLOAD

                  # Deploying build to nightly location on download.eclipse.org
                  if $SSH test -e ${DOWNLOAD_MOUNT}-new; then
                      $SSH rm -r ${DOWNLOAD_MOUNT}-new
                  fi
                  if $SSH test -e ${DOWNLOAD_MOUNT}-last; then
                      $SSH rm -r ${DOWNLOAD_MOUNT}-last
                  fi
                  $SSH mkdir -p ${DOWNLOAD_MOUNT}-new
                  $SCP -rp releng/org.eclipse.cdt.repo/target/repository/* "${SSHUSER}:"${DOWNLOAD_MOUNT}-new
                  $SCP -rp releng/org.eclipse.cdt.repo/target/org.eclipse.cdt.repo.zip "${SSHUSER}:"${DOWNLOAD_MOUNT}-new
                  if $SSH test -e ${DOWNLOAD_MOUNT}; then
                      $SSH mv ${DOWNLOAD_MOUNT} ${DOWNLOAD_MOUNT}-last
                  fi
                  $SSH mv ${DOWNLOAD_MOUNT}-new ${DOWNLOAD_MOUNT}
              '''
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
