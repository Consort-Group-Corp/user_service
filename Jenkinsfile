pipeline {
    agent any
    tools {
        jdk 'jdk-21'
        gradle 'gradle-8'
    }

    triggers {
        pollSCM('')
    }

    stages {
        stage('Checkout') {
            steps {
                git branch: 'main',
                url: 'https://github.com/Consort-Group-Corp/user_service.git',
                credentialsId: 'cfc4e90d-b61c-4d35-926e-b6c1746281a1'
            }
        }

        // ДОБАВЬТЕ ЭТОТ ЭТАП ↓
        stage('Build Core DTO') {
            steps {
                sh '''
                    git clone https://github.com/Consort-Group-Corp/core_api_dto.git
                    cd core_api_dto
                    chmod +x gradlew
                    ./gradlew publishToMavenLocal
                    cd ..
                '''
            }
        }

        stage('Fix permissions') {
            steps {
                sh 'chmod +x gradlew'
            }
        }

        stage('Build') {
            steps {
                sh './gradlew clean build -x test'
            }
        }

        stage('Test') {
            steps {
                sh './gradlew test'
            }
        }

        stage('Package') {
            steps {
                sh './gradlew bootJar'
            }
        }

        stage('Archive Artifact') {
            steps {
                archiveArtifacts artifacts: 'build/libs/*.jar', fingerprint: true
            }
        }
    }

    post {
        always {
            cleanWs()
        }
    }
}