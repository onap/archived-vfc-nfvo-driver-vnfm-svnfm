On-board NS
===========

The following section describes how to create an E2E service.

Create licensing model
----------------------

The following section will create a license model. The license model is required for onboarding a VNF.

- Log into ONAP portal with designer role (cs0008)

- Select SDC from the application

- Select ONBOARD

- Click on CREATE NEW VLM

- Specify the name of the license <licenseName>

- Specify the description of the license

- Select Entitlement pools from left

- Click on ADD ENTITLEMENT POOL on right

  - Specify the name (can be anything)

  - Click on SAVE

- Select License key groups from left

- Click on ADD LICENSE KEY GROUP

  - Specify the name (can be anything)

  - Select universal type

  - Click on SAVE

- Select Feature groups from left

- Click on ADD FEATURE GROUP

 - Specify the name (can be anything) <featureGroup>

 - Set 123456 as part number

 - Set 123456 as manufacturer reference number

 - Click on entitlement pools in middle

   - Add previously created entitlement pool with arrow button

 - Click on license key groups in middle

   - Add previously created license key group with arrow button

 - Click on SAVE

- Select License agreements from left

- Click on ADD LICENSE AGREEMENT on right

 - Specify the name (can be anything)

 - Select unlimited license term

 - Click on feature groups

   - Add previously created feature group with arrow button

- Click on the submit button on top right

Prepare the ETSI configuration JSON
-----------------------------------

The ETSI configuration of VF is the information gap that is required to instantiate a VNF, but this information is not
provided by the VF-C component to the VNFM. The ETSI configuration is a JSON serialized into a string, which is specified
as a VF property during VF design time.

The JSON has the following root elements:

- vimType: The type of the VIM

- instantiationLevel: The initial instantiation level of the VNF.

- computeResourceFlavours: The collection of compute flavors.

- zones: The collection of availability zones.

- softwareImages: The collection of software images.

- extManagedVirtualLinks: The collection of externally managed virtual links.

- externalConnectionPointAddresses: Addresses of the external connection points.

- extVirtualLinks: The collection of external virtual links.

- extensions: The collection of VNF properties

- additionalParams: Additional parameters passed during instantiation to the VNFM.

- domain: The domain of the OpenStack cloud (only available in Amsterdam)


On-board VNF
------------

The following section requires the CSAR and the ETSI configuration of the VNF to be available as a prerequisite.

- Log into ONAP portal with designer role (cs0008)

- Select SDC from the application

- Select ONBOARD

- Click on CREATE NEW VSP

  - Specify a the name of the VNF <vnfPackageName> (ex. vnf_simple_20180526_1)

  - Select the previously created license model as vendor

  - Select Database (General) for the category. It is important to select a category that has been linked to the customer.

  - Select network package for onboarding procedure

  - Specify description

  - Click on CREATE

- Click on SELECT FILE from SOFTWARE PRODUCT ATTACHMENTS and upload the CSAR file

- Click on General on the left

  - Select 1.0 as for licensing version under LICENSES

  - Select the previously created license agreement <licenseName>

  - Select the previously created feature group <featureGroup>

  - Click on save icon at top right

  - Click on Submit icon at top right

- Select HOME using the small arrow left from ONBOARD at top

- Hoover over the import icon and select Import VSP

- Select the created VSP from the list by name <vnfPackageName>

- Click on import VSP icon

  - Set the CBAM VNF package identifier as the Vendor model number on left bottom

  - Click on create on top right

- Click on Properties assignment on left

  - Click on Inputs

    - Specify NokiaSVNFM for the nf_type property

  - Click on Save on the middle

  - Click on Inputs

    - Specify the ETSI configuration JSON for the etsi_config property

    - If the ETSI configuration is larger than the maximal allowed value for a field

      - Instead of specifying it using a property click on Deployment Artifact at left

      - Click on Add other artifact at bottom middle

        - Specify etsiConfig as Artifact Label

        - Choose OTHER for type

        - Specify anything for Description

        - Select the ETSI configuration file using Browse

          - the name of the file on the local file system must be etsi_config.json

        - Click on Done

- Click on Check in

- Search for the created VNF using the search box at top right <vnfPackageName>

- Click on the VF

- Click on submit for testing at top right

- Log out using the small person icon at top right

- Log in with tester role (jm0007)

- Select SDC from the application

- Search for the created VNF using the search box at top right <vnfPackageName>

- Click on Start testing

- Click on Accept

- Log out using the small person icon at top right


Design a network service
------------------------

The following section design a network service. The prerequisite is that the tested VF package is available.

- Log into ONAP portal with designer role (cs0008)

- Select SDC from the application

- Select HOME

- Hoover over the Add icon and select add service

  - Specify the name of the network service <nsName> (ex. ns_simple_20180526_1)

  - Specify 123456 ad project code

  - Specify description

  - Select Network Service for the category

    - If the network service is missing from the list

      - Log in as demo user and select SDC

  - Click on Create

  - Click on Composition at left

    - Search for the created VF using the search box at top left <vnfPackageName>

    - Drag the VF icon to middle

    - Wait for the icon to appear at the middle (only drag once)

    - Click on the icon on the middle

      - Click on very small pencil icon at top right

      - Specify the name of the VF

  - Click on check in

  - Search for the created NS using the search box at top right <nsName>

  - Click on the NS icon

  - Click on Submit for testing

