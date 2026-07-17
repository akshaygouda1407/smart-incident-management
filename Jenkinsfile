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
                    call npm run build
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
                        Write-Host "===== Jenkins Docker Credential Check ====="

                        Write-Host "Username: $env:DOCKER_USERNAME"

                        if ([string]::IsNullOrWhiteSpace($env:DOCKER_PASSWORD)) {
                            throw "Password is EMPTY!"
                        }

                        Write-Host "Password Length: $($env:DOCKER_PASSWORD.Length)"

                        Write-Host "Attempting Docker Login..."

                        docker logout 2>$null

                        $env:DOCKER_PASSWORD | docker login `
                            --username $env:DOCKER_USERNAME `
                            --password-stdin

                        if ($LASTEXITCODE -ne 0) {
                            throw "Docker Login Failed"
                        }

                        Write-Host "Docker Login Successful"
                    '''
                }
            }
        }

        stage('Docker Push') {
            steps {
                bat '''
                @echo off

                docker push %BACKEND_IMAGE%:%IMAGE_TAG%
                if errorlevel 1 exit /b 1

                docker push %FRONTEND_IMAGE%:%IMAGE_TAG%
                if errorlevel 1 exit /b 1
                '''
            }
        }

        stage('Deploy') {
            steps {
                bat '''
                @echo off

                docker compose pull
                docker compose up -d --remove-orphans
                docker compose ps
                '''
            }
        }
    }

    post {
        always {
            powershell '''
                docker logout 2>$null
            '''
        }

        success {
            echo 'Pipeline completed successfully.'
        }

        failure {
            echo 'Pipeline failed.'
        }
    }
}