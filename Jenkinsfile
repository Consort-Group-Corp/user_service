pipeline {
  agent any
  options { timestamps(); skipDefaultCheckout() }

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
    ENV_FILE       = '/var/jenkins_home/.env'   // сейчас ты его уже скопировал сюда
  }

  stages {
    stage('Checkout') {
      steps {
        checkout scm
        script {
          env.GIT_COMMIT_SHORT = sh(script: 'git rev-parse --short HEAD', returnStdout: true).trim()
          env.IMAGE_TAG        = "${env.SERVICE_NAME}:${env.GIT_COMMIT_SHORT}"
          env.IMAGE_LATEST     = "${env.SERVICE_NAME}:latest"
        }
      }
    }

    stage('Load .env') {
      steps {
        script {
          def p = readProperties file: env.ENV_FILE
          env.POSTGRES_DB       = p.POSTGRES_DB ?: 'consort_group'
          env.POSTGRES_USER     = p.POSTGRES_USER ?: 'consort'
          env.POSTGRES_PASSWORD = p.POSTGRES_PASSWORD ?: ''
          env.DB_USERNAME       = p.DB_USERNAME ?: env.POSTGRES_USER
          env.DB_PASSWORD       = p.DB_PASSWORD ?: env.POSTGRES_PASSWORD
          env.KAFKA_CLUSTER_ID  = p.KAFKA_CLUSTER_ID ?: 'consort-cluster-id'
          env.SECURITY_TOKEN    = p.SECURITY_TOKEN ?: ''
        }
      }
    }

    stage('Build core_api_dto to local maven') {
      steps {
        dir("${env.WORKSPACE}@core-dto") {
          sh '''
            rm -rf .
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
          chmod +x gradlew
          ./gradlew --no-daemon clean build -x test
        '''
      }
    }

    stage('Docker build') {
      steps {
        sh "docker build -t ${IMAGE_TAG} -t ${IMAGE_LATEST} ."
      }
    }

    stage('Deploy') {
      steps {
        sh """
          mkdir -p ${LOGS_DIR} || true

          docker stop ${CONTAINER_NAME} || true
          docker rm   ${CONTAINER_NAME} || true

          docker run -d \
            --name ${CONTAINER_NAME} \
            --network ${DOCKER_NETWORK} \
            -p 8081:8081 \
            -v ${LOGS_DIR}:/var/log/user \
            -e SPRING_PROFILES_ACTIVE=dev \
            -e SPRING_DATASOURCE_URL=jdbc:postgresql://consort-postgres:5432/${POSTGRES_DB} \
            -e SPRING_DATASOURCE_USERNAME=${POSTGRES_USER} \
            -e SPRING_DATASOURCE_PASSWORD=${POSTGRES_PASSWORD} \
            -e SPRING_DATA_REDIS_HOST=consort-redis-user-service \
            -e SPRING_DATA_REDIS_PORT=6379 \
            -e SPRING_CLOUD_EUREKA_CLIENT_SERVICE_URL_DEFAULTZONE=http://eureka-service:8762/eureka/ \
            -e SECURITY_TOKEN=${SECURITY_TOKEN} \
            ${IMAGE_TAG}

          echo "✅ Deployed ${CONTAINER_NAME} with image ${IMAGE_TAG}"
        """
      }
    }
  }

  post {
    success {
      echo "✅ Build & deploy success: ${env.IMAGE_TAG}"
      archiveArtifacts artifacts: 'build/libs/*.jar', fingerprint: true
    }
    failure {
      echo '❌ Pipeline failed — cleaning up...'
      sh """
        docker logs --tail=200 ${CONTAINER_NAME} || true
        docker stop ${CONTAINER_NAME} || true
        docker rm   ${CONTAINER_NAME} || true
        docker rmi  ${IMAGE_TAG} || true
      """
    }
    always {
      cleanWs(deleteDirs: true, disableDeferredWipeout: true, notFailBuild: true)
    }
  }
}
