pipeline {
    agent any
    tools {
        jdk 'jdk-21'
        gradle 'gradle-8'
    }

    environment {
        ENV_FILE = '/var/jenkins_home/.env'

        POSTGRES_DB = sh(script: "grep '^POSTGRES_DB=' ${ENV_FILE} | cut -d= -f2", returnStdout: true).trim()
        POSTGRES_USER = sh(script: "grep '^POSTGRES_USER=' ${ENV_FILE} | cut -d= -f2", returnStdout: true).trim()
        POSTGRES_PASSWORD = sh(script: "grep '^POSTGRES_PASSWORD=' ${ENV_FILE} | cut -d= -f2", returnStdout: true).trim()
        DB_USERNAME = sh(script: "grep '^DB_USERNAME=' ${ENV_FILE} | cut -d= -f2", returnStdout: true).trim()
        DB_PASSWORD = sh(script: "grep '^DB_PASSWORD=' ${ENV_FILE} | cut -d= -f2", returnStdout: true).trim()
        KAFKA_CLUSTER_ID = sh(script: "grep '^KAFKA_CLUSTER_ID=' ${ENV_FILE} | cut -d= -f2", returnStdout: true).trim()
        SECURITY_TOKEN = sh(script: "grep '^SECURITY_TOKEN=' ${ENV_FILE} | cut -d= -f2", returnStdout: true).trim()
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
                    mkdir -p /tmp/core-dto-build
                    cd /tmp/core-dto-build
                    git clone https://github.com/Consort-Group-Corp/core_api_dto.git
                    cd core_api_dto
                    chmod +x gradlew
                    ./gradlew publishToMavenLocal
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

        stage('Deploy') {
            steps {
                sh '''
                    docker build -t user-service:latest .

                    # Останавливаем старый контейнер
                    docker stop consort-user-service || true
                    docker rm consort-user-service || true

                    # Запускаем новый контейнер
                    docker run -d \\
                        --name consort-user-service \\
                        --network consort-infra_consort-network \\
                        -p 8081:8081 \\
                        -v /app/logs/user:/var/log/user \\
                        -e SPRING_PROFILES_ACTIVE=dev \\
                        -e SPRING_DATASOURCE_URL=jdbc:postgresql://consort-postgres:5432/${POSTGRES_DB} \\
                        -e SPRING_DATASOURCE_USERNAME=${POSTGRES_USER} \\
                        -e SPRING_DATASOURCE_PASSWORD=${POSTGRES_PASSWORD} \\
                        -e SPRING_DATA_REDIS_HOST=consort-redis-user-service \\
                        -e SPRING_DATA_REDIS_PORT=6379 \\
                        -e SPRING_CLOUD_EUREKA_CLIENT_SERVICE_URL_DEFAULTZONE=http://eureka-service:8762/eureka/ \\
                        -e SECURITY_TOKEN=${SECURITY_TOKEN} \\
                        user-service:latest

                    echo "✅ User service deployed successfully!"
                '''
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