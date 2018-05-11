Prepare a ONAP VNF package
==========================

Automatic
---------

- Visit http://msb.api.simpledemo.onap.org/api/NokiaSVNFM/v1/convert
- Select the CBAM package to be converted into an ONAP package
- Click on upload button and the ONAP package will be downloaded


Manual
------

- the VNF must declare the externalVnfmId and onapCsarId as modifyable attribute in CBAM package. Each should have
a default value. (The concrete value will be filled out by CBAM)
- each operation must declare a jobId additional parameter in CBAM package (value will be filled out by CBAM)
- the heal operation must declare the jobId, vmName, vnfcId and action parameters in CBAM package (values will be filled out by CBAM)
- each operation (including built-in) must include the following section as the last pre_action (all JS are provided by CBAM)

.. code-block:: console

    - javascript: javascript/cbam.pre.collectConnectionPoints.js
      include:
        - javascript/cbam.collectConnectionPoints.js
      output: operation_result

- each operation must include the following section as the last post_action (all JS are provided by CBAM)

.. code-block:: console

    - javascript: javascript/cbam.post.collectConnectionPoints.js
      include:
        - javascript/cbam.collectConnectionPoints.js
      output: operation_result

- CBAM supplied JavaScrips

 - :download:`cbam.post.collectConnectionPoints.js <sample/cbam.post.collectConnectionPoints.js>`
 - :download:`cbam.pre.collectConnectionPoints.js <sample/cbam.pre.collectConnectionPoints.js>`
 - :download:`cbam.collectConnectionPoints.js <sample/cbam.collectConnectionPoints.js>`

- the ONAP package must be written so that the VDU.Compute, VDU.VirtualStorage, VnfVirtualLinkDesc, VduCpd has exactly the same name as in CBAM package
- the metadata section of the ONAP package must be the following

 - the vendor must be the same as in Nokia package vendor field
 - the vnfdVersion must be the same as in Nokia package the descriptor_version field
 - the name must be the same as in Nokia package the product_info_name field
 - the version must be the same as in Nokia package the software_version field
 - the vnfmType must be NokiaSVNFM

- the complete CBAM package must be placed in the in Artifacts/OTHER/cbam.package.zip file
