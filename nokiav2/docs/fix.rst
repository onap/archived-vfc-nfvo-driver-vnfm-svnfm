Fix ONAP installation
=====================

This document is intended to help to fix common problems in the ONAP released software. Since the ONAP software in some
cases uses "snapshots" or "master" these steps may not be 100 percent accurate.

Amsterdam release
-----------------

- Create SDC consumer in SDC (username = SDC  password=SDC )

  - curl -X POST -i -H "Accept: application/json; charset=UTF-8" -H "Content-Type: application/json" -H "USER_ID: jh0003" http://sdc.api.simpledemo.onap.org:8080/sdc2/rest/v1/consumers/ -d '{"consumerName": "SDC", "consumerSalt": "00ae7619efccee9bf5b53d7f72c3cdf7","consumerPassword": "516e53e4b822601ef58d96abcc054709d15cb42179aa3b6302f48c6c7cf575a6"}'

- Log into the multi-service node via SSH

  - Fix VF-C

    - docker exec -it `docker ps | grep nslcm | awk '{print $1}'` /bin/bash

    - vim.tiny /service/vfc/nfvo/lcm/lcm/pub/config/config.py

      - AAI_BASE_URL = "https://10.0.1.1:8443/aai/v11"

      - SDC_BASE_URL = "https://10.0.3.1:8443/sdc/v1"

    - vim.tiny /service/vfc/nfvo/lcm/lcm/ns/vnfs/grant_vnfs.py

      - Fix according to: https://gerrit.onap.org/r/#/c/27941/3/lcm/ns/vnfs/grant_vnfs.py

    - vim.tiny /service/vfc/nfvo/lcm/lcm/ns/vnfs/notify_lcm.py

      - Fix according to: https://gerrit.onap.org/r/#/c/25447/1/lcm/ns/vnfs/notify_lcm.py

    - vim.tiny /service/vfc/nfvo/lcm/lcm/ns/ns_delete.py

      - Fix according to: https://gerrit.onap.org/r/#/c/25373/

    - exit

  - Fix catalog

    - docker exec -it `docker ps | grep catalog | awk '{print $1}'` /bin/bash

      - vim.tiny /service/vfc/nfvo/catalog/catalog/pub/config/config.py

        - SDC_BASE_URL = "https://10.0.3.1:8443"

      - vim.tiny /service/vfc/nfvo/catalog/catalog/pub/utils/toscaparser/vnfdmodel.py

        - Fix according to: https://gerrit.onap.org/r/#/c/27937/1/catalog/pub/utils/toscaparser/vnfdmodel.py

      - vim.tiny /service/vfc/nfvo/catalog/catalog/pub/utils/toscaparser/basemodel.py

        - Fix according to: https://gerrit.onap.org/r/#/c/25157/1/catalog/pub/utils/toscaparser/basemodel.py

      - exit

  - Fix multi cloud (required if cloud endpoint is over HTTPS)

    - docker exec -it `docker ps | grep ocata | awk '{print $1}'` /bin/bash

      - apt-get install vim

      - vim /opt/ocata/lib/newton/newton/requests/views/util.py

      - Change from return session.Session(auth=auth) to: session.Session(auth=auth, verify=False)

      - exit

- Specify quotas for the tenant (VF-C resource managed does not handle unlimited quotas)

Beijing release
---------------

- create SDC user (SDC:SDC)

  - curl -X POST -i -H "Accept: application/json; charset=UTF-8" -H "Content-Type: application/json" -H "USER_ID: jh0003" http://sdc.api.simpledemo.onap.org:8080/sdc2/rest/v1/consumers/ -d '{"consumerName": "SDC", "consumerSalt": "00ae7619efccee9bf5b53d7f72c3cdf7","consumerPassword": "516e53e4b822601ef58d96abcc054709d15cb42179aa3b6302f48c6c7cf575a6"}'

- Log into the multi-service node via SSH

  - Fix catalog

    - docker exec -it `docker ps | grep catalog | awk '{print $1}'` /bin/bash

      - vim.tiny /service/vfc/nfvo/catalog/catalog/pub/config/config.py

        - SDC_BASE_URL = "https://10.0.3.1:8443"

  - Fix VF-C

    - docker exec -it `docker ps | grep nslcm | awk '{print $1}'` /bin/bash

    - vim.tiny /service/vfc/nfvo/lcm/lcm/pub/config/config.py

      - AAI_BASE_URL = "https://10.0.1.1:8443/aai/v11"

      - SDC_BASE_URL = "https://10.0.3.1:8443/sdc/v1"



https://gerrit.onap.org/r/#/c/44203/
