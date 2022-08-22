// Based on https://wiki.eclipse.org/Jenkins#Pipeline_job_without_custom_pod_template
pipeline {
  agent any
  parameters {
    booleanParam(defaultValue: true, description: 'Do a dry run of the build. All commands will be echoed.First run with this on, then when you are sure it is right, choose rebuild in the passing job and uncheck this box', name: 'DRY_RUN')
    booleanParam(defaultValue: false, description: 'Include CDT standalone debugger when publishing (if applicable)', name: 'STANDALONE')
    booleanParam(defaultValue: false, description: 'Publish only the standalon debugger. This is used to add the standalone debugger from a different job to the already published CDT release', name: 'STANDALONE_ONLY')
    string(defaultValue: '9.8', description: 'The major and minor version of CDT being released (e.g. 9.7, 9.8, 10.0).', name: 'MINOR_VERSION')
    string(defaultValue: 'cdt-9.8.0', description: 'The full name of this release (e.g. cdt-9.4.2, cdt-9.5.0-rc1, cdt-9.5.0-photon-m7, cdt-9.5.0-photon-rc1)', name: 'MILESTONE')
    string(defaultValue: 'cdt/job/main', description: 'The CI job name being promoted from (e.g. cdt/job/cdt_10_7, cdt/job/main', name: 'CDT_JOB_NAME')
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
        sshagent ( ['projects-storage.eclipse.org-bot-ssh']) {
          sh './releng/scripts/promote-a-build.sh'
        }
      }
    }
  }
}
