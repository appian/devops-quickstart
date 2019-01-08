#!/bin/bash

# Clean up from old runs
( docker stop $(docker ps -aq --filter ancestor=jenkins-configuration:CDO ) | xargs docker rm ) &> /dev/null
docker stack rm jenkins &> /dev/null

# Create all the secrets
base64_print() {
  printf  "%s" "$@" | base64 --decode
}

# Checking for just one of the secrets is enough because if one is there, all the other ones should be there as well
if [ -z "$(docker secret ls | awk '{print $2}' | grep jenkins-user | awk '{print $1}')" ]; then
  printf "\\n\\e[38;5;196m"; base64_print IGFkODg4ODg4ODg4OGJhICAgICAgIExldCdzIHNldCB1cCBTRUNSRVRTIQogZFAnICAgICAgICAgYCI4YiwKIDggICxhYWEsICAgICAgICJZODg4YSAgICAgLGFhYWEsICAgICAsYWFhLCAgLGFhLAogOCAgOCcgYDggICAgICAgICAgICI4OGJhYWRQIiIiIlliYWFhZFAiIiJZYmRQIiJZYgogOCAgOCAgIDggICAgICAgICAgICAgICIiIiAgICAgICAgIiIiICAgICAgIiIgICAgOGIKIDggIDgsICw4ICAgICAgICAgLGFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWRkZGRkODhQCiA4ICBgIiIiJyAgICAgICAsZDgiIgogWWIsICAgICAgICAgLGFkOCIgICAKICAiWTg4ODg4ODg4ODhQIg==
  printf "\\e[0m"; printf '\n'
fi

if [ -z "$(docker secret ls | awk '{print $2}' | grep jenkins-user | awk '{print $1}')" ]; then
  read -p "Please enter the username for the jenkins admin in this docker container: " JENKINS_USR
  echo $JENKINS_USR | docker secret create jenkins-user -
fi
if [ -z "$(docker secret ls | awk '{print $2}' | grep jenkins-pass | awk '{print $1}')" ]; then
  read -p "Please enter the password for this jenkins admin: " JENKINS_PASS
  echo $JENKINS_PASS | docker secret create jenkins-pass -
fi
if [ -z "$(docker secret ls | awk '{print $2}' | grep artifactory-token | awk '{print $1}')" ]; then
  read -p "Please enter your Artifactory access token: " ART_TOKEN
  echo $ART_TOKEN | docker secret create artifactory-token -
fi
if [ -z "$(docker secret ls | awk '{print $2}' | grep vc-password | awk '{print $1}')" ]; then
  read -p "Please enter your version control access token: " ART_TOKEN
  echo $VC_PASS | docker secret create vc-password -
fi

printf "\\n"

# Start the jenkins service
docker build --no-cache . -t jenkins-configuration:CDO
docker stack deploy -c jenkins.yml jenkins
