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
import copy

from driver.pub.config.config import REG_TO_MSB_WHEN_START, REG_TO_MSB_REG_URL, REG_TO_MSB_REG_PARAM
from django.conf.urls import include, url
urlpatterns = [
    url(r'^', include('driver.interfaces.urls')),
    url(r'^', include('driver.swagger.urls')),
]

# regist to MSB when startup
if REG_TO_MSB_WHEN_START == "true":
    import json
    from driver.pub.utils.restcall import req_by_msb
    req_by_msb(REG_TO_MSB_REG_URL, "POST", json.JSONEncoder().encode(REG_TO_MSB_REG_PARAM))
    for ms_name in ["nfvo", "vnfs", "resource"]:
        param = copy.copy(REG_TO_MSB_REG_PARAM)
        param.pop("visualRange")
        param["serviceName"] = "zte-%s" % ms_name
        param["url"] = "/v1/%s" % ms_name
        param["enable_ssl"] = "false"
        param["lb_policy"] = "ip_hash"
        req_by_msb(REG_TO_MSB_REG_URL, "POST", json.JSONEncoder().encode(param))
        param["serviceName"] = "_%s" % param["serviceName"]
        param["path"] = param["url"]
        req_by_msb(REG_TO_MSB_REG_URL, "POST", json.JSONEncoder().encode(param))
