Integration directly through SO
===============================


Requirements on the VNF package
-------------------------------

- the VNFD must have a instantiation level named default

- each VDU must have a corresponding software image named <vduname>_image

- the VNF integrator must use the availability zones to specify the location of the VNFCs. One availability zone
will be defined for each VDU

Limitations of the current release
----------------------------------

- at least one server instance mapping for each VDU must be supplied in the VNF activation request