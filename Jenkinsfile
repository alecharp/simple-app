#!/usr/bin/env groovy

properties([
  [$class: 'GithubProjectProperty', displayName: 'Simple Application', projectUrlStr: 'https://github.com/alecharp/simple-app/'],
  buildDiscarder(logRotator(artifactNumToKeepStr: '5', daysToKeepStr: '15'))
])

node {
  stage('Checkout') {
    checkout scm
    commit = sh(returnStdout:true, script:'git rev-parse --short HEAD').trim()
    currentBranch = sh(returnStdout: true, script: 'git rev-parse --abbrev-ref HEAD').trim()
    currentBuild.description = "#${commit}"
  }

  stage('Build') {
    mvn 'clean package -Dmaven.test.skip=true'
    archiveArtifacts 'target/*.jar'
    stash name: 'docker', includes: 'src/main/docker/Dockerfile, target/*.jar'
  }
}

stage('Tests') {
  parallel 'Unit tests': {
    node {
      checkout scm
      mvn 'clean test'
      junit allowEmptyResults: true, testResults: 'target/surefire-reports/*.xml'
    }
  }, 'Integration tests': {
    node {
      checkout scm
      mvn 'clean test-compile failsafe:integration-test'
      junit allowEmptyResults: true, testResults: 'target/failsafe-reports/*.xml'
    }
  }
}

stage('Build Docker img') {
  node {
    unstash 'docker'
    image = docker.build("alecharp/simple-app:${commit}", "-f src/main/docker/Dockerfile .")
  }
}

stage('Validate Docker container') {
  node {
    container = image.run('-P')
    ip = container.port(8080)
  }

  try {
    input message: "See http://${ip}. Is it ok?", ok: 'Publish'
  } finally {
    node { container.stop() }
  }
}

stage('Publish Docker img') {
  node {
    docker.withRegistry('http://localhost:5000') {
      image.push "${commit}"
      if ('master'.equals(currentBranch)) {
        milestone label: 'docker-image-latest'
        image.push "latest"
      }
    }
  }
}

if ('master'.equals(currentBranch)) {
  stage('Release') {
    milestone label: 'release only the latest build'
    def release = input message: 'Choose release parameters', ok: 'Done',
      parameters: [
        string(defaultValue: '', description: 'Version for the release', name: 'version'),
        string(defaultValue: '', description: 'Next development version', name: 'nextVersion')
      ]
    docker.image(dockerBuildImage).inside('-v /Users/adrien/.m2:/home/build/.m2') {
      checkout scm
      sh 'git config user.name Jenkins && git config user.email no-mail@example.com'
      sh "mvn clean release:prepare release:perform -B" +
        (release?.version?.trim() ? " -DreleaseVersion=" + release.version : '') +
        (release?.nextVersion?.trim() ? " -DdevelopmentVersion=" + release.nextVersion : '')
    }
  }
}

def mvn(def goals) {
  withMaven(jdk: '8u112', maven: '3.3.9') {
    sh "mvn ${goals}"
  }
}
