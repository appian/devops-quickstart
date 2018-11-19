# Appian DevOps Quick Start - Custom Test Suites

This folder should contain the integration/acceptance test suites that are developed within the FitNesseForAppian tool.

## Quickstart Example

The provided QuickStartIntegrationTest and QuickStartAcceptanceTest serve as a quick walkthrough of how a FitNesse test script can be used in the pipeline. To set up use of this example in the default quickstart pipeline, make sure to first have a fully functional Appian environment and then follow the directions below:

1. Edit the QuickStartIntegrationTest/content.txt and QuickStartAcceptanceTest/content.txt files:
    * Replace 'APPIAN_URL' with your Appian URL, beginning with https:// and including /suite at the end (e.g. https://forum.appian.com/suite)
    * Replace 'APPIAN_VERSION' with the version of Appian your test site is running (17.1, 17.2, 17.3, 17.4, 18.1, 18.2, 18.3 or 18.4) 
    * Replace 'APPIAN_LOCALE' with (en_US or en_GB) to handle differences in how dates are displayed
    * Replace 'APPIAN_USERNAME' with an authorized username for Appian URL above
2. The 'APPIAN_USERNAME' provided needs to be configured with the corresponding password so the script can login to the test site. To do this:
    * Open the "users.properties" file found in devops/f4a
    * Add a line in the file that is of the form *APPIAN_USERNAME=PASSWORD* where the *APPIAN_USERNAME* is the username provided above and the *PASSWORD* is the corresponding password for that username
    * Save this file
3. The pipeline is now ready to execute with this Quickstart example serving as the integration/acceptance test used to progress the pipeline

## Custom Integration/Acceptance Tests with FitNesseForAppian

To create your own integration/acceptance FitNesse tests, you can use the FitNesseForAppian tool to develop custom tests. You can read all about this tool [here](https://community.appian.com/w/the-appian-playbook/97/automated-testing-with-fitnesse-for-appian).

As you create standalone tests and bigger test suites within your FitNesseForAppian installation, your local filesystem will reflect the changes made within the tool. For example, adding a new test under the "Examples" heading will create a new directory corresponding to that test page in your filesystem (e.g. FitNesseForAppian/FitNesseRoot/FitNesseForAppian/Examples/NewTest). 

## Pipeline Integration

To integrate all your newly created test suites into this pipeline example:

1. Copy all the corresponding test folders from the FitNesseForAppian installation directory
1. Paste all those folders into this directory (devops/f4a/test_suites)
1. Navigate to the parent directory (devops/f4a) and edit the following files:
    * "fitnesse-automation.acceptance.properties"
    * "fitnesse-automation.integrate.properties"
1. Edit the "testPath" field to include the name of a particular test folder that was copied into this directory.(e.g. testPath=FitNesseForAppian.Examples.**QuickStartF4AExample**?suite). Only edit the **bolded** part
1. The properties files are used in the pipeline to kick-off desired integration/acceptance test suites
    * The pasted folders now serve as integration/acceptance tests instead of the default QuickStartF4AExample
