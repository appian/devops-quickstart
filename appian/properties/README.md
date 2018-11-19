# Appian DevOps Quick Start - Application Customization Files

Any customization files generated for Appian applictions should be contained here. 

Within the folder <Application Name>, there should be 4 files:

<application_folder_name>.properties

<application_folder_name>.prod.properties

<application_folder_name>.stag.properties

<application_folder_name>.test.properties

The <application_folder_name>.properties file should be the commented-out file obtained on application export. The environment files should have appropriate values. 

**Example**:

* appian/properties
    * HelloAppianWorld
        * HelloAppianWorld.prod.properties
        * HelloAppianWorld.stag.properties
        * HelloAppianWorld.test.properties
        * HelloAppianWorld.properties
