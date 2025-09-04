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

        stage('Build Core DTO') {
            steps {
                sh '''
                    # Создаем отдельную директорию вне workspace
                    mkdir -p /tmp/core-dto-build
                    cd /tmp/core-dto-build

                    # Клонируем и собираем core-api-dto
                    git clone https://github.com/Consort-Group-Corp/core_api_dto.git
                    cd core_api_dto
                    chmod +x gradlew
                    ./gradlew publishToMavenLocal

                    # Очищаем временную директорию
                    cd /
                    rm -rf /tmp/core-dto-build
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
        success {
            echo '✅ Build successful! Deployment ready!'
        }
        failure {
            echo '❌ Build failed! Check the logs!'
        }
    }
}