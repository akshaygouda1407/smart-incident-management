pipeline {
    agent any

    options {
        skipDefaultCheckout(true)
        timestamps()
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
                    if errorlevel 1 exit /b 1

                    git --version
                    if errorlevel 1 exit /b 1

                    docker --version
                    if errorlevel 1 exit /b 1

                    docker compose version
                    if errorlevel 1 exit /b 1

                    node --version
                    if errorlevel 1 exit /b 1

                    npm --version
                    if errorlevel 1 exit /b 1
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
                            echo Backend build failed.
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
                            echo npm install failed.
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
                        echo Backend Docker build failed.
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
                        echo Frontend Docker build failed.
                        exit /b 1
                    )
                '''
            }
        }

        stage('Docker Environment') {
            steps {
                powershell '''
                    Write-Host "===== Docker Environment ====="

                    Write-Host "Current Windows account:"
                    whoami

                    Write-Host ""

                    Write-Host "Docker executable:"
                    Get-Command docker

                    Write-Host ""

                    Write-Host "User profile:"
                    Write-Host $env:USERPROFILE

                    Write-Host ""

                    Write-Host "Docker config:"
                    Write-Host $env:DOCKER_CONFIG

                    Write-Host ""

                    docker version

                    if ($LASTEXITCODE -ne 0) {
                        throw "Docker is not available to Jenkins"
                    }
                '''
            }
        }

        stage('Verify Docker Credentials') {
            steps {
                withCredentials([
                    usernamePassword(
                        credentialsId: 'dockerhub-credentials',
                        usernameVariable: 'DOCKER_USERNAME',
                        passwordVariable: 'DOCKER_PASSWORD'
                    )
                ]) {
                    powershell '''
                        $ErrorActionPreference = "Stop"

                        Write-Host "===== Jenkins Docker Credential Check ====="
                        Write-Host "Username: $env:DOCKER_USERNAME"

                        if ([string]::IsNullOrWhiteSpace($env:DOCKER_PASSWORD)) {
                            throw "Docker Hub PAT is empty"
                        }

                        Write-Host "Password Length: $($env:DOCKER_PASSWORD.Length)"
                        Write-Host "Attempting Docker Hub login..."

                        docker logout 2>$null

                        $env:DOCKER_PASSWORD | docker login `
                            --username $env:DOCKER_USERNAME `
                            --password-stdin

                        if ($LASTEXITCODE -ne 0) {
                            throw "Docker Hub login failed"
                        }

                        Write-Host "Docker Hub login successful"
                    '''
                }
            }
        }

        stage('Docker Push') {
            steps {
                bat '''
                    @echo off

                    docker push %BACKEND_IMAGE%:%IMAGE_TAG%

                    if errorlevel 1 (
                        echo Backend image push failed.
                        exit /b 1
                    )

                    docker push %FRONTEND_IMAGE%:%IMAGE_TAG%

                    if errorlevel 1 (
                        echo Frontend image push failed.
                        exit /b 1
                    )

                    echo Both Docker images pushed successfully.
                '''
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

                    if errorlevel 1 (
                        echo Docker Compose status check failed.
                        exit /b 1
                    )
                '''
            }
        }

        stage('Health Check') {
            steps {
                powershell '''
                    Write-Host "Waiting for backend startup..."
                    Start-Sleep -Seconds 20

                    try {
                        $response = Invoke-RestMethod `
                            -Uri "http://localhost:8081/actuator/health" `
                            -Method Get `
                            -TimeoutSec 30

                        Write-Host "Backend health status: $($response.status)"

                        if ($response.status -ne "UP") {
                            throw "Backend health status is not UP"
                        }
                    }
                    catch {
                        Write-Host "Backend health check failed"
                        docker compose ps
                        docker compose logs --tail=100 backend
                        throw
                    }
                '''
            }
        }
    }

    post {
        always {
            powershell '''
                docker logout 2>$null
                exit 0
            '''
        }

        success {
            echo 'Smart Incident Management pipeline completed successfully.'
            echo 'Frontend: http://localhost:5173'
            echo 'Backend: http://localhost:8081'
            echo 'SonarQube: http://localhost:9000'
            echo 'Prometheus: http://localhost:9092'
            echo 'Grafana: http://localhost:3002'
        }

        failure {
            echo 'Smart Incident Management pipeline failed. Check the failed stage in Console Output.'
        }
    }
}