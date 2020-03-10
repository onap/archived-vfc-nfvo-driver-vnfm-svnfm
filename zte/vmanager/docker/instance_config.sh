#!/bin/bash

MSB_PROTO=`echo $MSB_PROTO`
MSB_IP=`echo $MSB_ADDR | cut -d: -f 1`
MSB_PORT=`echo $MSB_ADDR | cut -d: -f 2`

if [ $REG_TO_MSB_WHEN_START ]; then
    sed -i "s|REG_TO_MSB_WHEN_START = .*|REG_TO_MSB_WHEN_START = '$REG_TO_MSB_WHEN_START'|" vfc/nfvo/driver/vnfm/svnfm/zte/vmanager/driver/pub/config/config.py
fi

if [ $MSB_PROTO ]; then
    sed -i "s|MSB_SERVICE_PROTOCOL = .*|MSB_SERVICE_PROTOCOL = '$MSB_PROTO'|" vfc/nfvo/driver/vnfm/svnfm/zte/vmanager/driver/pub/config/config.py
fi

if [ $MSB_IP ]; then
    sed -i "s|MSB_SERVICE_IP = .*|MSB_SERVICE_IP = '$MSB_IP'|" vfc/nfvo/driver/vnfm/svnfm/zte/vmanager/driver/pub/config/config.py
fi

if [ $MSB_PORT ]; then
    sed -i "s|MSB_SERVICE_PORT = .*|MSB_SERVICE_PORT = '$MSB_PORT'|" vfc/nfvo/driver/vnfm/svnfm/zte/vmanager/driver/pub/config/config.py
fi

if [ $SERVICE_IP ]; then
    sed -i "s|\"ip\": \".*\"|\"ip\": \"$SERVICE_IP\"|" vfc/nfvo/driver/vnfm/svnfm/zte/vmanager/driver/pub/config/config.py
fi

cat vfc/nfvo/driver/vnfm/svnfm/zte/vmanager/driver/pub/config/config.py
