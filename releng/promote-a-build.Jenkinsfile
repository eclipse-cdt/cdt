// Based on https://wiki.eclipse.org/Jenkins#Pipeline_job_without_custom_pod_template
pipeline {
  agent any
  parameters {
    booleanParam(defaultValue: true, description: 'Do a dry run of the build. All commands will be echoed.First run with this on, then when you are sure it is right, choose rebuild in the passing job and uncheck this box', name: 'DRY_RUN')
    string(defaultValue: '9.8', description: 'The major and minor version of CDT being released (e.g. 11.3, 12.0, cdt-lsp-1.0, cdt-lsp-1.1).', name: 'MINOR_VERSION')
    string(defaultValue: 'cdt-9.8.0', description: 'The full name of this release (e.g. cdt-9.4.2, cdt-9.5.0-rc1, cdt-lsp-1.0.0, cdt-lsp-1.0.0-rc1)', name: 'MILESTONE')
    choice(choices: ['cdt', 'cdt-lsp'], description: 'The repo being published', name: 'CDT_REPO')
    string(defaultValue: 'main', description: 'The repo branch being published (e.g. main, master, cdt_11_3)', name: 'CDT_BRANCH')
    string(defaultValue: '12345', description: 'The CI build number being promoted from', name: 'CDT_BUILD_NUMBER')
    choice(choices: ['releases', 'builds'], description: 'Publish location (releases or builds)', name: 'RELEASE_OR_BUILD')
  }
  options {
    timestamps()
    disableConcurrentBuilds()
  }
  stages {
    stage('Upload') {
      steps {
        script {
          def jobName = "$CDT_REPO/job/$CDT_BRANCH"
          def description = "Promoted as $MILESTONE to <a href='https://download.eclipse.org/tools/cdt/$RELEASE_OR_BUILD/$MINOR_VERSION/$MILESTONE'>download.eclipse.org/tools/cdt/$RELEASE_OR_BUILD/$MINOR_VERSION/$MILESTONE</a>"
          if (params.DRY_RUN) {
            description = "Dry Run: $description"
          }

          // TODO: Can we get permission from EF IT to use Jenkins.instance.getItemByFullName
          // def job = Jenkins.instance.getItemByFullName(jobName)
          // def build = job?.getBuildByNumber(buildNumber)
          
          // if (build) {
          //     build.setDescription(description)
          //     build.keepLog(true)
          // } else {
          //     echo "Build not found: ${jobName} #${CDT_BUILD_NUMBER}"
          // }
          currentBuild.description = "$description from <a href='https://ci.eclipse.org/cdt/job/$CDT_REPO/job/$CDT_BRANCH/$CDT_BUILD_NUMBER'>$CDT_REPO/job/$CDT_BRANCH/$CDT_BUILD_NUMBER</a>"
        }
        sshagent ( ['projects-storage.eclipse.org-bot-ssh']) {
          sh './releng/scripts/promote-a-build.sh'
        }
      }
    }
  }
}
