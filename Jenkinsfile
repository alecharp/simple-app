#!groovy

node {
  stage('Checkout') {
    checkout scm
    commit = sh(returnStdout:true, script:'git rev-parse --short HEAD').trim()
    currentBuild.description = "#${commit}"
  }

  stage('Build') {
    mvn('clean package -Dmaven.test.skip=true')
    archiveArtifacts 'target/*.jar'
  }
}

def mvn(def goals) {
  withMaven(jdk: '8u112', maven: '3.3.9') {
    sh "mvn ${goals}"
  }
}
