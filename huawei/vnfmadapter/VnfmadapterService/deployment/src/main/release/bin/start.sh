#
# Copyright 2016 Huawei Technologies Co., Ltd.
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

#!/bin/bash
cd ..
export CATALINA_BASE=$(cd `dirname $0`; pwd)

if [ -z "$JAVA_HOME" ]
then
    echo "There is no JAVA_HOME"
    exit 1
fi

if [ -z "$CATALINA_HOME" ]
then
    echo "There is no CATALINA_HOME"
    exit 1
fi

if [ -z "$CATALINA_BASE" ]
then
    echo "There is no CATALINA_BASE"
    exit 1
fi

export CATALINA_PID="$CATALINA_BASE/bin/catalina.pid"

LOG_DIR=$CATALINA_BASE/logs
if [ ! -d "$LOG_DIR" ]; then
  mkdir $LOG_DIR
fi

$CATALINA_HOME/bin/catalina.sh start


