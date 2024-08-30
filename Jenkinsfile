pipeline {
  agent any

  parameters {
    booleanParam(name: 'coverage', defaultValue: true, description: 'Calculate code coverage')
  }

  environment {
    // artifactory creds
    artifactoryCred = credentials('artifactory-na')
    ARTIFACTORY_USERNAME = "$artifactoryCred_USR"
    ARTIFACTORY_API_KEY = "$artifactoryCred_PSW"
    // empty values are not added to env, dummy property is needed
    args = "${params.coverage ? '-Pcoverage' : '-Pnocoverage'}"
    branch = "${GIT_BRANCH}".minus(~/origin\\//)
    main_branch = 'master'
    skipStage = true
  }

  stages {
    stage('Pre') {
      steps{
        sh 'printenv | sort'
        echo(message: 'skipStage: ' + !env.skipStage)
      }
    }

    stage ('Test') {
      when {
        expression { return !env.skipStage }
        environment name: 'GIT_LOCAL_BRANCH', value: "${main_branch}"
        environment name: 'GIT_BRANCH', value: "origin/${main_branch}"
        branch "${main_branch}"
        // build marked as UNSTABLE by junit plugin does not stop the pipeline
        expression { return currentBuild.currentResult == 'SUCCESS' }
      }

      steps{
        echo(message: 'Hello Jenkins')
      }
    }
  }
}
