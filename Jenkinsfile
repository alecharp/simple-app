#!groovy

node {
  stage('Checkout') {
    checkout scm
  }

  sh 'git rev-parse --short HEAD > GIT_COMMIT'
  short_commit = readFile('GIT_COMMIT').trim()
  sh 'rm GIT_COMMIT'

  def pom = readMavenPom file:'pom.xml'
  currentBuild.description = "${pom.getVersion()} - ${short_commit}"

  stage('Build') {
    withMaven {
      sh 'mvn clean package -Dmaven.test.skip=true'
    }
  }

  dir('target') {
    archive "${pom.getArtifactId()}.${pom.getPackaging()}"
  }

  stash name: 'binary', includes: "target/${pom.getArtifactId()}.${pom.getPackaging()}"
  dir('src/main/docker') {
    stash name: 'dockerfile', includes: 'Dockerfile'
  }

  stage('Tests') {
    parallel 'unit-tests': {
      withMaven {
        sh 'mvn clean test'
        step([$class: 'JUnitResultArchiver', testResults: 'target/surefire-reports/*.xml'])
      }
    }, 'integration-tests': {
      withMaven {
        sh 'mvn clean test-compile failsafe:integration-test'
        step([$class: 'JUnitResultArchiver', testResults: 'target/failsafe-reports/*.xml'])
      }
    }
  }
}

node('docker') {
  unstash 'dockerfile'
  unstash 'binary'

  stage('Building Docker Img') {
    image = docker.build("alecharp/simpleapp:${short_commit}")
  }

  container = image.run('-P')
  ip = container.port(8080)
}

stage('Container validation') {
  try {
    input message: "http://${ip}. Is it ok?", ok: 'Publish it'
  } finally {
      node('docker') { container.stop() }
  }
}

node('docker') {
  stage('Publishing Docker Img') {
    // requirement: local docker registry available on port 5000
    docker.withRegistry('http://localhost:5000', '') {
      image.push('latest')
    }
  }
}

// Custom step
def withMaven(def body) {
  def javaHome = tool name: 'oracle-8u77', type: 'hudson.model.JDK'
  def mavenHome = tool name: 'maven-3.3.9', type: 'hudson.tasks.Maven$MavenInstallation'

  withEnv(["JAVA_HOME=${javaHome}", "PATH+MAVEN=${mavenHome}/bin"]) {
      body.call()
  }
}
