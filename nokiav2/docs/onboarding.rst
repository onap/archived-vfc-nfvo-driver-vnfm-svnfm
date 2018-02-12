Onboard NS
==========

Prepare environment
-------------------
- Create customer in A&AI

 - tool: Postman
 - method: PUT
 - URL: https://aai.api.simpledemo.onap.org:8443/aai/v11/business/customers/customer/123456
 - Headers

  - basic auth AAI:AAI
  - X-FromAppId : any
  - Content-type: application/json
  - Accept: application/json

 - body: :download:`aai.create.customer.request.json <sample/aai.create.customer.request.json>`

  - Edit tenant id, cloud owner, cloud region

- Log into ONAP portal with designer role (cs0008)

 - Create License model

  - ONBOARD / Create new VLP

   - name = select a name easy to remember

  - Entitlement pool / add new Entitlement pool

   - name = any

  - License key group / add new licencse key group

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

 - Create VF

  - home / import / import vsp (select VSP from list)

   - set General / Vendor model number to CBAM package VNFD ID

  - set sVNFM property assignment / inputs / nf_type  set NokiaSVNFM
  - commit
  - submit for testing

 - Test VF

  - Log in as tester role
  - Select VF
  - Start testing button
  - Accept testing

 - Create Service

  - Log in as designer role
  - home / add / add service
  - name = select a name easy to remember
  - project code = 123456
  - Check in & check out (required to save a safe point to restore to if something goes wrong)

 - Add created VF (Composition)

  - drag icon to main picture (be patient only drag once, if multiple icons appear restart procedure )
  - Check in
  - Open service again and verify that the VF is part of the service under composition

 - Submit for testing
 - Test Service

  - Log in as tester role (jm0007)
  - start testing & accept

 - Approve service

  - Log in as governence user (gv0001)
  - Select service and press approve

 - Distribute the service

  - Log in as operations personen (op0001)
  - Select service and push distribute
  - Click on monitor (verify that the state of the service is distributed)
