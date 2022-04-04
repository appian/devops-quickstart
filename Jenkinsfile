pipeline {
  agent any
environment {

SITEBASEURL = null
APIKEY = null
PACKAGEFILENAME = null
initiateInspectionJson = null
deploymentResponseJson = null
response = null
warnings = null
errors = null
DEPLOYMENTNAME = null
DEPLOYMENTDESCRIPTION = null
}
  stages {
    
    
    stage("Install AVM and FitNesse for Appian") {
      steps {
        script {
          def jenkinsUtils = load "groovy/JenkinsUtils.groovy"

          // Retrieve and setup ADM
          sh "rm -rf adm f4a"
          jenkinsUtils.shNoTrace("curl -H X-JFrog-Art-Api:$ARTIFACTORYAPIKEY -O $ARTIFACTORYURL/appian-devops/adm.zip")
          sh "unzip devops/adm.zip -d adm"
          sh "unzip adm/appian-adm-versioning-client-2.5.17.zip -d adm/appian-version-client"
          jenkinsUtils.setProperty("adm/appian-version-client/metrics.properties", "pipelineUsage", "true")
          // Retrieve and setup F4A
          jenkinsUtils.shNoTrace("curl -H X-JFrog-Art-Api:$ARTIFACTORYAPIKEY -O $ARTIFACTORYURL/appian-devops/f4a.zip")
          sh "unzip f4a.zip -d f4a"
          jenkinsUtils.setProperty("f4a/FitNesseForAppian/configs/metrics.properties", "pipeline.usage", "true")
          sh "cp -a devops/f4a/test_suites/. f4a/FitNesseForAppian/FitNesseRoot/FitNesseForAppian/Examples/"
          sh "cp devops/f4a/users.properties f4a/FitNesseForAppian/configs/users.properties"
          
        }
      }
    }
    stage("Build Package") {
      steps {
        script {
          def jenkinsUtils = load "groovy/JenkinsUtils.groovy"
          jenkinsUtils.buildPackage("version-manager.properties")
        }
      }
    }

    stage("Inspect Package - Test") {
      steps {
        script {
          def properties = readProperties file: "devops\\deploymentmanagement.test.properties"
          DEPLOYMENTDESCRIPTION = properties['deploymentDescription']
          DEPLOYMENTNAME = properties['deploymentName']
          SITEBASEURL = properties['url']
          APIKEY = properties['siteApiKey']
          PACKAGEFILENAME = properties['packageFileName']
          def jenkinsUtils = load "groovy/JenkinsUtils.groovy"
          jenkinsUtils.inspectPackage("${APPLICATIONNAME}.test.properties")
        }
      }
    }
    
    stage("Create Deployment Request - Test") {
      steps {
        script {
          def properties = readProperties file: "devops\\deploymentmanagement.test.properties"
          DEPLOYMENTDESCRIPTION = properties['deploymentDescription']
          DEPLOYMENTNAME = properties['deploymentName']
          SITEBASEURL = properties['url']
          APIKEY = properties['siteApiKey']
          PACKAGEFILENAME = properties['packageFileName']
          def jenkinsUtils = load "groovy/JenkinsUtils.groovy"
          jenkinsUtils.createDeployment("${APPLICATIONNAME}.test.properties")
          


        }
      }
    }
    stage("Check Deployment Status - Test") {
      steps {
        script {
          def properties = readProperties file: "devops\\deploymentmanagement.test.properties"
          DEPLOYMENTDESCRIPTION = properties['deploymentDescription']
          DEPLOYMENTNAME = properties['deploymentName']
          SITEBASEURL = properties['url']
          APIKEY = properties['siteApiKey']
          PACKAGEFILENAME = properties['packageFileName']
          def jenkinsUtils = load "groovy/JenkinsUtils.groovy"
          jenkinsUtils.checkDeploymentStatus()
        }
      }
    }
    stage("Tag Successful Import into Test") {
      steps {
        script {
          def githubUtils = load "groovy/GitHubUtils.groovy"
          githubUtils.tagSuccessfulImport("TEST")
        }
      }
    }
    stage("Run Integration Tests") {
      steps {
        script {
          def jenkinsUtils = load "groovy/JenkinsUtils.groovy"
          jenkinsUtils.runTestsDocker("fitnesse-automation.integrate.properties")
        }
      }
      post {
        always {
          sh script: "docker-compose -f docker/docker-compose.yml down", returnStatus: true
          dir("f4a/FitNesseForAppian"){ junit "fitnesse-results.xml" }
        }
        failure {
          script {
            def jenkinsUtils = load "groovy/JenkinsUtils.groovy"
            archiveArtifacts artifacts: jenkinsUtils.retrieveLogs("fitnesse-automation.integrate.properties"), fingerprint: true
          }
        }
      }
    }
    stage("Inspect Package - Stag") {
      steps {
        script {
          def properties = readProperties file: "devops\\deploymentmanagement.stag.properties"
          DEPLOYMENTDESCRIPTION = properties['deploymentDescription']
          DEPLOYMENTNAME = properties['deploymentName']
          SITEBASEURL = properties['url']
          APIKEY = properties['siteApiKey']
          PACKAGEFILENAME = properties['packageFileName']
          def jenkinsUtils = load "groovy/JenkinsUtils.groovy"
          jenkinsUtils.inspectPackage("${APPLICATIONNAME}.stag.properties")
        }
      }
    }
    
    stage("Create Deployment Request - Stag") {
      steps {
        script {
          def properties = readProperties file: "devops\\deploymentmanagement.stag.properties"
          DEPLOYMENTDESCRIPTION = properties['deploymentDescription']
          DEPLOYMENTNAME = properties['deploymentName']
          SITEBASEURL = properties['url']
          APIKEY = properties['siteApiKey']
          PACKAGEFILENAME = properties['packageFileName']
          def jenkinsUtils = load "groovy/JenkinsUtils.groovy"
          jenkinsUtils.createDeployment("${APPLICATIONNAME}.stag.properties")
          


        }
      }
    }
    stage("Check Deployment Status - Stag") {
      steps {
        script {
          def properties = readProperties file: "devops\\deploymentmanagement.stag.properties"
          DEPLOYMENTDESCRIPTION = properties['deploymentDescription']
          DEPLOYMENTNAME = properties['deploymentName']
          SITEBASEURL = properties['url']
          APIKEY = properties['siteApiKey']
          PACKAGEFILENAME = properties['packageFileName']
          def jenkinsUtils = load "groovy/JenkinsUtils.groovy"
          jenkinsUtils.checkDeploymentStatus()
        }
      }
    }
    stage("Tag Successful Import into Stag") {
      steps {
        script {
          def githubUtils = load "groovy/GitHubUtils.groovy"
          githubUtils.tagSuccessfulImport("STAG")
        }
      }
    }
    stage("Run Acceptance Tests") {
      steps {
        script {
          def jenkinsUtils = load "groovy/JenkinsUtils.groovy"
          jenkinsUtils.runTestsDocker("fitnesse-automation.acceptance.properties")
        }
      }
      post {
        always { 
          sh script: "docker-compose -f docker/docker-compose.yml down", returnStatus: true
          dir("f4a/FitNesseForAppian"){ junit "fitnesse-results.xml" }
        }
        failure {
          script {
            def jenkinsUtils = load "groovy/JenkinsUtils.groovy"
            archiveArtifacts artifacts: jenkinsUtils.retrieveLogs("fitnesse-automation.acceptance.properties"), fingerprint: true
          }
        }
      }
    }
    stage("Create Application Release") {
      steps {
        script {
          def githubUtils = load "groovy/GitHubUtils.groovy"
          githubUtils.releaseApplication("RELEASE", "${APPLICATIONNAME}.properties")
        }
      }
    }
    stage("Promotion Decision") {
      steps {
        input "Deploy to Production?"
      }
    }
    stage("Inspect Package - Prod") {
      steps {
        script {
          def properties = readProperties file: "devops\\deploymentmanagement.prod.properties"
          DEPLOYMENTDESCRIPTION = properties['deploymentDescription']
          DEPLOYMENTNAME = properties['deploymentName']
          SITEBASEURL = properties['url']
          APIKEY = properties['siteApiKey']
          PACKAGEFILENAME = properties['packageFileName']
          def jenkinsUtils = load "groovy/JenkinsUtils.groovy"
          jenkinsUtils.inspectPackage("${APPLICATIONNAME}.prod.properties")
        }
      }
    }
    
    stage("Create Deployment Request - Prod") {
      steps {
        script {
          def properties = readProperties file: "devops\\deploymentmanagement.prod.properties"
          DEPLOYMENTDESCRIPTION = properties['deploymentDescription']
          DEPLOYMENTNAME = properties['deploymentName']
          SITEBASEURL = properties['url']
          APIKEY = properties['siteApiKey']
          PACKAGEFILENAME = properties['packageFileName']
          def jenkinsUtils = load "groovy/JenkinsUtils.groovy"
          jenkinsUtils.createDeployment("${APPLICATIONNAME}.prod.properties")
          


        }
      }
    }
    stage("Check Deployment Status - Prod") {
      steps {
        script {
          def properties = readProperties file: "devops\\deploymentmanagement.prod.properties"
          DEPLOYMENTDESCRIPTION = properties['deploymentDescription']
          DEPLOYMENTNAME = properties['deploymentName']
          SITEBASEURL = properties['url']
          APIKEY = properties['siteApiKey']
          PACKAGEFILENAME = properties['packageFileName']
          def jenkinsUtils = load "groovy/JenkinsUtils.groovy"
          jenkinsUtils.checkDeploymentStatus()
        }
      }
    }
    stage("Tag Successful Import into Prod") {
      steps {
        script {
          def githubUtils = load "groovy/GitHubUtils.groovy"
          githubUtils.tagSuccessfulImport("PROD")
        }
      }
    }
  }
}


