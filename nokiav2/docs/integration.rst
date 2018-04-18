Integrate ONAP with Nokia VNFM
==============================

The following section describes how to integrate the Nokia Virtualized Network Function Manager (VNFM) into ONAP. The integration is easier if the
VNFM is installed before ONAP.

Prepare the VNFM
----------------

* Start the VNFM.
 The VNFM must be able to communicate with the ONAP VF-C interface, the virtualized infrastructure manager (VIM) and the virtualized network function (VNF), so the VNFM must
 have the correct network setup. The VNFM uses lifecycle notifications (LCNs) to notify the VF-C about the executed changes, therefore, the LCN zone of the
 VNFM must be configured so that the VNFM is able to reach the VF-C LCN interface.

* Register driver in CBAM

 - Using SSH, log in to the CloudBand Application Manager (CBAM) virtual machine as cbam user and determine the Keycloak
  auto-generated admin password with the following command: ectl get /cbam/cluster/components/keycloak/admin_credentials/password

  - Copy the printout of the command.

 - Access the Keycloak login page with the following URL: https://<cbamIp>/auth/admin where <cbamIp> is the FQDN or IP
 address assigned to CBAM node during instantiation. Optionally, it may contain a port, for example, cbam.mycompany.com:port or 1.2.3.4:port.

   Result: The Keycloak Administration Console login page loads up.

 - Log in to Keycloak with the 'admin' username and the auto-generated admin password you copied to clipboard, then change the auto-generated password and note the new password.

   Result: You are logged in to the Keycloak Administration Console.

 - Add a new client on Keycloak:

  - From the Configure menu, select Clients.

    - Result: The Clients pane appears.

  - Click Create.

    - Result: The Add Client pane appears.

  - Set the Client ID to onapClientId and click Save. Note the Client ID which will be referred to as <clientId>.

    - Result: The following notification appears: Success! The client has been created. The new client's profile page appears.

  - Customize the following settings for the newly created client:

    - Access Type: select confidential. Keycloak will generate a client secret that serves as a type of password for your client.

    - Make sure the following settings are ON: Standard Flow Enabled, Direct Access Grants Enabled, Service Accounts Enabled, Authorization Enabled

    - Type * in the Valid Redirect URIs field.

    - Click Save.

      - Result: The following notification appears: Success! Your changes have been saved to the client.

  - Note the Client Secret which will be referred to as <clientSecret>:

    - Select the Credentials tab.

    - From the Client Authenticator drop-down list, select the Client ID and Secret and check the value of Secret.

 - Add a new user on Keycloak:

  - From the Manage menu, select Users.

    - Result: The Users pane appears.

  - Click Add user and define the parameters for the creation:

    - Username: onap

      - Note the username, it will be referred to as <onapUsername>.

    - User Enabled: make sure it is On.

  - Click Save.

    - Result: The following notification appears: Success! The user has been created. The new user's profile page appears.

  - Create a password for the user: select the Credentials tab on the user profile and set the password.

    - Note: The user is prompted to change this password when logging in to CBAM for the first time.

  - Assign the "user" role to the created user:

    - Select the Role Mappings tab on the user profile.

    - Select the "user" role from the Available Roles box, then click Add selected.

 - Access the CBAM GUI login page with the following URL: https://<cbamIp> where <cbamIp> is the FQDN or IP address assigned to CBAM node during instantiation. Optionally, it may contain a port, for example, cbam.mycompany.com:port or 1.2.3.4:port.

 - Log in to CBAM GUI using the created user.

  - Change and note the password which will be referred to as <onapPassword>.

 - Using SSH, add SSL certificates for all VIM connections or disable certificate verification as follows:

  - For insecure connection (all certificates are automatically trusted)

   - execute the below commands in the following order:

