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

DIRNAME=`dirname $0`
DOCKER_BUILD_DIR=`cd $DIRNAME/; pwd`
echo "DOCKER_BUILD_DIR=${DOCKER_BUILD_DIR}"
cd ${DOCKER_BUILD_DIR}

BUILD_ARGS="--no-cache"
ORG="onap"
VERSION="1.2.2"
PROJECT="vfc"
IMAGE="nfvo/svnfm/nokia"
DOCKER_REPOSITORY="nexus3.onap.org:10003"
IMAGE_NAME="${DOCKER_REPOSITORY}/${ORG}/${PROJECT}/${IMAGE}"
TIMESTAMP=$(date +"%Y%m%dT%H%M%S")

BUILD_ARGS+=" --build-arg PKG_VERSION=${VERSION}"

if [ $HTTP_PROXY ]; then
    BUILD_ARGS+=" --build-arg HTTP_PROXY=${HTTP_PROXY}"
fi
if [ $HTTPS_PROXY ]; then
    BUILD_ARGS+=" --build-arg HTTPS_PROXY=${HTTPS_PROXY}"
fi

function build_image {
    echo "Start build docker image: ${IMAGE_NAME}"
    docker build ${BUILD_ARGS} -t ${IMAGE_NAME}:latest .
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
push_image
