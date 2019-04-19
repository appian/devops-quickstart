#!/usr/bin/env groovy

void releaseApplication(tag, customProperties) {
  def releaseName = tagSuccessfulImport(tag)
  def remoteURL = getRemoteRepo("appian/applications/${APPLICATIONNAME}").split("/")
  def remoteRepo = remoteURL[-1].trim().minus(".git")
  def remoteOwner = remoteURL[-2].trim()

  shNoTrace("curl --user \"${REPOUSERNAME}:${REPOPASSWORD}\" -X GET https://api.github.com/repos/${remoteOwner}/${remoteRepo}/releases/tags/${releaseName} > releaseGetResponse.json")
  def previousURL = sh script: "jq \'.url\' releaseGetResponse.json", returnStdout: true
  previousURL = previousURL.trim()
  if (previousURL != "null") {
    shNoTrace("curl --user \"${REPOUSERNAME}:${REPOPASSWORD}\" -X DELETE ${previousURL}")
  }

  def body = "${APPLICATIONNAME} has passed integration and acceptance tests and is being released."
  shNoTrace("curl --user \"${REPOUSERNAME}:${REPOPASSWORD}\" -d \'{\"tag_name\":\"${releaseName}\", \"name\":\"${releaseName}\", \"body\":\"${body}\", \"draft\":false, \"prerelease\":false}\' -X POST https://api.github.com/repos/${remoteOwner}/${remoteRepo}/releases > releasePostResponse.json")

  def uploadURL = sh script: "jq \'.upload_url\' releasePostResponse.json", returnStdout: true
  uploadURL = uploadURL.trim().minus("{?name,label}")
  shNoTrace("curl -s --user \"${REPOUSERNAME}:${REPOPASSWORD}\" --header \"Content-Type:application/zip\" --data-binary \"@adm/app-package.zip\" -X POST ${uploadURL}?name=${releaseName}.zip")
  if (fileExists("appian/properties/${APPLICATIONNAME}/" + customProperties)) {
    shNoTrace("curl -s --user \"${REPOUSERNAME}:${REPOPASSWORD}\" --header \"Content-Type:application/text\" --data-binary \"@appian/properties/${APPLICATIONNAME}/${customProperties}\" -X POST ${uploadURL}?name=${releaseName}.properties")
  }
}

void tagSuccessfulImport(tag) {
  def releaseName = "${APPLICATIONNAME}_${tag}"
  dir("appian/applications/${APPLICATIONNAME}") {
    def currentCommit = sh script: "git log -n 1 --format='%h' ./", returnStdout: true
    def remoteURL = sh script: "git config --get remote.origin.url", returnStdout: true
    remoteURL = "https://${REPOUSERNAME}:${REPOPASSWORD}@" + remoteURL.split("https://")[1]
    sh "git checkout master"
    sh "git tag -af ${releaseName} -m 'The application ${APPLICATIONNAME} has successfully been imported into ${tag}' ${currentCommit}"
    shNoTrace("git push -f --follow-tags --repo=${remoteURL}")
  }
  return releaseName
}

void getRemoteRepo(path) {
  dir("${path}") {
    def remoteURL = sh script: "git config --get remote.origin.url", returnStdout: true
    return remoteURL
  }
}

def shNoTrace(cmd) {
  sh '#!/bin/sh -e\n' + cmd
}

return this
