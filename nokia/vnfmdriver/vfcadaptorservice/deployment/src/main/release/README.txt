Introduction:
=============
    This document provides the required steps for installation
    and configuration this service.

Installation steps:
====================
    Install following software:
        - Java 1.8 or lattest
        - MySql Server 5.7 or lattest.
        - Apache Tomcat Server 8.0 or lattest

    Set the following environment variables:
        - JAVA_HOME: Set to JAVA JDK installed location
        - CATALINIA_HOME: Set to Tomcat installed location
        - CATALINIA_BASE: Set to the location, where this
          service installer is unzipped, its optional
        - PATH: Update it with the location of command 'mysql'

    - In command console, cd to 'bin' directory under the location,
      where this service installer is unzipped and
      run ./init_db.sh <db user> <db password> <db server ip> <db port>
        CAUTION: Existing vnfm_db will be cleaned before
        initializing the schema, so please take a back-up of it
        before executing it next time.

Configuration steps:
====================
    - Update the db credetials in 'application.properties' under webapps directory.
    - Update the MSB address in 'application.properties' under webapps directory.
    - Update the VNFM address in 'application.properties' under webapps directory.
    - Update the Catalog address in 'application.properties' under webapps directory.
    - Update the NSLCM address in 'application.properties' under webapps directory.


How to run?
===========
    - In command console, cd to 'bin' directory under the location,
      where this service installer is unzipped and
      run ./startup.sh
          NOTE: It starts the tomcat at predefined http port. To change
          default port, update the port in tomcat configuration file
          'conf/server.xml'
          - Verify that 'Tomcat started.' is reported on the console.
    - Once service is started, please verify below details:
        - from MSB service, verify that "nokiavnfm"  is reported from GET request on "/openoapi/microservices/v1/services"
        - from this service, run one of the supported REST API mentioned in open-o NFVO wiki and verify that the
          expected response is returned.
How to stop?
=============
    - In command console, cd to 'bin' directory under the location,
      where this service installer is unzipped and
      run ./shutdown.sh

Update:
=========
The latest information are available through the OPEN-O release notes at
https://wiki.open-o.org/view/OPEN-O_Sun_Release_Notes
