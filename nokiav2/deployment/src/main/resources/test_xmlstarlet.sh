#!/bin/bash

DIRNAME=`dirname $0`
BUILD_DIR=`cd $DIRNAME/; pwd`

#If xmlstarlet is not present build will fail
xmlstarlet ed ${BUILD_DIR}/../../../pom.xml
