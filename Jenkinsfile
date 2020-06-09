#!/usr/bin/env groovy

// This Jenkinsfile provides examples of:
// - modular stages
// - continuous deployment
// - stage-level containers
// - running Sonar scanner using command-line flags
// - avoiding the use of most Jenkins plugins
// - building and tagging docker containers
// - formatting of Jenkinsfiles for legibility
// - using jfrog cli

pipeline {

  agent {
    label 'docker'
  }

  options {
    // Option to add timestamps to Jenkins build log.
    timestamps()

    // Option to disable concurrent builds because Sonar cannot perform concurrent scans for the same project key.
    disableConcurrentBuilds()
  }

  environment {
    // Environment vars for application.
    APPLICATION_NAME = "wopr"
    APPLICATION_VERSION_STRING = "0.1.0"
    UNIQUE_BUILD_ID = "${sh(returnStdout: true, script: 'git log -1 --pretty=%h').trim()}"

    // Environment vars for SonarQube.
    SONARQUBE_CREDENTIALS = credentials('SONARQUBE_AZURE')
    SONARQUBE_HOST_URL = "http://10.240.1.20"
    SONARQUBE_PROJECT_KEY = "wopr"

    // Environment vars for Artifactory.
    ARTIFACTORY_CREDENTIALS = credentials('ARTIFACTORY_SECURE24')
    ARTIFACTORY_URL = "http://usbalp-artfct01.cts.cubic.cub:8085/artifactory"
    ARTIFACTORY_PATH = "devops/wopr/${UNIQUE_BUILD_ID}/"

    // Environment vars for the container registry.
    CONTAINER_REGISTRY_CREDENTIALS = credentials('CUBICNEXTCLOUDREGISTRY')
    CONTAINER_REGISTRY_FQDN = "cubicnextcloudregistry.azurecr.io"
    CONTAINER_REPOSITORY = "${CONTAINER_REGISTRY_FQDN}/cts/devops/wopr"
    CONTAINER_TAG = "${UNIQUE_BUILD_ID}"

    // Environment vars for the Azure subscription used for deployment.
    AZURE_CREDENTIALS = credentials('CUBICNEXTCLOUDREGISTRY')
    AZURE_TENANT_NAME = "ctsopsitservicescubic.onmicrosoft.com"
    AZURE_SUBSCRIPTION_NAME = "CTS DevOpsTransformation"
    AZURE_RESOURCE_GROUP_LOCATION = "westus"
    AZURE_RESOURCE_GROUP_NAME = "wopr-rg"
    AZURE_CONTAINER_GROUP_NAME = "wopr"
    AZURE_DNS_NAME_LABEL = "${AZURE_CONTAINER_GROUP_NAME}-${UNIQUE_BUILD_ID}"
    AZURE_CONFIG_DIR = "./.azure"
  }
 
  stages {  
    stage('Maven test') {
      agent {
        docker {
          reuseNode true
          image 'maven'
        }
      } 
      steps {
        // Test the source code.
        sh 'mvn clean test'
      }
    }

    stage('Maven package') {
      agent {
        docker {
          reuseNode true
          image 'maven'
        }
      } 
      steps {
        // Package the source code.
        sh 'mvn package ' +
        '-DskipTests ' +
        '-Dmaven.test.skip=true'
      }
    }

    stage('OWASP Dependency Check') {
      agent {
        docker {
          reuseNode true
          image 'maven'
        }
      } 
      steps {
        // Run OWASP dependency-check.
        sh 'mvn org.owasp:dependency-check-maven:check'
      }
    }

    stage('Archive artifacts') {
      agent {
        docker {
          reuseNode true
          image 'node'
        }
      } 
      steps {
        // Install the jfrog CLI tool using npm.
        sh 'npm install jfrog-cli-go'

        // This command is used to configure JFrog CLI with Artifactory servers, which can later be used in the other commands.
        sh './node_modules/jfrog-cli-go/bin/jfrog rt config ' +
        '--url ${ARTIFACTORY_URL} ' +
        '--user ${ARTIFACTORY_CREDENTIALS_USR} ' +
        '--password ${ARTIFACTORY_CREDENTIALS_PSW} ' +
        '--interactive=false'

        // This command is used to upload files to Artifactory.
        sh './node_modules/jfrog-cli-go/bin/jfrog rt upload "./target/${APPLICATION_NAME}-${APPLICATION_VERSION_STRING}.jar" "${ARTIFACTORY_PATH}"'

        // This command is used to upload files to Artifactory.
        sh './node_modules/jfrog-cli-go/bin/jfrog rt upload "./target/dependency-check-report.html" "${ARTIFACTORY_PATH}"'
      }
    }

    stage('Docker build') {
      steps {
        // Run docker build and create the primary tag.
        sh 'docker build . ' +
        '--file Dockerfile ' +
        '--tag ${CONTAINER_REPOSITORY}:${CONTAINER_TAG}'

        // Create a latest tag using the primary tag.
        sh 'docker tag ${CONTAINER_REPOSITORY}:${CONTAINER_TAG} ${CONTAINER_REPOSITORY}:latest'
      }
    }

    stage('Docker push') {
      steps {
        // Docker login.
        sh 'docker login ${CONTAINER_REGISTRY_FQDN} ' +
        '--username ${CONTAINER_REGISTRY_CREDENTIALS_USR} ' +
        '--password ${CONTAINER_REGISTRY_CREDENTIALS_PSW}'

        // Docker push the primary tag.
        sh 'docker push ${CONTAINER_REPOSITORY}:${CONTAINER_TAG}'

        // Docker push the latest tag.
        sh 'docker push ${CONTAINER_REPOSITORY}:latest'
      }
      post {
        always {
          // Remove all the images used in the build.
          sh 'docker rmi $(docker images -q ${CONTAINER_REPOSITORY}) ' +
          '--force'

          // Docker logout.
          sh 'docker logout ${CONTAINER_REGISTRY_FQDN}'
        }
      }
    }

    stage('SonarQube analysis') {
      agent {
        docker {
          reuseNode true
          image 'maven'
        }
      } 
      steps {
        // Run Sonar Scanner.
        sh 'mvn sonar:sonar ' +
        '-Dsonar.projectKey=${SONARQUBE_PROJECT_KEY} ' +
        '-Dsonar.host.url=${SONARQUBE_HOST_URL} ' +
        '-Dsonar.login=${SONARQUBE_CREDENTIALS_PSW} ' +
        '-Dsonar.language=${SONARQUBE_LANGUAGE} ' +
        '-Dsonar.sources=${SONARQUBE_SOURCES} ' +
        '-Dsonar.tests=${SONARQUBE_TESTS}'
      }
    }

    stage('SonarQube quality gate') {
      agent {
        docker {
          reuseNode true
          image 'maven'
        }
      } 
      steps {
        // Fail the build if the Sonarqube server quality gate is failed.
        sh 'if curl --silent "${SONARQUBE_HOST_URL}/api/qualitygates/project_status?projectKey=${SONARQUBE_PROJECT_KEY}" | grep -q "ERROR"; then exit 1;  fi'
      }
    }

    stage('Deploy container') {
      agent {
        docker {
          reuseNode true
          image 'microsoft/azure-cli'
        }
      } 
      steps {
        // Sign in with Azure CLI
        sh 'az login ' +
        '--service-principal ' +
        '--tenant ${AZURE_TENANT_NAME} ' +
        '--username ${AZURE_CREDENTIALS_USR} ' +
        '--password ${AZURE_CREDENTIALS_PSW}'

        // Set a subscription to be the current active subscription.
        sh 'az account set ' +
        '--subscription "${AZURE_SUBSCRIPTION_NAME}"'

        // Create a new resource group.
        sh 'az group create ' +
        '--location ${AZURE_RESOURCE_GROUP_LOCATION} ' +
        '--name ${AZURE_RESOURCE_GROUP_NAME}'

        // Deploy container with Azure CLI.
        sh 'az container create ' +
        '--name ${AZURE_CONTAINER_GROUP_NAME} ' +
        '--resource-group ${AZURE_RESOURCE_GROUP_NAME} ' +
        '--image ${CONTAINER_REPOSITORY}:latest ' +
        '--registry-login-server ${CONTAINER_REGISTRY_FQDN} ' +
        '--registry-username ${CONTAINER_REGISTRY_CREDENTIALS_USR} ' +
        '--registry-password ${CONTAINER_REGISTRY_CREDENTIALS_PSW} ' +
        '--dns-name-label ${AZURE_DNS_NAME_LABEL}'

        // Fail the build if a deployment condition is not met.
        sh 'if curl --silent "http://${AZURE_DNS_NAME_LABEL}.${AZURE_RESOURCE_GROUP_LOCATION}.azurecontainer.io/actuator/health" | grep -q "UP"; then exit 0;  fi'
      }
      post {
        always {
          // This stage has been added as an always post condition to ensure resources created by this Jenkinsfile
          // are always torn down at the end. You may wish to keep the Azure resources and change this to a failure post condition.
          sh 'az group delete ' +
          '--yes ' +
          '--name ${AZURE_RESOURCE_GROUP_NAME}'
        }
      }
    }

  }

  post {
    always {
      echo "put global events for always here"
    }
    success {
      echo "put global events for success here"
    }
    failure {
      echo "put global events for failure here"
    }
  }
}