.. code-block:: console

   sudo su -
   ectl set /cbam/cluster/components/tlm/insecure_vim_connection true
   ectl set /actions/reconfigure start
   journalctl -fu cbam-reconfigure.service

   - Wait for the "Started cbam-reconfigure.service." message.

  - For secure connection : read the CBAM documentation.

Prepare /ets/hosts file on your laptop
--------------------------------------

Note: This is an optional step with which it is easier to copy paste URLs

* Using the OpenStack Horizon Dashboard, find the ONAP servers you have deployed and note their IP addresses.

* Depending on your operating system, use the respective method to prepare an /ets/hosts file to link the DNS servers to the corresponding IP addresses, see the table below:

+-------------------+---------------------------------+
| IP address        | DNS entry                       |
+===================+=================================+
| <fill IP address> | portal.api.simpledemo.onap.org  |
+-------------------+---------------------------------+
| <fill IP address> | policy.api.simpledemo.onap.org  |
+-------------------+---------------------------------+
| <fill IP address> | sdc.api.simpledemo.onap.org     |
+-------------------+---------------------------------+
| <fill IP address> | vid.api.simpledemo.onap.org     |
+-------------------+---------------------------------+
| <fill IP address> | aai.api.simpledemo.onap.org     |
+-------------------+---------------------------------+
| <fill IP address> | msb.api.simpledemo.onap.org     |
+-------------------+---------------------------------+
| <fill IP address> | robot.api.simpledemo.onap.org   |
+-------------------+---------------------------------+

Add the VNFM driver to ONAP
---------------------------

- Locate and note the IP address of the MSB (MSB_IP) on the OpenStack Horizon Dashboard. Look at the VM instances of ONAP and find one with vm1-multi-service name. This is where the MSB is located.

- Create VIM in A&AI Note:

  - The VIM may already exist.

  - Repeat this step for all VIMs planned to be used.

 - Go to http://msb.api.simpledemo.onap.org/iui/aai-esr-gui/extsys/vim/vimView.html

   - Result: The ONAP platform opens.

 - On the platform, click Register.

   - Result: The registration form opens.

 - Fill in the fields.

   - Note: Cloud credentials are supplied by the VNF integrator.

   - To obtain the value of the Auth URL field and the tenant id (which will be required later), follow these steps:
     - Note: The actual steps depend on the OpenStack Dashboard version and vendor.
     - Go to OpenStack Horizon Dashboard.
     - Select the Project main tab.
     - Select the API Access tab.
     - Click View Credentials.
     - Copy the value of Authentication URL and paste it in the Auth URL field.
     - Note the value of Project ID: this is the <tenantId> which will be required later (Repeat this step for all tenants planned to be used within the VIM.)

 - Click Save.

   - Result: The driver has been successfully added.

- Create tenant

  - Note:

    - The tenant may already exist.

    - Repeat this step for all tenants planned to be used within the VIM.

 - Using a REST client of your choice, send a request to the following URL: https://aai.api.simpledemo.onap.org:8443/aai/v11/cloud-infrastructure/cloud-regions/cloud-region/<cloudOwner>/<cloudRegion>/tenants/tenant/<tenantId>

   - download the content of the request: `aai.create.tenant.request.json <sample/aai.create.tenant.request.json>`
   - In the request URL and in the content of the request, substitute <tenantId>, <cloudRegion> and <cloudOwner> with the respective values.
   - HTTP method: PUT
   - Set the following values in the Header of the request:

     - basic auth AAI:AAI
     - X-FromAppId : any
     - Content-type: application/json
     - Accept: application/json

- Register the VNFM as an external system:

  - Note: - Repeat this step for all VIMs planned to be used.

 - Access the following URL: http://msb.api.simpledemo.onap.org/iui/aai-esr-gui/extsys/vnfm/vnfmView.html

   - Result: The ONAP platform opens

 - On the platform, click Register.

   - Result: The registration form opens.

 - Fill in the fields as follows:

   - Note: Cloud credentials are supplied by the VNF integrator.

