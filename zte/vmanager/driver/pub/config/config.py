# Copyright 2016-2017 ZTE Corporation.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#         http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
import os

# [VNFFTP]
VNF_FTP = "ftp://VMVNFM:Vnfm_1g3T@127.0.0.1:21/"

# [MSB]
MSB_BASE_URL = os.getenv("MSB_HOST", "http://127.0.0.1:80")
MSB_ENABLED = os.getenv("MSB_ENABLED", True)

# [MDC]
SERVICE_NAME = "ztevnfmdriver"
FORWARDED_FOR_FIELDS = ["HTTP_X_FORWARDED_FOR", "HTTP_X_FORWARDED_HOST",
                        "HTTP_X_FORWARDED_SERVER"]

# [register]
REG_TO_MSB_WHEN_START = os.getenv("REG_TO_MSB_WHEN_START", "false")
REG_TO_MSB_REG_URL = "/api/microservices/v1/services"
REG_TO_MSB_REG_PARAM = {
    "serviceName": "ztevnfmdriver",
    "version": "v1",
    "url": "/api/ztevnfmdriver/v1",
    "protocol": "REST",
    "visualRange": "1",
    "nodes": [{
        "ip": "127.0.0.1",
        "port": "8410",
        "ttl": 0
    }]
}
