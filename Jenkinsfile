#!groovy

properties([
  [$class: 'GithubProjectProperty', displayName: 'Simple Application', projectUrlStr: 'https://github.com/alecharp/simple-app/'],
  buildDiscarder(logRotator(artifactNumToKeepStr: '5', daysToKeepStr: '15'))
])

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
    stash name: 'docker', includes: 'src/main/docker/Dockerfile, target/*.jar'
  }
}

stage('Tests') {
  parallel 'Unit tests': {
    docker.image('alecharp/java-build-tools:7a7e8f9').inside {
      unstash 'project'
      sh 'mvn clean test'
      junit 'target/surefire-reports/*.xml'
    }
  }, 'Integration tests': {
    docker.image('alecharp/java-build-tools:7a7e8f9').inside {
      unstash 'project'
      sh 'mvn clean test-compile failsafe:integration-test'
      junit 'target/failsafe-reports/*.xml'
    }
  }
}


stage('Build Docker img') {
  node {
    unstash 'docker'
    image = docker.build("alecharp/simple-app:${short_commit}", '-f src/main/docker/Dockerfile .')
  }
}

stage('Validate Docker img') {
  node {
    container = image.run('-P')
    ip = container.port(8080)
  }
  try {
    input message: "Is http://${ip} ok?", ok: 'Publish'
  } finally {
    node { container.stop() }
  }
}

stage('Publish Docker img') {
  node {
    docker.withRegistry('http://localhost:5000') {
      image.push "${short_commit}"
    }
  }
}
