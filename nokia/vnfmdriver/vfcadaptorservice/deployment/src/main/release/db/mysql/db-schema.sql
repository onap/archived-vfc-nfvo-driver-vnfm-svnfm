--
--
--    Copyright (c) 2016, Nokia Corporation.
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
drop database IF EXISTS vnfm_db;
delete from user where User='vnfm';
FLUSH PRIVILEGES;

/******************create new database and user***************************/
create database vnfm_db CHARACTER SET utf8;

GRANT ALL PRIVILEGES ON vnfm_db.* TO 'vnfm'@'%' IDENTIFIED BY 'vnfmpass' WITH GRANT OPTION;
GRANT ALL PRIVILEGES ON mysql.* TO 'vnfm'@'%' IDENTIFIED BY 'vnfmpass' WITH GRANT OPTION;

GRANT ALL PRIVILEGES ON vnfm_db.* TO 'vnfm'@'localhost' IDENTIFIED BY 'vnfmpass' WITH GRANT OPTION;
GRANT ALL PRIVILEGES ON mysql.* TO 'vnfm'@'localhost' IDENTIFIED BY 'vnfmpass' WITH GRANT OPTION;
FLUSH PRIVILEGES;

use vnfm_db;
set Names 'utf8';

/******************drop old table and create new***************************/

DROP TABLE IF EXISTS  vnfm_job_execution_record;
CREATE TABLE `vnfm_job_execution_record` (
  `jobId` int(11) auto_increment primary key,
  `vnfInstanceId` varchar(60) DEFAULT NULL,
  `vnfmExecutionId` varchar(60) DEFAULT NULL,
  `vnfmInterfceName` varchar(60) DEFAULT NULL,
  `status` varchar(24) DEFAULT NULL
) ENGINE=MyISAM DEFAULT CHARSET=utf8;


DROP TABLE IF EXISTS vnfm_resource_record;
CREATE TABLE `vnfm_resource_record` (
  `id` int(11) auto_increment primary key,
  `type` enum ('VDU','VL','CP','Storage') DEFAULT NULL,
  `resourceDefinitionId` varchar(60) DEFAULT NULL,
  `vdu` varchar(60) DEFAULT NULL,
  `status` varchar(24) DEFAULT NULL
) ENGINE=MyISAM DEFAULT CHARSET=utf8;