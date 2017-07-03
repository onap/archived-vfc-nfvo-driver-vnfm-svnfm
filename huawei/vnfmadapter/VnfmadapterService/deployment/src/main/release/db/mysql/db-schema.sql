--
--
--    Copyright (c) 2016, Huawei Technologies Co., Ltd.
--
--    Licensed under the Apache License, Version 2.0 (the "License");
--    you may not use this file except in compliance with the License.
--    You may obtain a copy of the License at
--
--    http://www.apache.org/licenses/LICENSE-2.0
--
--    Unless required by applicable law or agreed to in writing, software
--    distributed under the License is distributed on an "AS IS" BASIS,
--    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
--    See the License for the specific language governing permissions and
--    limitations under the License.
--

/******************drop old database and user***************************/
use mysql;
drop database IF  EXISTS vnfmdb;
delete from user where User='vnfm';
FLUSH PRIVILEGES;

/******************create new database and user***************************/
create database vnfmdb CHARACTER SET utf8;

GRANT ALL PRIVILEGES ON vnfmdb.* TO 'vnfm'@'%' IDENTIFIED BY 'vnfm' WITH GRANT OPTION;
GRANT ALL PRIVILEGES ON mysql.* TO 'vnfm'@'%' IDENTIFIED BY 'vnfm' WITH GRANT OPTION;

GRANT ALL PRIVILEGES ON vnfmdb.* TO 'vnfm'@'localhost' IDENTIFIED BY 'vnfm' WITH GRANT OPTION;
GRANT ALL PRIVILEGES ON mysql.* TO 'vnfm'@'localhost' IDENTIFIED BY 'vnfm' WITH GRANT OPTION;
FLUSH PRIVILEGES;

use vnfmdb;
set Names 'utf8';

/******************drop old table and create new***************************/

DROP TABLE IF EXISTS VNFM;
CREATE TABLE VNFM (
	ID						VARCHAR(128)       NOT NULL,	
	VERSION                 VARCHAR(256)       NULL,
	VNFDID              	VARCHAR(256)       NULL,
	VNFPACKAGEID            VARCHAR(256)       NULL,
    CONSTRAINT VNFM PRIMARY KEY(ID)
);