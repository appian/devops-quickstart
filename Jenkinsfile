pipeline {
  agent any
  stages {
    stage("Install ADM and FitNesse for Appian") {
      steps {
        script {
          def jenkinsUtils = load "groovy/JenkinsUtils.groovy"

          // Retrieve and setup ADM
          sh "rm -rf adm f4a"
          jenkinsUtils.shNoTrace("curl -H X-JFrog-Art-Api:$ARTIFACTORYAPIKEY -O $ARTIFACTORYURL/appian-devops/adm.zip")
          sh "unzip adm.zip -d adm"
          sh "unzip adm/appian-adm-import*.zip -d adm/appian-import-client"
          jenkinsUtils.setProperty("adm/appian-import-client/metrics.properties", "pipelineUsage", "true")
          sh "unzip adm/appian-adm-versioning*.zip -d adm/appian-version-client"
          jenkinsUtils.setProperty("adm/appian-version-client/metrics.properties", "pipelineUsage", "true")

          // Retrieve and setup F4A
          jenkinsUtils.shNoTrace("curl -H X-JFrog-Art-Api:$ARTIFACTORYAPIKEY -O $ARTIFACTORYURL/appian-devops/f4a.zip")
          sh "unzip f4a.zip -d f4a"
          jenkinsUtils.setProperty("f4a/FitNesseForAppian/configs/metrics.properties", "pipeline.usage", "true")
          sh "cp -a devops/f4a/test_suites/. f4a/FitNesseForAppian/FitNesseRoot/FitNesseForAppian/Examples/"
          sh "cp devops/f4a/users.properties f4a/FitNesseForAppian/configs/users.properties"

          // WebDriver Docker Container setup
          sh "docker-compose -f docker/docker-compose.yml pull"
          jenkinsUtils.setProperty("f4a/FitNesseForAppian/configs/custom.properties", "firefox.host.port", "4444")
          jenkinsUtils.setProperty("f4a/FitNesseForAppian/configs/custom.properties", "chrome.host.port", "4445")
        }
      }
    }
    stage("Build Application Package from Repo") {
      steps {
        script {
          def jenkinsUtils = load "groovy/JenkinsUtils.groovy"
          jenkinsUtils.buildPackage("version-manager.properties")
        }
      }
    }
    stage("Deploy to Test") {
      steps {
        script {
          def jenkinsUtils = load "groovy/JenkinsUtils.groovy"
          jenkinsUtils.importPackage("import-manager.test.properties", "${APPLICATIONNAME}.test.properties")
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
    stage("Deploy to Staging") {
      steps {
        script {
          def jenkinsUtils = load "groovy/JenkinsUtils.groovy"
          jenkinsUtils.importPackage("import-manager.stag.properties", "${APPLICATIONNAME}.stag.properties")
        }
      }
    }
    stage("Tag Successful Import into Staging") {
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
    stage("Deploy to Production") {
      steps {
        script {
          def jenkinsUtils = load "groovy/JenkinsUtils.groovy"
          jenkinsUtils.importPackage("import-manager.prod.properties", "${APPLICATIONNAME}.prod.properties")
        }
      }
    }
    stage("Tag Successful Import into Production") {
      steps {
        script {
          def githubUtils = load "groovy/GitHubUtils.groovy"
          githubUtils.tagSuccessfulImport("PROD")
        }
      }
    }
  }
}
