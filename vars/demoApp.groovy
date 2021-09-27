def call(build_parameters) {

    def application = build_parameters['application']
    def test_service = build_parameters['test_service']

    pipeline {

        agent any

        environment {
            APPLICATION = "$application"
            TEST_SERVICE = "$test_service"
        }

        stages {
            stage('Tests') {
                steps {
                    sh '''#!/bin/bash -l
                        if test -f "execTests.sh"; then
                            ./execTests.sh
                        fi
                        '''
                    
                    sh '''#!/bin/bash -l
                        if test -f ".docker/test.docker-compose.yaml"; then
                           /var/jenkins_home/docker-compose -f .docker/test.docker-compose.yaml up --abort-on-container-exit --exit-code-from ${TEST_SERVICE}
                        fi
                        '''
                }
            }
            stage('Build') {
                steps {
                    echo "Building image..."
                }
            }
        }
        post {
            always {
                script {
                    if (fileExists('test/junit.xml')) {
                        junit 'test/junit.xml'
                    }
                }
                sh '''#!/bin/bash -l
                    if test -f ".docker/test.docker-compose.yaml"; then
                        /var/jenkins_home/docker-compose -f .docker/test.docker-compose.yaml down
                    fi
                    '''
            }
        }
    }

}
