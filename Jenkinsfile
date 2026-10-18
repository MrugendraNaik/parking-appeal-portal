// =====================================================================
//  Parking Violation Appeal Portal - CI/CD pipeline (Pipeline as Code)
//  Experiments 8, 10 and 12:
//    Checkout -> Build & Unit Test -> Package -> Selenium Tests
//      -> Deploy to Tomcat (WAR)   and/or
//      -> Docker Build -> Push Image -> Deploy Container
//  A failing unit/Selenium test fails the build, so NO deploy stage runs.
// =====================================================================

pipeline {
    agent any

    options {
        timestamps()
        buildDiscarder(logRotator(numToKeepStr: '15'))
        disableConcurrentBuilds()
    }

    // Experiment 7/8: build automatically on new commits (poll GitHub every ~2 min).
    // If you configure a GitHub webhook instead, you can delete this block.
    triggers {
        pollSCM('H/2 * * * *')
    }

    parameters {
        choice(name: 'DEPLOY_ENV', choices: ['staging', 'production', 'dev'],
               description: 'Environment setting passed to the app (shown in the page footer)')
        choice(name: 'DEPLOY_TARGET', choices: ['docker', 'tomcat', 'both', 'none'],
               description: 'Where to deploy after tests pass')
        booleanParam(name: 'RUN_SELENIUM', defaultValue: true,
                     description: 'Run the Selenium UI suite (failed tests stop deployment)')
        string(name: 'TOMCAT_HOME', defaultValue: '/opt/tomcat', description: 'Tomcat 10.1 install folder')
        string(name: 'TOMCAT_URL', defaultValue: 'http://localhost:8081', description: 'Tomcat base URL')
        string(name: 'IMAGE_REPO', defaultValue: 'localhost:5000/parking-appeal',
               description: 'Image name: localhost:5000/parking-appeal (local registry) or <dockerhub-user>/parking-appeal')
        booleanParam(name: 'PUSH_IMAGE', defaultValue: true, description: 'Push the image to the registry')
        string(name: 'APP_PORT', defaultValue: '8090', description: 'Host port for the deployed container')
    }

    environment {
        APP_NAME     = 'parking-appeal'
        // For a real setup store this as a Jenkins "Secret text" credential and use:
        // REVIEWER_KEY = credentials('reviewer-key')
        REVIEWER_KEY = 'reviewer123'
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
                script {
                    env.GIT_SHORT = sh(script: 'git rev-parse --short HEAD', returnStdout: true).trim()
                    env.IMAGE_TAG = "${env.BUILD_NUMBER}-${env.GIT_SHORT}"
                }
                sh 'git log --oneline -5'
                echo "Version tag for this build: ${env.IMAGE_TAG}"
            }
        }

        stage('Build & Unit Test') {
            steps {
                sh 'mvn -B clean test'
            }
            post {
                always {
                    junit allowEmptyResults: true, testResults: 'target/surefire-reports/*.xml'
                }
            }
        }

        stage('Package') {
            steps {
                sh 'mvn -B package -DskipTests'
                archiveArtifacts artifacts: 'target/parking-appeal.war', fingerprint: true
            }
        }

        stage('Selenium Tests') {
            when { expression { params.RUN_SELENIUM } }
            steps {
                // Starts the app on port 8095, runs the Selenium journeys, stops the app.
                sh 'mvn -B verify -Pselenium -DskipUnitTests=true'
            }
            post {
                always {
                    junit allowEmptyResults: true, testResults: 'target/failsafe-reports/*.xml'
                    archiveArtifacts allowEmptyArchive: true, artifacts: 'target/screenshots/**'
                }
            }
        }

        stage('Deploy to Tomcat') {
            when { expression { ['tomcat', 'both'].contains(params.DEPLOY_TARGET) } }
            steps {
                sh '''
                    set -e
                    DATA_DIR="${TOMCAT_HOME}/appdata/${DEPLOY_ENV}"
                    mkdir -p "$DATA_DIR" "${TOMCAT_HOME}/conf/Catalina/localhost"
                    chmod 2775 "$DATA_DIR"   # Tomcat (group tomcat) must be able to write the H2 DB here
                    # Parameterised environment settings -> Tomcat context parameters read by Spring Boot
                    cat > "${TOMCAT_HOME}/conf/Catalina/localhost/${APP_NAME}.xml" <<EOF2
<Context>
    <Parameter name="app.env" value="${DEPLOY_ENV}" override="false"/>
    <Parameter name="app.data-dir" value="${DATA_DIR}" override="false"/>
    <Parameter name="app.build" value="${IMAGE_TAG}" override="false"/>
    <Parameter name="app.reviewer-key" value="${REVIEWER_KEY}" override="false"/>
</Context>
EOF2
                    cp target/${APP_NAME}.war "${TOMCAT_HOME}/webapps/${APP_NAME}.war"
                    echo "Deployed WAR to ${TOMCAT_HOME}/webapps (env=${DEPLOY_ENV})"
                '''
                waitForHealth("${params.TOMCAT_URL}/${env.APP_NAME}/actuator/health")
                echo "Tomcat URL: ${params.TOMCAT_URL}/${env.APP_NAME}/"
            }
        }

        stage('Docker Build') {
            when { expression { ['docker', 'both'].contains(params.DEPLOY_TARGET) } }
            steps {
                sh '''
                    docker build --build-arg APP_VERSION=${IMAGE_TAG} \
                        -t ${IMAGE_REPO}:${IMAGE_TAG} -t ${IMAGE_REPO}:latest .
                    docker image ls ${IMAGE_REPO}
                '''
            }
        }

        stage('Push Image') {
            when {
                allOf {
                    expression { ['docker', 'both'].contains(params.DEPLOY_TARGET) }
                    expression { params.PUSH_IMAGE }
                }
            }
            steps {
                script {
                    if (params.IMAGE_REPO.startsWith('localhost:')) {
                        sh 'docker push ${IMAGE_REPO}:${IMAGE_TAG} && docker push ${IMAGE_REPO}:latest'
                    } else {
                        withCredentials([usernamePassword(credentialsId: 'dockerhub-creds',
                                usernameVariable: 'DH_USER', passwordVariable: 'DH_PASS')]) {
                            sh '''
                                echo "$DH_PASS" | docker login -u "$DH_USER" --password-stdin
                                docker push ${IMAGE_REPO}:${IMAGE_TAG}
                                docker push ${IMAGE_REPO}:latest
                                docker logout
                            '''
                        }
                    }
                }
            }
        }

        stage('Deploy Container') {
            when { expression { ['docker', 'both'].contains(params.DEPLOY_TARGET) } }
            steps {
                sh '''
                    CONTAINER=${APP_NAME}-${DEPLOY_ENV}
                    echo "Replacing container $CONTAINER with ${IMAGE_REPO}:${IMAGE_TAG}"
                    docker rm -f $CONTAINER 2>/dev/null || true
                    docker run -d --name $CONTAINER --restart unless-stopped \
                        -p ${APP_PORT}:8080 \
                        -e APP_ENV=${DEPLOY_ENV} \
                        -e REVIEWER_KEY=${REVIEWER_KEY} \
                        -v ${APP_NAME}-${DEPLOY_ENV}-data:/app/data \
                        ${IMAGE_REPO}:${IMAGE_TAG}
                    docker ps --filter name=$CONTAINER
                '''
                waitForHealth("http://localhost:${params.APP_PORT}/actuator/health")
                echo "Container URL: http://localhost:${params.APP_PORT}/"
            }
        }
    }

    post {
        success {
            echo "SUCCESS: build ${env.IMAGE_TAG} tested and deployed (target=${params.DEPLOY_TARGET}, env=${params.DEPLOY_ENV})"
        }
        failure {
            echo 'FAILED: a stage failed - nothing after it was deployed. Check the test report and target/screenshots.'
        }
    }
}

/** Polls a health URL until it returns HTTP 200 (max 3 minutes). */
def waitForHealth(String url) {
    timeout(time: 3, unit: 'MINUTES') {
        waitUntil(initialRecurrencePeriod: 5000) {
            def code = sh(script: "curl -s -o /dev/null -w '%{http_code}' ${url} || true", returnStdout: true).trim()
            echo "Health check ${url} -> HTTP ${code}"
            return code == '200'
        }
    }
}
