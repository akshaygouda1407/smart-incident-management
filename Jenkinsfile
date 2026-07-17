pipeline {
    agent any

    environment {
        BACKEND_IMAGE = "akshaygouda646/smartims-backend"
        FRONTEND_IMAGE = "akshaygouda646/smartims-frontend"
        IMAGE_TAG = "v1"
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Backend Build') {
            steps {
                sh '''
                    chmod +x backend/mvnw
                    backend/mvnw -f backend/pom.xml clean package -DskipTests
                '''
            }
        }

	stage('SonarQube Analysis') {
   		 steps {
       			 dir('backend') {
           			 withSonarQubeEnv('sonarqube') {
               				 sh '''
                   				 chmod +x mvnw

                   				 ./mvnw clean verify sonar:sonar \
                     					 -Dsonar.projectKey=smart-incident-management \
                     					 -Dsonar.projectName="Smart Incident Management Backend"
               				 '''
            			}
       			 }
   		 }
	}

	stage('Quality Gate') {
   		 steps {
       			 timeout(time: 5, unit: 'MINUTES') {
           			 waitForQualityGate abortPipeline: true
       			 }
   		 }
	}

        stage('Frontend Build') {
            steps {
                sh '''
                    cd frontend
                    npm ci
                    npm run build
                '''
            }
        }

        stage('Backend Docker Build') {
            steps {
                sh '''
                    docker build \
                    -t ${BACKEND_IMAGE}:${IMAGE_TAG} \
                    backend
                '''
            }
        }

        stage('Frontend Docker Build') {
            steps {
                sh '''
                    docker build \
                    --build-arg VITE_API_URL=http://localhost:8081 \
                    -t ${FRONTEND_IMAGE}:${IMAGE_TAG} \
                    frontend
                '''
            }
        }

        stage('Docker Push') {
            steps {
                withCredentials([
                    usernamePassword(
                        credentialsId: 'dockerhub-credentials',
                        usernameVariable: 'DOCKER_USERNAME',
                        passwordVariable: 'DOCKER_PASSWORD'
                    )
                ]) {
                    sh '''
                        echo "$DOCKER_PASSWORD" | docker login \
                        -u "$DOCKER_USERNAME" \
                        --password-stdin

                        docker push ${BACKEND_IMAGE}:${IMAGE_TAG}
                        docker push ${FRONTEND_IMAGE}:${IMAGE_TAG}
                    '''
                }
            }
        }

        stage('Deploy') {
            steps {
                sh '''
		    cd /var/lib/jenkins/projects/smart-incident-management-platform
		    docker compose pull
		    docker compose up -d --remove-orphans
		    docker compose ps
                '''
            }
        }
    }

    post {
        always {
            sh 'docker logout || true'
        }

        success {
            echo 'Smart Incident Management deployment completed successfully.'
        }

        failure {
            echo 'Smart Incident Management deployment failed.'
        }
    }
}