- Log out using the small person icon at top right

- Log in with tester role (jm0007)

  - Select SDC from the application

  - Search for the created VNF using the search box at top right <vnfPackageName>

  - Click on Start testing

  - Click on Accept

  - Log out using the small person icon at top right

- Log in with tester role (gv0001)

  - Select SDC from the application

  - Search for the created VNF using the search box at top right <vnfPackageName>

  - Click on Approve on top right

  - Log out using the small person icon at top right

- Log in with operation role (op0001)

  - Select SDC from the application

  - Search for the created VNF using the search box at top right <vnfPackageName>

  - Click on Distribute on top right

  - Click on monitor in an order to verify that the distribution was successful

    - Click on the small arrow next to the Distribution ID

    - The list should contain at least two lines staring with sdc and aai-ml

    - If the list does not contain enough elements it can be refreshed with the small icon at middle right

    - Each of the two lines should contain a green check sign next to Deployed

  - Log out using the small person icon at top right



Design a VF for the E2E service
-------------------------------

This step is only required if the UUI is planed to be used to manage the E2E service. The VF of the E2E service is
a wrapper to be able to treat the created NS as a VF. The prerequisite of this step is that the network service was
successfully distributed.

- Determine the UUIDs of the created NS in previous step

  - Using a REST client of your choice, send a request to the following URL: https://sdc.api.simpledemo.onap.org:8443/sdc/v1/catalog/services

    - HTTP method: GET

    - Set the following values in the Header of the request:

      - basic auth SDC:SDC

      - X-ECOMP-InstanceID: VFC

      - Accept: application/json

  - Search for the created service by name <nsName> and note the uuid and invariantUUID fields

- Log in with designer role (cs0008)

  - Select SDC from the application

  - Select HOME

  - Hoover over the Add icon and select add VF

    - Specify the name of the VF <vfForNsName> (ex. vf_for_ns_simple_20180526_1_vIMS)

      - the name must contain the vIMS character sequence (even if this is not an IMS)

    - Specify something for the Vendor

    - Specify any numeric value for the Vendor Release

    - Specify something for description

    - Select Network Service for the category

    - Click on Create on top right

  - Click on Composition at left

    - Search for the NSD using the search box at top left

    - Drag the NSD icon to middle

    - Wait for the icon to appear at the middle (only drag once)

    - Click on the icon on the middle

      - Click on very small pencil icon at top right

      - Specify the name of the NSD (ex. firstNsd ) <nsdName>

    - Click on the name of the VF next to HOME at top

    - Select Properties Assignment

      - Select the check box before providing_service_uuid and providing_service_invariant_uuid properties

      - Click on Declare at right

      - Click on Inputs at middle

      - Specify the UUID of the service (that was determined in previous step) for the <nsdName>_providing_service_uuid property

      - Specify the invariant UUID of the service (that was determined in previous step) for the <nsdName>_providing_service_invariant_uuid property

      - Click on Save

  - Click on Check in

  - Search for the created VF using the search box at top right <vfForNsName>

  - Click on the VF icon

  - Click on Submit for testing

- Log out using the small person icon at top right

- Log in with tester role (jm0007)

  - Select SDC from the application

  - Search for the created VNF using the search box at top right <vfForNsName>

  - Click on Start testing

  - Click on Accept

  - Log out using the small person icon at top right


Design a E2E service
--------------------

This step is only required if the UUI is planed to be used to manage the E2E service. The prerequisite of this step is that the VF
wrapping the network service is tested.

- Log in with designer role (cs0008)

  - Select SDC from the application

  - Select HOME

  - Hoover over the Add icon and select add Service

    - Specify the name of the NS <e2eNsName> (ex. e2e_simple_20180526_1)

    - Specify any numeric value for the Project Code

    - Specify something for description

    - Select E2E Service for the category

    - Click on Create on top right

  - Click on Composition at left

    - Search for the created VF using the search box at top left <vfForNsName>

    - Drag the VF icon to middle

    - Wait for the icon to appear at the middle (only drag once)

    - Click on the name of the NS next to HOME at top

  - Click on Check in

  - Search for the created NS using the search box at top right <e2eNsName>

  - Click on the NS icon

  - Click on Submit for testing

- Log out using the small person icon at top right

- Log in with tester role (jm0007)

  - Select SDC from the application

  - Search for the created NS using the search box at top right <e2eNsName>

  - Click on Start testing

  - Click on Accept

  - Log out using the small person icon at top right

- Log in with tester role (gv0001)

  - Select SDC from the application

  - Search for the created VNF using the search box at top right <e2eNsName>

  - Click on Approve on top right

  - Log out using the small person icon at top right

- Log in with operation role (op0001)

  - Select SDC from the application

  - Search for the created VNF using the search box at top right <e2eNsName>

  - Click on Distribute on top right

  - Click on monitor in an order to verify that the distribution was successful

    - Click on the small arrow next to the Distribution ID

    - The list should contain at least two lines staring with sdc and aai-ml

    - If the list does not contain enough elements it can be refreshed with the small icon at middle right

    - Each of the two lines should contain a green check sign next to Deployed

  - Log out using the small person icon at top right
