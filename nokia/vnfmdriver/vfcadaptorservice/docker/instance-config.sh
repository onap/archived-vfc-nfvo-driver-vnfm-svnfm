#!/bin/bash
#
# Copyright 2017, Nokia Corporation
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
# Config MSB address
MSB_IP=`echo $MSB_ADDR | cut -d: -f 1`
sed -i "s|127\.0\.0\.1|${MSB_IP}|" etc/conf/restclient.json
cat etc/conf/restclient.json

# Set self IP
sed -i "s|127\.0\.0\.1|$SERVICE_IP|" etc/adapterInfo/vnfmadapterinfo.json
cat etc/adapterInfo/vnfmadapterinfo.json

