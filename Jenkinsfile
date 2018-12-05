pipeline {
  agent any
  stages {
    stage("Install ADM and FitNesse for Appian") {
      steps {
        sh "rm -rf adm f4a"
        shNoTrace "curl -H X-JFrog-Art-Api:$ARTIFACTORYAPIKEY -O $ARTIFACTORYURL/appian-devops/adm.zip"
        sh "unzip adm.zip -d adm"
        sh "unzip adm/appian-adm-import*.zip -d adm/appian-import-client"
        setProperty("adm/appian-import-client/metrics.properties", "pipelineUsage", "true")
        sh "unzip adm/appian-adm-versioning*.zip -d adm/appian-version-client"
        setProperty("adm/appian-version-client/metrics.properties", "pipelineUsage", "true")
        shNoTrace "curl -H X-JFrog-Art-Api:$ARTIFACTORYAPIKEY -O $ARTIFACTORYURL/appian-devops/f4a.zip"
        sh "unzip f4a.zip -d f4a"
        setProperty("f4a/FitNesseForAppian/configs/metrics.properties", "pipeline.usage", "true")
        sh "cp -a devops/f4a/test_suites/. f4a/FitNesseForAppian/FitNesseRoot/FitNesseForAppian/Examples/"
        sh "cp devops/f4a/users.properties f4a/FitNesseForAppian/configs/users.properties"
      }
    }
    stage("Build Application Package from Repo") {
      steps {
        buildPackage("version-manager.properties")
      }
    }
    stage("Deploy to Test") {
      steps {
        importPackage("import-manager.test.properties", "${APPLICATIONNAME}.test.properties")
      }
    }
    stage("Tag Successful Import into Test") {
      steps {
        tagSuccessfulImport("TEST")
      }
    }
    stage("Run Integration Tests") {
      steps {
        runTests("fitnesse-automation.integrate.properties")
      }
      post {
        always { dir("f4a/FitNesseForAppian"){ junit "fitnesse-results.xml" } }
        failure { archiveArtifacts artifacts: retrieveLogs("fitnesse-automation.integrate.properties"), fingerprint: true }
      }
    }
    stage("Deploy to Staging") {
      steps {
        importPackage("import-manager.stag.properties", "${APPLICATIONNAME}.stag.properties")
      }
    }
    stage("Tag Successful Import into Staging") {
      steps {
        tagSuccessfulImport("STAG")
      }
    }
    stage("Run Acceptance Tests") {
      steps {
        runTests("fitnesse-automation.acceptance.properties")
      }
      post {
        always { dir("f4a/FitNesseForAppian"){ junit "fitnesse-results.xml" } }
        failure { archiveArtifacts artifacts: retrieveLogs("fitnesse-automation.acceptance.properties"), fingerprint: true }
      }
    }
    stage("Create Application Release") {
      steps {
        releaseApplication("RELEASE", "${APPLICATIONNAME}.properties")
      }
    }
    stage("Promotion Decision") {
      steps {
        input "Deploy to Production?"
      }
    }
    stage("Deploy to Production") {
      steps {
        importPackage("import-manager.prod.properties", "${APPLICATIONNAME}.prod.properties")
      }
    }
    stage("Tag Successful Import into Production") {
      steps {
        tagSuccessfulImport("PROD")
      }
    }
  }
}

void releaseApplication(tag, customProperties) {
  def releaseName = tagSuccessfulImport(tag)
  def remoteURL = getRemoteRepo("appian/applications/$APPLICATIONNAME").split("/")
  def remoteRepo = remoteURL[-1].trim().minus(".git")
  def remoteOwner = remoteURL[-2].trim()

  shNoTrace "curl --user \"${REPOUSERNAME}:${REPOPASSWORD}\" -X GET https://api.github.com/repos/${remoteOwner}/${remoteRepo}/releases/tags/${releaseName} > releaseGetResponse.json"
  def previousURL = sh script: "jq \'.url\' releaseGetResponse.json", returnStdout: true
  previousURL = previousURL.trim()
  if (previousURL != "null") {
    shNoTrace "curl --user \"${REPOUSERNAME}:${REPOPASSWORD}\" -X DELETE ${previousURL}"
  }

  def body = "${APPLICATIONNAME} has passed integration and acceptance tests and is being released."
  shNoTrace "curl --user \"${REPOUSERNAME}:${REPOPASSWORD}\" -d \'{\"tag_name\":\"${releaseName}\", \"name\":\"${releaseName}\", \"body\":\"${body}\", \"draft\":false, \"prerelease\":false}\' -X POST https://api.github.com/repos/${remoteOwner}/${remoteRepo}/releases > releasePostResponse.json"

  def uploadURL = sh script: "jq \'.upload_url\' releasePostResponse.json", returnStdout: true
  uploadURL = uploadURL.trim().minus("{?name,label}")
  shNoTrace "curl -s --user \"${REPOUSERNAME}:${REPOPASSWORD}\" --header \"Content-Type:application/zip\" --data-binary \"@adm/app-package.zip\" -X POST ${uploadURL}?name=${releaseName}.zip"
  if (fileExists("appian/properties/${APPLICATIONNAME}/" + customProperties)) {
    shNoTrace "curl -s --user \"${REPOUSERNAME}:${REPOPASSWORD}\" --header \"Content-Type:application/text\" --data-binary \"@appian/properties/${APPLICATIONNAME}/${customProperties}\" -X POST ${uploadURL}?name=${releaseName}.properties"
  }
}

