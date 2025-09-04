pipeline {
    agent any

    tools {
        jdk 'jdk-21'
        gradle 'gradle-8'
    }


    triggers {
        githubPush()
    }

    stages {
        stage('Checkout') {
            steps {
                git branch: 'main',
                url: 'https://github.com/Consort-Group-Corp/user_service.git',
                credentialsId: 'cfc4e90d-b61c-4d35-926e-b6c1746281a1'
            }
        }

        stage('Fix permissions') {
            steps {
                sh 'chmod +x gradlew'  // исправляем права на выполнение
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
            cleanWs()  // очистка workspace
        }
        success {
            echo '✅ Build successful! Deployment ready!'
            // Можно добавить уведомление в Slack/Telegram
        }
        failure {
            echo '❌ Build failed! Check the logs!'
            // Можно добавить уведомление об ошибке
        }
    }
}