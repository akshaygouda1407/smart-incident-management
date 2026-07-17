pipeline {
    agent any

    options {
        skipDefaultCheckout(true)
    }

    environment {
        JAVA_HOME = 'C:\\Program Files\\Java\\jdk-21.0.11'
        PATH = "${JAVA_HOME}\\bin;${env.PATH}"

        BACKEND_IMAGE = 'akshaygouda646/smartims-backend'
        FRONTEND_IMAGE = 'akshaygouda646/smartims-frontend'
        IMAGE_TAG = 'v1'
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Environment Check') {
            steps {
                bat '''
                    @echo off
                    java -version
                    git --version
                    docker --version
                    docker compose version
                    node --version
                    npm --version
                '''
            }
        }

        stage('Backend Build') {
            steps {
                dir('backend') {
                    bat '''
                        @echo off
                        call mvnw.cmd clean package -DskipTests

                        if errorlevel 1 (
                            echo Backend Maven build failed.
                            exit /b 1
                        )
                    '''
                }
            }
        }

        stage('SonarQube Analysis') {
            steps {
                dir('backend') {
                    withSonarQubeEnv('Local SonarQube') {
                        bat '''
                            @echo off
                            call mvnw.cmd sonar:sonar ^
                              -Dsonar.projectKey=smart-incident-management ^
                              "-Dsonar.projectName=Smart Incident Management Backend"

                            if errorlevel 1 (
                                echo SonarQube analysis failed.
                                exit /b 1
                            )
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
                dir('frontend') {
                    bat '''
                        @echo off
                        call npm ci

                        if errorlevel 1 (
                            echo Frontend dependency installation failed.
                            exit /b 1
                        )

                        call npm run build

                        if errorlevel 1 (
                            echo Frontend build failed.
                            exit /b 1
                        )
                    '''
                }
            }
        }

        stage('Backend Docker Build') {
            steps {
                bat '''
                    @echo off
                    docker build ^
                      -t %BACKEND_IMAGE%:%IMAGE_TAG% ^
                      backend

                    if errorlevel 1 (
                        echo Backend Docker image build failed.
                        exit /b 1
                    )
                '''
            }
        }

        stage('Frontend Docker Build') {
            steps {
                bat '''
                    @echo off
                    docker build ^
                      --build-arg VITE_API_URL=http://localhost:8081 ^
                      -t %FRONTEND_IMAGE%:%IMAGE_TAG% ^
                      frontend

                    if errorlevel 1 (
                        echo Frontend Docker image build failed.
                        exit /b 1
                    )
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
                    bat '''
                        @echo off

                        docker logout >nul 2>&1

                        echo %DOCKER_PASSWORD% | docker login ^
                          --username %DOCKER_USERNAME% ^
                          --password-stdin

                        if errorlevel 1 (
                            echo Docker Hub login failed.
                            exit /b 1
                        )

                        echo Docker Hub login succeeded.

                        docker push %BACKEND_IMAGE%:%IMAGE_TAG%

                        if errorlevel 1 (
                            echo Backend Docker image push failed.
                            exit /b 1
                        )

                        docker push %FRONTEND_IMAGE%:%IMAGE_TAG%

                        if errorlevel 1 (
                            echo Frontend Docker image push failed.
                            exit /b 1
                        )

                        echo Both Docker images pushed successfully.
                    '''
                }
            }
        }

        stage('Deploy') {
            steps {
                bat '''
                    @echo off

                    docker compose pull

                    if errorlevel 1 (
                        echo Docker Compose pull failed.
                        exit /b 1
                    )

                    docker compose up -d --remove-orphans

                    if errorlevel 1 (
                        echo Docker Compose deployment failed.
                        exit /b 1
                    )

                    docker compose ps
                '''
            }
        }
    }

    post {
        always {
            bat '''
                @echo off
                docker logout >nul 2>&1
                exit /b 0
            '''
        }

        success {
            echo 'Smart Incident Management deployment completed successfully.'
        }

        failure {
            echo 'Smart Incident Management deployment failed. Check Console Output.'
        }
    }
}