void tagSuccessfulImport(tag) {
  def releaseName = "${APPLICATIONNAME}_${tag}"
  dir("appian/applications/$APPLICATIONNAME") {
    def currentCommit = sh script: "git log -n 1 --format='%h' ./", returnStdout: true
    def remoteURL = getRemoteRepo("appian/applications/$APPLICATIONNAME")
    remoteURL = "https://${REPOUSERNAME}:${REPOPASSWORD}@" + remoteURL.split("https://")[1]
    sh "git checkout master"
    sh "git tag -af ${releaseName} -m 'The application $APPLICATIONNAME has successfully been imported into ${tag}' ${currentCommit}"
    shNoTrace "git push -f --follow-tags --repo=${remoteURL}"
  }
  return releaseName
}

void getRemoteRepo(path) {
  dir("${path}") {
    def remoteURL = sh script: "git config --get remote.origin.url", returnStdout: true
    return remoteURL
  }
}

void runTests(propertyFile) {
  sh "cp devops/f4a/" + propertyFile + " f4a/FitNesseForAppian/fitnesse-automation.properties"
  dir("f4a/FitNesseForAppian") {
    wrap([$class:'Xvnc', useXauthority: true]) {
      sh script: "bash ./runFitNesseTest.sh"
    }
  }
}

void retrieveLogs(propertyFile) {
  def test = sh script: "cat \"devops/f4a/${propertyFile}\" | grep \"testPath=\" | cut -d'=' -f2", returnStdout: true
  test = test.trim().minus(~"\\?.*")
  def zipName = "${test}_Results.zip"
  dir("f4a/FitNesseForAppian/FitNesseRoot/files/testResults") {
    sh "zip -r ${zipName} ${test}/**"
  }
  return "f4a/FitNesseForAppian/FitNesseRoot/files/testResults/${zipName}"
}

void buildPackage(versionPropertyFile) {
  sh "cp devops/adm/" + versionPropertyFile + " adm/appian-version-client/version-manager.properties"
  dir("adm/appian-version-client") {
    sh "sed -i -e 's|#vcUsername=.*|vcUsername=$REPOUSERNAME|' version-manager.properties"
    shNoTrace "sed -i -e 's|#vcPassword=.*|vcPassword=$REPOPASSWORD|' version-manager.properties"
    sh "sed -i -e 's|#appianObjectsRepoPath=.*|appianObjectsRepoPath=appian/applications/$APPLICATIONNAME|' version-manager.properties"
    sh "./version-application.sh -package_path ../app-package.zip"
  }
}

void importPackage(importPropertyFile, customProperties) {
  sh "cp devops/adm/" + importPropertyFile + " adm/appian-import-client/import-manager.properties"
  dir("adm/appian-import-client") {
    sh "sed -i -e 's|#username=.*|username=$SITEUSERNAME|' import-manager.properties"
    shNoTrace "sed -i -e 's|#password=.*|password=$SITEPASSWORD|' import-manager.properties"
    if (fileExists("../../appian/properties/${APPLICATIONNAME}/" + customProperties)) {
      sh "sed -i -e 's|#importCustomizationPath=.*|importCustomizationPath=../../appian/properties/$APPLICATIONNAME/" + customProperties + "|' import-manager.properties"
    }
    sh "./deploy-application.sh -application_path ../app-package.zip"
  }
}

void setProperty(filePath, property, propertyValue) {
  sh "sed -i -e 's|${property}=.*|${property}=${propertyValue}|' ${filePath}"
}

def shNoTrace(cmd) {
  sh '#!/bin/sh -e\n' + cmd
}
