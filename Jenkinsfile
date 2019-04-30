pipeline {
  agent any
  
  stages {
    stage('Build') {
      tools {
        maven '3.3.9'
        jdk '8u112'
      }
      
      steps {
        sh 'mvn clean verify'
      }
      
      post {
        success {
          archiveArtifact 'target/simple-app.jar'
        }
      }
    }
  }
}
