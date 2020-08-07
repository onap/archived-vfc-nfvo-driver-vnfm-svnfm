#!/bin/bash

# Configure service based on docker environment variables
python vfc/nfvo/driver/vnfm/svnfm/zte/vmanager/driver/pub/config/config.py
cat vfc/nfvo/driver/vnfm/svnfm/zte/vmanager/driver/pub/config/config.py

# microservice-specific one-time initialization
vfc/nfvo/driver/vnfm/svnfm/zte/vmanager/docker/instance_init.sh

date > init.log

# Start the microservice
vfc/nfvo/driver/vnfm/svnfm/zte/vmanager/docker/instance_run.sh
