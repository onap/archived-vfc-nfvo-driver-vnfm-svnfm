#*******************************************************************************
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
#*******************************************************************************
#!/bin/bash

cd ..

if [ -z "$1" ]
then
    echo "usage: init_db.sh <db user> <db password> <db server ip> <db port> "
    exit 1
fi

if [ -z "$2" ]
then
    echo "usage: init_db.sh <db user> <db password> <db server ip> <db port> "
    exit 1
fi

if [ -z "$3" ]
then
    echo "usage: init_db.sh <db user> <db password> <db server ip> <db port> "
    exit 1
fi

if [ -z "$4" ]
then
    echo "usage: init_db.sh <db user> <db password> <db server ip> <db port> "
    exit 1
fi

echo
echo "DB-INIT [vnfmdb] : START"

mysql -u$1 -p$2 -h$3 -P$4 <$(cd `dirname $0`; pwd)/db/mysql/db-schema.sql

if [ $? != 0 ] ; then
   echo "DB-INIT [vnfmdb] : FAILED !"
   exit 1
fi

echo "DB-INIT [vnfmdb] : PASSED"
echo
echo "*************************************************************"
echo "CAUTION: Existing vnfmdb will be cleaned before"
echo "initializing the schema, so please take a back-up of it"
echo "before executing it next time."
echo "*************************************************************"
exit 0