+-----------------+-----------------------------------+
| key             | Value                             |
+-----------------+-----------------------------------+
| Name            | CbamVnfm                          |
+-----------------+-----------------------------------+
| type            | NokiaSVNFM                        |
+-----------------+-----------------------------------+
| Vendor          | Nokia                             |
+-----------------+-----------------------------------+
| version         | v1                                |
+-----------------+-----------------------------------+
| URL             | https://<cbamIp>:443/vnfm/lcm/v3/ |
+-----------------+-----------------------------------+
| VIM             | <cloudOwner>_<cloudRegionId>      |
+-----------------+-----------------------------------+
| certificate URL |                                   |
+-----------------+-----------------------------------+
| Username        | <clientId>                        |
+-----------------+-----------------------------------+
| Password        | <clientSecret>                    |
+-----------------+-----------------------------------+

 - Click Save.

   - Result: The registration has been completed.

 - Determine the UUID of the VNFM:

   - Note: If the VNFM was registered multiple times, select one of them at random
   - Access the following URL: http://msb.api.simpledemo.onap.org:9518/api/aai-esr-server/v1/vnfms
   - Look for the previously registered VNFM and note the value of <vnfmId>.


Configure the SVNFM driver (generic)
------------------------------------

- Using SSH, download the CBAM SVNFM driver by executing the following command:
  docker pull https://nexus.onap.org/content/sites/raw/onap/vfc/nfvo/svnfm/nokiav2:1.1.0-STAGING-latest

- Determine the IMAGE ID:

 - Execute the following command: docker images
 - Find the required image and note the IMAGE ID.

- Start the driver:

 - Fill in the required values and execute the following:

.. code-block:: console

   export CBAM_IP=<cbamIp>
   export MULTI_NODE_IP=<multiNodeIp>
   export VNFM_ID=<vnfmId>
   export IMAGE_ID=<imageId>
   export CBAM_PASSWORD=<onapPassword>
   export CBAM_USERNAME=<onapUsername>
   docker run --name vfc_nokia -p 8089:8089 -e "MSB_IP=$MULTI_NODE_IP" -e "CONFIGURE=kuku" -e "EXTERNAL_IP=$MULTI_NODE_IP" -e "CBAM_CATALOG_URL=https://$CBAM_IP:443/api/catalog/adapter/" -e "CBAM_LCN_URL=https://$CBAM_IP:443/vnfm/lcn/v3/" -e "CBAM_KEYCLOAK_URL=https://$CBAM_IP:443/auth/" -e "CBAM_USERNAME=$CBAM_USERNAME" -e "CBAM_PASSWORD=$CBAM_PASSWORD" -e "VNFM_ID=$VNFM_ID" -d --stop-timeout 300 $IMAGE_ID

- Determine the identifier of the container:

 - Execute the following command: docker ps
 - Find the required container and note the CONTAINER ID (first column/first row on the list).

- Verify if the VNFM driver has been successfully started by executing the following commands:

.. code-block:: console

  execute docker exec -it <containerId> /bin/bash
  execute tail -f service.log

  - Result: The SVNFM integration is successful if the end of the command output contains "Started NokiaSvnfmApplication".

- Verify if the SVNFM is registered into MSB:

 - Go to http://msb.api.simpledemo.onap.org/msb
 - Check if NokiaSVNFM micro service is present in the boxes.


Configure the SVNFM driver (ONAP demo environment)
--------------------------------------------------
This step is executed instead of the "Configure the SVNFM driver (generic)" in case of an ONAP demo environment.

- Configure the already running instance:

 - Execute the following command: docker exec -it `docker ps | grep nokiav2 | awk '{print $1}'` /bin/bash
 - Edit /service/application.properties:

   - In this file, change the default values of the following keys to the correct values: cbamCatalogUrl, cbamLcnUrl, cbamKeyCloakBaseUrl, cbamUsername, cbamPassword, vnfmId
