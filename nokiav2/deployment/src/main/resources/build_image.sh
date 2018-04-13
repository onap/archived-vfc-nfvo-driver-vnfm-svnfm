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

if [ "a$1" != "abuildDocker" ] ; then
  echo "Skipping building Docker image"
  echo "If you would like to build the docker image by script execute $0 buildDocker" 
  echo "If you would like to build and push the docker image by script execute $0 buildDocker pushImage" 
  echo "If you would like to build the docker image by maven execute mvn package -Dexec.args=\"buildDocker\"" 
  echo "If you would like to push the docker image by maven execute mvn package -Dexec.args=\"buildDocker pushImage\""
  exit
fi 

DIRNAME=`dirname $0`
DOCKER_BUILD_DIR=`cd $DIRNAME/; pwd`
echo "----- Build directory ${DOCKER_BUILD_DIR}"
cd ${DOCKER_BUILD_DIR}

#VERSION=`xmlstarlet sel -t -v "/_:project/_:version" ../../../pom.xml | sed 's/-SNAPSHOT//g'`
VERSION=`xmlstarlet sel -t -v "/_:project/_:version" ../../../pom.xml`
echo "------ Detected version: $VERSION"

PROJECT="vfc"
IMAGE="nfvo/svnfm/nokiav2"
DOCKER_REPOSITORY="nexus3.onap.org:10003"
ORG="onap"
BUILD_ARGS="--no-cache"
# it looks like that ONAP jenkins does not support squash
#BUILD_ARGS="--no-cache --squash"

IMAGE_NAME="${DOCKER_REPOSITORY}/${ORG}/${PROJECT}/${IMAGE}"
TIMESTAMP=$(date +"%Y%m%dT%H%M%S")

if [ $HTTP_PROXY ]; then
    echo "----- Using HTTP proxy ${HTTP_PROXY}"
    BUILD_ARGS+=" --build-arg HTTP_PROXY=${HTTP_PROXY}"
fi

if [ $HTTPS_PROXY ]; then
    echo "----- Using HTTPS proxy ${HTTPS_PROXY}"
    BUILD_ARGS+=" --build-arg HTTPS_PROXY=${HTTPS_PROXY}"
fi

function build_image {
    echo "Start build docker image: ${IMAGE_NAME}"
    echo "docker build --build-arg VERSION=${VERSION} ${BUILD_ARGS} -t ${IMAGE_NAME}:latest ."
    docker build --build-arg VERSION=${VERSION} ${BUILD_ARGS} -t ${IMAGE_NAME}:latest .
}

function push_image_tag {
    TAG_NAME=$1
    echo "Start push ${TAG_NAME}"
    docker tag ${IMAGE_NAME}:latest ${TAG_NAME}
    docker push ${TAG_NAME}
}

function push_image {
    echo "Start push ${IMAGE_NAME}:latest"
    docker push ${IMAGE_NAME}:latest
    
    push_image_tag ${IMAGE_NAME}:${VERSION}-SNAPSHOT-latest
    push_image_tag ${IMAGE_NAME}:${VERSION}-STAGING-latest
    push_image_tag ${IMAGE_NAME}:${VERSION}-STAGING-${TIMESTAMP}
}

build_image

if [ "a$2" != "apushImage" ]; then
   echo "Skipping image pushing"
   echo "If you would like to push the docker image by maven execute mvn package -Dexec.args=\"buildDocker pushImage\""
else
   echo "Pushing image"
   push_image
fi
