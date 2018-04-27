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

CONFIG_LOCATION=/service/application.properties

function shutdown(){
  echo "Recieved sutdown signal"
  if [ ! -e /proc/$PID ] ; then
    echo "The process already exited $PID"
    exit
  fi
  echo "Sending shutdown signal to $PID"
  kill -15 $PID
  for i in `seq 0 20`; do
    if [ ! -e /proc/$PID ] ; then
      echo "The driver exited normally $PID"
      exit 0
    fi
    sleep 10
  done
  echo "Terminating the driver forcefully $PID"
  kill -9 $PID
}

function switchLine(){
  PATTERN=$1
  REPLACEMENT=$2
  if [ ! -z $REPLACEMENT ]; then
    sed -i "s/${PATTERN}.*/${PATTERN}/g" $CONFIG_LOCATION
    sed -i "s|${PATTERN}|${PATTERN}=${REPLACEMENT}|g" $CONFIG_LOCATION
  fi
}

function configure(){
  if [ ! -z "$CONFIGURE" ] ; then
     switchLine driverMsbExternalIp $EXTERNAL_IP
     switchLine driverVnfmExternalIp $EXTERNAL_IP
     switchLine messageBusIp $MSB_IP
     switchLine vnfmId $VNFM_ID
     switchLine ipMap $IP_MAP
  fi 
}


#during shutdown signal the ervice to stop
trap shutdown SIGINT SIGTERM 

#configure if required
configure

#start the service
cd /service

while true ; do
  echo "Starting server"
  java -jar driver.war --spring.config.location=file:/service/application.properties &>> /service/service.log &
  export PID=$!
  echo "Server process started in background with id $PID"
  while test -e /proc/$PID; do
    sleep 10
  done
  echo "Server quit (unexpected)"
done
