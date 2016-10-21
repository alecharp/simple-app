#!groovy

docker.image('alecharp/java-build-tools:7a7e8f9').inside {
  stage('Checkout') {
    checkout scm
    short_commit = sh(script: 'git rev-parse --short HEAD', returnStdout: true).trim()
    currentBuild.description = "#${short_commit}"
  }

  stage('Build') {
    sh 'mvn clean package -Dmaven.test.skip=true'
    archiveArtifacts artifacts: 'target/*.jar', fingerprint: true
  }
}
