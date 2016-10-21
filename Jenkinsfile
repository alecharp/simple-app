#!groovy

docker.image('alecharp/java-build-tools:7a7e8f9').inside {
  stage('Checkout') {
    checkout scm
    short_commit = sh(script: 'git rev-parse --short HEAD', returnStdout: true).trim()
    currentBuild.description = "#${short_commit}"
  }
  stash name: 'project', includes: '**', excludes: 'target'

  stage('Build') {
    sh 'mvn clean package -Dmaven.test.skip=true'
    archiveArtifacts artifacts: 'target/*.jar', fingerprint: true
  }
}

stage('Tests') {
  parallel 'UnitTests': {
    docker.image('alecharp/java-build-tools:7a7e8f9').inside {
      unstash 'project'
      sh 'mvn clean test'
      junit 'target/surefire-reports/*.xml'
    }
  }, 'IntegrationTests': {
    docker.image('alecharp/java-build-tools:7a7e8f9').inside {
      unstash 'project'
      sh 'mvn clean test-compile failsafe:integration-test'
      junit 'target/failsafe-reports/*.xml'
    }
  }
}
