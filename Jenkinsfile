pipeline {
    agent any
    
    environment {
        SONAR_HOST_URL = 'http://sonarqube:9000'
        SONAR_LOGIN = credentials('sonarqube-token')
        JMETER_HOME = '/opt/apache-jmeter-5.5'
    }
    
    triggers {
        // Trigger on SCM push (or configure GitHub webhooks for PR merges)
        pollSCM('H/5 * * * *')
    }
    
    stages {
        stage('Validate Branch') {
            when { branch 'main' }
            steps {
                echo "Building on main branch..."
            }
        }
        
        stage('Checkout') {
            steps {
                checkout scm
            }
        }
        
        stage('Build') {
            steps {
                sh 'mvn clean compile'
            }
        }
        
        stage('Unit Tests') {
            steps {
                sh 'mvn test'
            }
            post {
                always {
                    junit '**/target/surefire-reports/*.xml'
                }
            }
        }
        
        stage('Static Analysis (SonarQube)') {
            steps {
                echo 'Running SonarQube analysis...'
                sh """
                    mvn verify sonar:sonar \
                        -Dsonar.host.url=${SONAR_HOST_URL} \
                        -Dsonar.login=${SONAR_LOGIN}
                """
            }
        }
        
        stage('Security Analysis (OWASP)') {
            steps {
                sh 'mvn org.owasp:dependency-check-maven:check'
            }
            post {
                always {
                    dependencyCheckPublisher pattern: '**/dependency-check-report.xml'
                }
            }
        }
        
        stage('Generate JavaDocs') {
            steps {
                sh 'mvn javadoc:javadoc'
            }
        }
        
        stage('Performance Testing') {
            steps {
                echo 'Running JMeter performance tests...'
                sh '''
                    ${JMETER_HOME}/bin/jmeter -n -t ./performance_tests/test_plan.jmx \
                    -l ./target/jmeter/results.jtl -e -o ./target/jmeter/html
                '''
            }
        }
    }
    
    post {
        always {
            archiveArtifacts artifacts: 'target/*.jar', fingerprint: true
            archiveArtifacts artifacts: 'target/site/apidocs/**', fingerprint: true
        }
    }
}