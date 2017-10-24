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
# Config mysql credentials

function start_mysql {
    echo "start mysql in instance_init ... "
    systemctl start mysql.service  > myout_instance_init.file 2>&1
    cat myout_instance_init.file
    systemctl status mysql.service > myout_instance_init_mysql_status.file 2>&1
    cat myout_instance_init_mysql_status.file
    sleep 5
}

start_mysql

# Initialize MySQL schema
cd bin
./init_db.sh root rootpass 127.0.0.1 3306
