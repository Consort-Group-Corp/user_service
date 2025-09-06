pipeline {
  agent any
  options { timestamps() }

  tools {
    jdk 'jdk-21'
    gradle 'gradle-8'
  }

  triggers {
    githubPush()
  }

  environment {
    SERVICE_NAME   = 'user-service'
    CONTAINER_NAME = 'consort-user-service'
    DOCKER_NETWORK = 'consort-infra_consort-network'
    LOGS_DIR       = '/app/logs/user'
    ENV_FILE       = '/var/jenkins_home/.env'
  }

  stages {
    stage('Checkout') {
      steps {
        checkout scm
        script {
          env.GIT_COMMIT_SHORT = sh(script: 'git rev-parse --short HEAD', returnStdout: true).trim()
          env.IMAGE_TAG    = "${env.SERVICE_NAME}:${env.GIT_COMMIT_SHORT}"
          env.IMAGE_LATEST = "${env.SERVICE_NAME}:latest"
        }
      }
    }

    stage('Load .env') {
      steps {
        script {
          def rd = { k -> sh(script: "grep -E '^${k}=' ${env.ENV_FILE} | head -n1 | cut -d= -f2- || true", returnStdout: true).trim() }
          env.POSTGRES_DB       = rd('POSTGRES_DB')       ?: 'consort_group'
          env.POSTGRES_USER     = rd('POSTGRES_USER')     ?: 'consort'
          env.POSTGRES_PASSWORD = rd('POSTGRES_PASSWORD') ?: ''
          env.DB_USERNAME       = rd('DB_USERNAME')       ?: env.POSTGRES_USER
          env.DB_PASSWORD       = rd('DB_PASSWORD')       ?: env.POSTGRES_PASSWORD
          env.SECURITY_TOKEN    = rd('SECURITY_TOKEN')    ?: ''
        }
      }
    }

    stage('Build core_api_dto to local maven') {
      steps {
        dir("${env.WORKSPACE}@core-dto") {
          sh '''
            set -e
            find . -mindepth 1 -maxdepth 1 -exec rm -rf {} + || true
            git clone https://github.com/Consort-Group-Corp/core_api_dto.git .
            chmod +x gradlew
            ./gradlew --no-daemon publishToMavenLocal
          '''
        }
      }
    }

    stage('Gradle build') {
      steps {
        sh '''
          set -e
          chmod +x gradlew
          ./gradlew --no-daemon clean build -x test
        '''
      }
    }

    stage('Archive JAR') {
      steps {
        archiveArtifacts artifacts: 'build/libs/*.jar', fingerprint: true, allowEmptyArchive: true
      }
    }

    stage('Docker build') {
      steps {
        sh '''
          set -e
          docker build -t ${IMAGE_TAG} -t ${IMAGE_LATEST} .
        '''
      }
    }

    stage('Deploy') {
      steps {
        sh '''
          set -e
          mkdir -p ${LOGS_DIR} || true

          docker stop ${CONTAINER_NAME} || true
          docker rm ${CONTAINER_NAME} || true

          docker run -d \
            --name ${CONTAINER_NAME} \
            --network ${DOCKER_NETWORK} \
            --restart unless-stopped \
            -p 8081:8081 \
            -v ${LOGS_DIR}:/var/log/user \
            --env-file ${ENV_FILE} \
            -e TZ=Asia/Tashkent \
            -e SPRING_PROFILES_ACTIVE=dev \
            -e SPRING_DATASOURCE_URL=jdbc:postgresql://consort-postgres:5432/${POSTGRES_DB} \
            -e SPRING_DATASOURCE_USERNAME=${POSTGRES_USER} \
            -e SPRING_DATASOURCE_PASSWORD=${POSTGRES_PASSWORD} \
            -e SPRING_DATA_REDIS_HOST=consort-redis-user-service \
            -e SPRING_DATA_REDIS_PORT=6379 \
            -e SPRING_KAFKA_BOOTSTRAP_SERVERS=consort-kafka:9092 \
            -e EUREKA_CLIENT_SERVICEURL_DEFAULTZONE=http://consort-eureka-service:8762/eureka/ \
            -e JAVA_TOOL_OPTIONS="-Deureka.client.serviceUrl.defaultZone=http://consort-eureka-service:8762/eureka/ -Dspring.kafka.bootstrap-servers=consort-kafka:9092" \
            -e SPRING_APPLICATION_JSON='{"eureka":{"client":{"serviceUrl":{"defaultZone":"http://consort-eureka-service:8762/eureka/"}}},"spring":{"kafka":{"bootstrap-servers":"consort-kafka:9092"}}}' \
            -e SECURITY_TOKEN=${SECURITY_TOKEN} \
            ${IMAGE_TAG}

          # короткий smoke-check (опционально)
          sleep 5
          curl -sf http://localhost:8081/actuator/health >/dev/null || (docker logs --tail=200 ${CONTAINER_NAME}; exit 1)

          echo "✅ Deployed ${CONTAINER_NAME} with image ${IMAGE_TAG}"
        '''
      }
    }
  }

  post {
    success {
      echo "✅ Build & deploy success: ${env.IMAGE_TAG}"
    }
    failure {
      echo '❌ Pipeline failed — cleaning up...'
      sh '''
        docker logs --tail=200 ${CONTAINER_NAME} || true
        docker stop ${CONTAINER_NAME} || true
        docker rm ${CONTAINER_NAME} || true
        docker rmi ${IMAGE_TAG} || true
      '''
    }
    cleanup {
      cleanWs(deleteDirs: true, disableDeferredWipeout: true, notFailBuild: true)
    }
  }
}
