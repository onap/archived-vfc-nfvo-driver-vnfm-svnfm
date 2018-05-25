Onboard NS
==========

The following section describes how to create an E2E service.

Create licensing model
----------------------


- Log into ONAP portal with designer role (cs0008)

 - Create License model

  - SDC / ONBOARD / Create new VLM

   - name = select a name easy to remember

  - Entitlement pool / add new Entitlement pool

   - name = any

  - License key group / add new license key group

   - name = any

   - type = universal

  - Feature groups / add feature group

   - name = any

   - part number = 123456

   - manufacturer reference number = 123456

   - entitlement pool (add any with arrow button)

   - license key group (add any with arrow button)

   - save

  - License agreements / Add license agreement

   - name = any

   - license term unlimited

   - feature groups (add any with arrow button)

  - Check in (lock icon at top)

  - Submit (tick icon at top)

On-board VNF
------------

 - On-board / Create new VSP (vendor software package)

  - Create VSP

   - name = select a name easy to remember

   - vendor = name of the license model

   - category = Database (IMPORTANT NOT TO CHANGE THIS (linked to the global customer))

   - onboarding procedure = network package

   - description = any

  - Upload CSAR

   - overview / software product attachments / select file

  - Select licence

   - overview / software product details / license agreement

   - licenses

   - set license version, license agreement, feature groups

   - click on save icon at top

   - commit & submit using icons at top

 - Create the ETSI configuration JSON

   - TODO

 - Create VNF

  - home / import / import vsp (select VSP from list)

   - set General / Vendor model number to CBAM package VNFD ID

  - set the VNFM of the VNF by setting property assignment / inputs / nf_type to NokiaSVNFM

  - set configuration of the VNF by setting property assignment / inputs / etsi_config to the created JSON in previous step

  - commit

  - submit for testing

 - Test VF

  - Log in as tester role

  - Select VF

  - Start testing button

  - Accept testing

Design network service
----------------------

 - Create Service

  - Log in with designer role (cs0008)

  - home / add / add service

  - name = select a name easy to remember

  - project code = 123456

  - service type: Network Service (if missing log in as demo user and in SDC create the service type

  - Check in & check out (required to save a safe point to restore to if something goes wrong)

 - Add created VF (Composition)

  - drag icon to main picture (be patient only drag once, if multiple icons appear restart procedure )

  - Check in

  - Open service again and verify that the VF is part of the service under composition

 - Submit for testing

 - Test Service

  - Log in with tester role (jm0007)

  - start testing & accept

 - Approve service

  - Log in with governance role (gv0001)

  - Select service and press approve

 - Distribute the service

  - Log in with operations role (op0001)

  - Select service and push distribute

  - Click on monitor (verify that the state of the service is distributed)


Design a VF for the E2E service
-------------------------------

This step is only required if the UUI is planed to be used to manage the E2E service

- Create VF

  - Log in with designer role (cs0008)

  - home / add / add VF

  - name = select a name easy to remember, but the name MUST contain the vIMS string constant (ex. my_vIMS_something).

  - project code = 123456

  - service type: Network Service (if missing log in as demo user and in SDC create the service type

- Determine the UUID of the created NS in previous step

  - URL https://sdc.api.simpledemo.onap.org:8443/sdc/v1/catalog/services

  - Method: GET

  - Headers:

    - Content-type: application/json

    - X-ECOMP-InstanceID: VFC

  - Basic auth: SDC:SDC

  - Search for the created service by name and note the uuid and invariantUUID fields

- Add NS (Composition)

  - Select NSD from left panel and drag it into the main view

  - Go back to "Properties Assingment"

  - Set {"get_input":"nsd0_providing_service_invariant_uuid"} for the providing_service_invariant_uuid property

  - Set {"get_input":"nsd0_providing_service_uuid"} for the providing_service_uuid

  - Click on each of the two specified properties and click on the declare button

  - In the inputs section set the uuid and invariant uuid properties to the values from the previous step

  - click on save

  - Check in

 - Submit for testing

 - Test Service

  - Log in with tester role (jm0007)

  - start testing & accept

 - Approve service

  - Log in with governance role (gv0001)

  - Select service and press approve

 - Distribute the service

  - Log in with operations role (op0001)

  - Select service and push distribute

  - Click on monitor (verify that the state of the service is distributed)

Design a E2E service
--------------------

This step is only required if the UUI is planed to be used to manage the E2E service

- Create E2E service

  - Log in with designer role (cs0008)

  - home / add / add service

  - name = select a name easy to remember

  - project code = 123456

  - service type: E2E service (if missing log in as demo user and in SDC create the service type)

- TODO

