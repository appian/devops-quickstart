# Appian DevOps Quick Start - Overview

This repository is intended to serve as a model for Appian developers looking to construct a CI/CD pipeline in order to version and test their applications. It contains all configuration files required for setting up a fully functioning pipeline using the out-of-the-box [Automated Deployment Manager](https://community.appian.com/w/the-appian-playbook/198/deployment-automation) and [FitNesse For Appian](https://community.appian.com/w/the-appian-playbook/97/automated-testing-with-fitnesse-for-appian), as well as an example pipeline, Jenkinsfile. While the Jenkinsfile is intended to be run in the CI/CD tool, Jenkins, the same structure can be used to setup a pipeline in other CI/CD tools, such as GitLab.

Repository Components:

## Jenkinsfile

A sample pipeline for use in Jenkins, or as a model for other CI/CD tools.

## appian

This folder is the outermost folder to house all Appian applications and their corresponding custom properties. 

All Appian applications are located in appian/applications. View this [README.md](appian/applications/README.md) for more information.

All corresponding custom properties files are located in appian/properties. View this [README.md](appian/properties/README.md) for more information. 

## devops

This folder houses all configuration properties for both the Automated Deployment Manager (adm) and FitNesse for Appian (f4a), as well as the custom test suites for f4a. For more information about this folder, view this [README.md](devops/README.md). 

All property files relevant for the ADM are located in devops/adm. This folder includes the properties files for the Automated Import Manager for each environment included in the pipeline, as well as the property files for the Automated Versioning Manager for both local developement and pipeline usage.

All property files and test_suites relevant for F4A are located in devops/f4m. This folder contains the properties files for FitNesse For Appian in both staging and test environments. In addition, this folder also contains test_suites folder, which houses all of the test suites for FitNesse For Appian. More information about the test_suites folder can be found in this [README.md](devops/f4a/README.md). 
