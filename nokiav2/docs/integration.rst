Integrate ONAP with Nokia VNFM
==============================

The following section describes how the Nokia VNFM can be integrated into ONAP. The integration is the easiest if the
VNFM is installed before ONAP.

Prepare the VNFM
----------------
* Start VNFM: The VNFM must be able to communicate with the ONAP VF-C interface, the cloud and the VNF, so the VNFM must
 have the correct network setup. The VNFM will use LCNs to notify VF-C of the executed changes, so the LCN zone of the
 VNFM must be configured so that the VNFM is able to reach the VF-C LCN interface.

* Register driver in CBAM

 - Log into CBAM via SSH and determine the keycloak admin password

  - ectl get /cbam/cluster/components/keycloak/admin_credentials/password

 - Log into keycloak https://<cbamIp>/auth/admin with admin username and password from previous step and change password (note the changed password)
 - Add a new client

  - set client id to onapClient
  - change credential type to confidential
  - enable Standard Flow Enabled, Direct Access Grants Enabled, Service Accounts Enabled
  - add * for redirect URL
  - save
  - note the client id <clientId>
  - add new credential
  - note the client secret <clientSecret>

 - Add a new user

  - note the username <onapUsername>
  - reset password
  - assign the "user" role to the created user

 - Log into CBAM GUI using the created user

  - change and note the password <onapPassword>

 - Add SSL certificates for all VIM connection or disable certificate verification

  - For insecure (all certificates are automatically trusted)

   - sudo su -
   - ectl set /cbam/cluster/components/tlm/insecure_vim_connection true
   - ectl set /actions/reconfigure start
   - journalctl -fu cbam-reconfigure.service
   - (wait for "Started cbam-reconfigure.service.")

  - For secure: (read CBAM documentation)

Prepare /ets/hosts file on your laptop (optional easier to copy paste URLs)
----------------------------------------------------------------------------

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

- Locate the IP address of the MSB (MSB_IP). Look at the VM instances of ONAP and search one with vm1-multi-service name. This is where the MSB is located
- Create VIM in A&AI (may already exist) (repeat for all clouds planed to be used)

 - http://msb.api.simpledemo.onap.org/iui/aai-esr-gui/extsys/vim/vimView.html

- Determine the tenant id to be used (log into the cloud) (repeat for all tenants planed to be used within the cloud)

 - http://<horizonUrl>/project/access_and_security/ Intentity / Projects

- Create tenant (may already exist) (repeat for all tenants planed to be used within the cloud)

 + tool: Postman
 + change tenantId, cloudRegion and cloudOwner
 + method: PUT
 + url: https://aai.api.simpledemo.onap.org:8443/aai/v11/cloud-infrastructure/cloud-regions/cloud-region/<cloudOwner>/<cloudRegion>/tenants/tenant/<tenantId>
 + Headers

  - basic auth AAI:AAI
  - X-FromAppId : any
  - Content-type: application/json
  - Accept: application/json

 - Content: :download:`aai.create.tenant.request.json <sample/aai.create.tenant.request.json>`

  - change tenant id, region id owner id and tenant name

- Register the VNFM as external system (repeat for all clouds planed to be used)

 - Visit MSB http://msb.api.simpledemo.onap.org:9518/api/aai-esr-server/v1/vims

  - note the cloud owner field <cloudOwner>
  - note the region id field <cloudRegionId>

 - Visit MSB http://msb.api.simpledemo.onap.org/iui/aai-esr-gui/extsys/vnfm/vnfmView.html and click on register button

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

 - Determine the UUID of the VNFM (if the VNFM was registered multiple times select one at random)

  - visit http://msb.api.simpledemo.onap.org:9518/api/aai-esr-server/v1/vnfms and search for the previously registered VNFM
  - note the id field <vnfmId>

Configure the SVNFM driver (generic)
------------------------------------

 - Download the CBAM SVNFM driver
   - docker pull https://nexus.onap.org/content/sites/raw/onap/vfc/nfvo/svnfm/nokiav2:1.1.0-STAGING-latest
 - Start the driver (fill in values)

.. code-block:: console

   export CBAM_IP=<cbamIp>
   export MULTI_NODE_IP=<multiNodeIp>
   export VNFM_ID=<vnfmId>
   export IMAGE_ID=<imageId>
   export CBAM_PASSWORD=<onapPassword>
   export CBAM_USERNAME=<onapUsername>
   docker run --name vfc_nokia -p 8089:8089 -e "MSB_IP=$MULTI_NODE_IP" -e "CONFIGURE=kuku" -e "EXTERNAL_IP=$MULTI_NODE_IP" -e "CBAM_CATALOG_URL=https://$CBAM_IP:443/api/catalog/adapter/" -e "CBAM_LCN_URL=https://$CBAM_IP:443/vnfm/lcn/v3/" -e "CBAM_KEYCLOAK_URL=https://$CBAM_IP:443/auth/" -e "CBAM_USERNAME=$CBAM_USERNAME" -e "CBAM_PASSWORD=$CBAM_PASSWORD" -e "VNFM_ID=$VNFM_ID" -d --stop-timeout 300 $IMAGE_ID

Configure the SVNFM driver (ONAP demo environment)
--------------------------------------------------
 - Configure the already running instance
 - docker exec -it `docker ps | grep nokiav2 | awk '{print $1}'` /bin/bash
 - Edit /service/application.properties
 - Fill values for cbamCatalogUrl, cbamLcnUrl, cbamKeyCloakBaseUrl, cbamUsername, cbamPassword, vnfmId
