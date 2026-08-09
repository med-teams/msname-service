// Jenkinsfile - msname-service
// Un seul Jenkinsfile, comportement different selon le contexte :
//  - PR vers release/*      -> build + sonar + quality gate, AUCUN jar publie
//  - branche release/* elle-meme (apres merge) -> build jar + deploiement portal

pipeline {
    agent { label 'build-agent' }   // meme agent que commons-api pour lire son .m2 local

    parameters {
        string(name: 'MODULE_PATH', defaultValue: '.', description: 'Chemin du module a builder')
        string(name: 'SERVICE_NAME', defaultValue: 'msname-service', description: 'Nom du microservice')
        string(name: 'DEPLOY_ROOT', defaultValue: '/opt/portal/apps', description: 'Racine de deploiement sur le portal')
        booleanParam(name: 'REQUIRE_APPROVAL', defaultValue: true, description: 'Validation manuelle avant deploiement')
    }

    tools {
        maven 'Maven-3.9'
        jdk 'JDK-17'
    }

    options {
        timestamps()
        disableConcurrentBuilds()
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build & Test') {
            steps {
                dir("${params.MODULE_PATH}") {
                    sh 'mvn -B clean verify'
                }
            }
        }

        stage('Sonar (PR uniquement)') {
            when { changeRequest() }
            steps {
                dir("${params.MODULE_PATH}") {
                    withSonarQubeEnv('SonarQube-Server') {
                        sh "mvn -B sonar:sonar -Dsonar.projectKey=${params.SERVICE_NAME}"
                    }
                }
            }
        }

        stage('Quality Gate (PR uniquement)') {
            when { changeRequest() }
            steps {
                timeout(time: 10, unit: 'MINUTES') {
                    waitForQualityGate abortPipeline: true
                }
            }
        }

        stage('Build Jar (branche release/* uniquement)') {
            when { branch pattern: 'release/.*', comparator: 'REGEXP' }
            steps {
                dir("${params.MODULE_PATH}") {
                    sh 'mvn -B clean package -DskipTests'
                    script {
                        env.APP_VERSION = sh(
                            script: "mvn -q -Dexec.executable=echo -Dexec.args='\${project.version}' --non-recursive exec:exec",
                            returnStdout: true
                        ).trim()
                        env.JAR_PATH = sh(script: 'ls target/*.jar | head -n1', returnStdout: true).trim()
                    }
                }
                echo "Jar genere: ${env.JAR_PATH} (version ${env.APP_VERSION})"
            }
        }

        stage('Validation avant deploiement') {
            when {
                allOf {
                    branch pattern: 'release/.*', comparator: 'REGEXP'
                    expression { return params.REQUIRE_APPROVAL }
                }
            }
            steps {
                input message: "Deployer ${params.SERVICE_NAME} v${env.APP_VERSION} vers le portal ?"
            }
        }

        stage('Deploiement vers le portal (branche release/* uniquement)') {
            when { branch pattern: 'release/.*', comparator: 'REGEXP' }
            steps {
                script {
                    def targetDir = "${params.DEPLOY_ROOT}/${params.SERVICE_NAME}/${env.APP_VERSION}"
                    sh """
                        mkdir -p ${targetDir}
                        cp ${env.JAR_PATH} ${targetDir}/
                    """
                }
            }
        }
    }

    post {
        always {
            junit testResults: "${params.MODULE_PATH}/target/surefire-reports/*.xml", allowEmptyResults: true
        }
        success {
            echo 'Pipeline termine avec succes.'
        }
        failure {
            echo 'Pipeline en echec.'
        }
    }
}
