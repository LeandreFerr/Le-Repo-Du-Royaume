pipeline {
    agent any

    stages {
        stage('Git checkout') {
            steps {
                script {
                    git url: 'https://github.com/LeandreFerr/Le-Repo-Du-Royaume.git',
                        branch: 'ci-cd-docker'
                }
            }
        }

        stage('Maven Verify & SonarQube') {
            agent {
                docker {
                    image 'maven:3.10.0-eclipse-temurin-21'
                    args '''
                        -v $HOME/.m2:/root/.m2:z
                        -u root
                    '''
                    reuseNode true
                }
            }

            steps {
                dir('royaume_back') {
                    withSonarQubeEnv('sonar') {
                        sh 'mvn clean verify org.sonarsource.scanner.maven:sonar-maven-plugin:sonar -Dsonar.projectKey=royaume-back -Dsonar.projectName="Royaume Back (G2)" -Dsonar.token=$SONAR_AUTH_TOKEN'
                    }
                }
            }
        }

        stage('Angular Build') {
            agent {
                docker {
                    image 'node:lts'
                    args '''
                        -v $HOME/.npm:/root/.npm:z
                        -u root
                    '''
                    reuseNode true
                }
            }

            steps {
                dir('royaume_front') {
                    sh 'npm i'
                    sh './node_modules/.bin/ng build'
                }
            }
        }

        stage('Docker Compose') {
            steps {
                sh 'docker compose -f docker-compose.yml up -d --build'
                sh 'docker compose -f docker-compose.demo.yml up -d --build'
            }
        }
    }
}
