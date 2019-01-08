import jenkins.model.*
import com.cloudbees.plugins.credentials.*
import com.cloudbees.plugins.credentials.common.*
import com.cloudbees.plugins.credentials.domains.*
import com.cloudbees.plugins.credentials.impl.*
import com.cloudbees.jenkins.plugins.sshcredentials.impl.*
import org.jenkinsci.plugins.plaincredentials.*
import org.jenkinsci.plugins.plaincredentials.impl.*
import hudson.util.Secret

println("Setting credentials")

def domain = Domain.global()
def store = Jenkins.instance.getExtensionList('com.cloudbees.plugins.credentials.SystemCredentialsProvider')[0].getStore()

def artifactoryPassword = new File("/run/secrets/artifactory-token").text.trim()
def artifactorySecret = Secret.fromString(artifactoryPassword)
def artifactoryCreds = new StringCredentialsImpl(CredentialsScope.GLOBAL, "artifactoryToken", "Artifactory Token", artifactorySecret)

def vcPassword = new File("/run/secrets/vc-password").text.trim()
def vcSecret = Secret.fromString(vcPassword)
def vcCreds = new StringCredentialsImpl(CredentialsScope.GLOBAL, "vcToken", "Version Control Access Token", vcSecret)

store.addCredentials(domain, artifactoryCreds)
store.addCredentials(domain, vcCreds)
