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
#

if [ -z "$SERVICE_IP" ]; then
    export SERVICE_IP=`hostname -i`
fi
echo
echo Environment Variables:
echo "SERVICE_IP=$SERVICE_IP"

if [ -z "$MSB_ADDR" ]; then
    echo "Missing required variable MSB_ADDR: Microservices Service Bus address <ip>:<port>"
    exit 1
fi
echo "MSB_ADDR=$MSB_ADDR"
echo

# Wait for MSB initialization
echo Wait for MSB initialization
for i in {1..10}; do
    curl -sS -m 1 $MSB_ADDR > /dev/null && break
    sleep $i
done

echo

# Configure service based on docker environment variables
./instance-config.sh

function start_mysql {
    echo "start mysql in entry point ... "
    service mysql start  > myout_docker_enctrypoint.file 2>&1
    cat myout_docker_enctrypoint.file
    service mysql status > myout_docker_enctrypoint_mysql_status.file 2>&1
    cat myout_docker_enctrypoint_mysql_status.file
    sleep 5
}

# Start mysql
# su mysql -c /usr/bin/mysqld_safe &
#service mysql start
start_mysql

# Perform one-time config
if [ ! -e init.log ]; then
    # Perform workarounds due to defects in release binary
    ./instance-workaround.sh

    # Init mysql; set root password
    ./init-mysql.sh

    # microservice-specific one-time initialization
    ./instance-init.sh

    date > init.log
fi

# Start the microservice
./instance-run.sh

