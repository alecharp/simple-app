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
    stash name: 'docker', includes: 'src/main/docker/Dockerfile, target/*.jar'
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

node {
  stage('Build Docker img') {
    unstash 'docker'
    image = docker.build("alecharp/simple-app:${short_commit}", '-f src/main/docker/Dockerfile .')
  }
}

node {
  stage('Validate Docker img') {
    container = image.run('-P')
    ip = container.port(8080)
  }
}

try {
  input message: "Is http://${ip} ok?", ok: 'Publish'
} finally {
  node { container.stop() }
}

node {
  stage('Publish Docker img') {
    docker.withRegistry('http://localhost:5000') {
      image.push "${short_commit}"
    }
  }
}
