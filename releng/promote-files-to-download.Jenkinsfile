// Based on https://wiki.eclipse.org/Jenkins#Pipeline_job_without_custom_pod_template
pipeline {
  agent any
  parameters {
    booleanParam(defaultValue: true, description: 'Do a dry run of the build. All commands will be echoed.First run with this on, then when you are sure it is right, choose rebuild in the passing job and uncheck this box', name: 'DRY_RUN')
  }
  options {
    timestamps()
  }
  stages {
    stage('Upload') {
      steps {
        sshagent ( ['projects-storage.eclipse.org-bot-ssh']) {
          sh './releng/scripts/promote-files-to-download.sh'
        }
      }
    }
  }
}
