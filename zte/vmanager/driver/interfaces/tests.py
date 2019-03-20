# Copyright 2016-2017 ZTE Corporation.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#         http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

import json
import mock
from django.test import Client
from django.test import TestCase
from rest_framework import status
from driver.pub.utils import restcall


class InterfacesTest(TestCase):
    def setUp(self):
        self.client = Client()

    def tearDown(self):
        pass

    @mock.patch.object(restcall, 'call_req')
    def test_instantiate_vnf_001(self, mock_call_req):
        vnfm_info = {
            "userName": "admin",
            "vendor": "ZTE",
            "name": "ZTE_VNFM_237_62",
            "vimId": "516cee95-e8ca-4d26-9268-38e343c2e31e",
            "url": "http://192.168.237.165:2324",
            "certificateUrl": "",
            "version": "V1.0",
            "vnfmId": "b0797c9b-3da9-459c-b25c-3813e9d8fd70",
            "password": "admin",
            "type": "ztevnfmdriver",
            "createTime": "2016-10-31 11:08:39",
            "description": ""
        }
        vnfd_info = {
            "vnfInstanceInfo": [
                {
                    "vnfInstanceId": "59b79a9f-9e37-4f6c-acaf-5f41d9cb3f76",
                    "vnfInstanceName": "VFW_59b79a9f-9e37-4f6c-acaf-5f41d9cb3f76"
                },
                {
                    "vnfInstanceId": "6c5e4bd3-e8a6-42d8-a7a4-53a8ef74c6ac",
                    "vnfInstanceName": "VFW_6c5e4bd3-e8a6-42d8-a7a4-53a8ef74c6ac"
                },
                {
                    "vnfInstanceId": "930de5c9-8043-41df-ace8-ede2262a3713",
                    "vnfInstanceName": "VFW_930de5c9-8043-41df-ace8-ede2262a3713"
                },
                {
                    "vnfInstanceId": "c677a305-a7bd-4baf-9eee-c383c588bb3c",
                    "vnfInstanceName": "VFW_c677a305-a7bd-4baf-9eee-c383c588bb3c"
                },
                {
                    "vnfInstanceId": "e708e5c5-bdf4-436a-b928-826887806d82",
                    "vnfInstanceName": "VFW_e708e5c5-bdf4-436a-b928-826887806d82"
                }
            ],
            "csarId": "d852e1be-0aac-48f1-b1a4-cd825f6cdf9a",
            "imageInfo": [
                {
                    "status": "Enable",
                    "index": "0",
                    "vimId": "516cee95-e8ca-4d26-9268-38e343c2e31e",
                    "fileName": "VFW_IMAGE_VCPE_ZTE",
                    "vimUser": "admin",
                    "imageId": "d2b73154-0414-466a-a1e6-51b9461b753a",
                    "tenant": "admin"
                }
            ],
            "packageInfo": {
                "usageState": "NotInUse",
                "onBoardState": "onBoarded",
                "name": "VFW",
                "format": "yaml",
                "provider": "ZTE",
                "vnfdProvider": "zte",
                "vnfdId": "vcpe_vfw_zte_1_0",
                "deletionPending": False,
                "version": "v1.0",
                "vnfVersion": "1.0",
                "vnfdVersion": "1.0.0",
                "processState": "normal",
                "modifyTime": "2016-10-3116: 21: 32",
                "downloadUri": "http: //192.168.233.226: 80/",
                "operationalState": "Disabled",
                "createTime": "2016-10-3116: 21: 11",
                "size": "12.1MB"
            }
        }
        packageInfo = {
            "size": "12.1 MB",
            "usageState": "NotInUse",
            "onBoardState": "onBoarded",
            "name": "VFW",
            "format": "yaml",
            "packageInfo": {
                "vnfdModel": json.dumps({"metadata": {"name": "ZTE-MME-FIX-VL"}})
            },
            "modifyTime": "2016-10-31 16:21:32",
            "vnfdProvider": "zte",
            "vnfdId": "vcpe_vfw_zte_1_0",
            "deletionPending": False,
            "version": "v1.0",
            "vnfVersion": "1.0",
            "vnfdVersion": "1.0.0",
            "processState": "normal",
            "provider": "ZTE",
            "operationalState": "Disabled",
            "createTime": "2016-10-31 16:21:11",
            "downloadUri": "http://192.168.233.226:80/files/catalog-http/NFAR/ZTE/VFW/v1.0/VFW.csar"
        }

        inst_response = {
            "vnfInstanceId": "8",
            "jobid": "NF-CREATE-8-b384535c-9f45-11e6-8749-fa163e91c2f9"
        }
        ret = [0, json.JSONEncoder().encode(inst_response), '200']
        r1 = [0, json.JSONEncoder().encode(vnfm_info), "200"]
        r2 = [0, json.JSONEncoder().encode(vnfd_info), "200"]
        r3 = [0, json.JSONEncoder().encode(packageInfo), "200"]
        mock_call_req.side_effect = [r1, r2, r3, ret]

        req_data = {
            'vnfInstanceName': 'VFW_f88c0cb7-512a-44c4-bd09-891663f19367',
            'vnfPackageId': 'd852e1be-0aac-48f1-b1a4-cd825f6cdf9a',
            'vnfDescriptorId': 'vcpe_vfw_zte_1_0',
            'additionalParam': {
                'sdncontroller': 'e4d637f1-a4ec-4c59-8b20-4e8ab34daba9',
                'NatIpRange': '192.167.0.10-192.168.0.20',
                'm6000_mng_ip': '192.168.11.11',
                'externalPluginManageNetworkName': 'plugin_net_2014',
                'location': '516cee95-e8ca-4d26-9268-38e343c2e31e',
                'externalManageNetworkName': 'mng_net_2017',
                'sfc_data_network': 'sfc_data_net_2016',
                'externalDataNetworkName': 'Flow_out_net',
                'inputs': {}
            }
        }

        response = self.client.post(
            "/api/ztevnfmdriver/v1/ztevnfmid/vnfs",
            data=json.dumps(req_data),
            content_type="application/json")
        self.assertEqual(status.HTTP_200_OK, response.status_code)
        expect_resp_data = {
            "jobId": "NF-CREATE-8-b384535c-9f45-11e6-8749-fa163e91c2f9",
            "vnfInstanceId": "8"}
        self.assertEqual(expect_resp_data, response.data)

    @mock.patch.object(restcall, 'call_req')
    def test_terminate_vnf__002(self, mock_call_req):
        r1 = [0, json.JSONEncoder().encode({
            "vnfmId": "19ecbb3a-3242-4fa3-9926-8dfb7ddc29ee",
            "name": "g_vnfm",
            "type": "vnfm",
            "vimId": "",
            "vendor": "ZTE",
            "version": "v1.0",
            "description": "vnfm",
            "certificateUrl": "",
            "url": "http://10.74.44.11",
            "userName": "admin",
            "password": "admin",
            "createTime": "2016-07-06 15:33:18"}), "200"]

        r2 = [0, json.JSONEncoder().encode({"vnfInstanceId": "1", "jobId": "1"}), "200"]
        mock_call_req.side_effect = [r1, r2]
        req_data = {
            "terminationType": "GRACEFUL",
            "gracefulTerminationTimeout": 120
        }
        response = self.client.post(
            "/api/ztevnfmdriver/v1/ztevnfmid/vnfs/1/terminate", data=req_data)

        self.assertEqual(status.HTTP_200_OK, response.status_code)
        expect_resp_data = {"jobId": "1", "vnfInstanceId": "1"}
        self.assertEqual(expect_resp_data, response.data)

    @mock.patch.object(restcall, 'call_req')
    def test_query_vnf_003(self, mock_call_req):
        r1 = [0, json.JSONEncoder().encode({
            "vnfmId": "19ecbb3a-3242-4fa3-9926-8dfb7ddc29ee",
            "name": "g_vnfm",
            "type": "vnfm",
            "vimId": "",
            "vendor": "ZTE",
            "version": "v1.0",
            "description": "vnfm",
            "certificateUrl": "",
            "url": "http://10.74.44.11",
            "userName": "admin",
            "password": "admin",
            "createTime": "2016-07-06 15:33:18"}), "200"]

        r2 = [0, json.JSONEncoder().encode({"vnfinstancestatus": "1"}), "200"]
        mock_call_req.side_effect = [r1, r2]

        response = self.client.get(
            "/api/ztevnfmdriver/v1/ztevnfmid/vnfs/vbras_innstance_id")

        self.assertEqual(status.HTTP_200_OK, response.status_code)

        expect_resp_data = {"vnfInfo": {"vnfStatus": "1"}}
        self.assertEqual(expect_resp_data, response.data)

    @mock.patch.object(restcall, 'call_req')
    def test_operation_status_004(self, mock_call_req):
        vnfm_info = {
            "userName": "admin",
            "vendor": "ZTE",
            "name": "ZTE_VNFM_237_62",
            "vimId": "516cee95-e8ca-4d26-9268-38e343c2e31e",
            "url": "http://192.168.237.165:2324",
            "certificateUrl": "",
            "version": "V1.0",
            "vnfmId": "b0797c9b-3da9-459c-b25c-3813e9d8fd70",
            "password": "admin",
            "type": "ztevnfmdriver",
            "createTime": "2016-10-31 11:08:39",
            "description": ""
        }
        resp_body = {
            "responseDescriptor": {
                "status": "processing",
                "responseHistoryList": [
                    {
                        "status": "error",
                        "progress": 255,
                        "errorCode": "",
                        "responseId": 20,
                        "statusDescription": "'JsonParser' object has no attribute 'parser_info'"}],
                "responseId": 21,
                "errorCode": "",
                "progress": 40,
                "statusDescription": "Create nf apply resource failed"},
            "jobId": "NF-CREATE-11-ec6c2f2a-9f48-11e6-9405-fa163e91c2f9"}
        r1 = [0, json.JSONEncoder().encode(vnfm_info), '200']
        r2 = [0, json.JSONEncoder().encode(resp_body), '200']
        mock_call_req.side_effect = [r1, r2]
        response = self.client.get(
            "/api/ztevnfmdriver/v1/{vnfmid}/jobs/{jobid}?responseId={responseId}".format(
                vnfmid=vnfm_info["vnfmId"],
                jobid=resp_body["jobId"],
                responseId=resp_body["responseDescriptor"]["responseId"]))

        self.assertEqual(status.HTTP_200_OK, response.status_code)

        self.assertDictEqual(resp_body, response.data)

    @mock.patch.object(restcall, 'call_req')
    def test_grantvnf_005(self, mock_call_req):
        grant_data = {
            "vim": {
                "accessinfo": {
                    "tenant": "admin"
                },
                "vimid": "516cee95-e8ca-4d26-9268-38e343c2e31e"
            }
        }
        ret = [0, json.JSONEncoder().encode(grant_data), '201']

        req_data = {
            "vnfmid": "13232222",
            "nfvoid": "03212234",
            "vimid": "12345678",
            "exvimidlist ": [
                "exvimid"
            ],
            "tenant": " tenant1",
            "vnfistanceid": "1234",
            "operationright": "0",
            "vmlist": [
                {
                    "VMNumber": 1,
                    "VMFlavor": "VDU_S_CDB_51",
                    "vimid": "",
                    "tenant": ""
                }, {
                    "VMNumber": 1,
                    "VMFlavor": "VDU_M_SLB_42",
                    "vimid": "",
                    "tenant": ""
                }
            ]
        }

        mock_call_req.return_value = ret
        response = self.client.put(
            "/api/ztevnfmdriver/v1/resource/grant",
            data=json.dumps(req_data),
            content_type='application/json')

        self.assertEqual(status.HTTP_201_CREATED, response.status_code)

        expect_resp_data = {
            "vimid": "516cee95-e8ca-4d26-9268-38e343c2e31e",
            "tenant": "admin"}
        self.assertDictEqual(expect_resp_data, response.data)

    @mock.patch.object(restcall, 'call_req')
    def test_notify_006(self, mock_call_req):
        r1 = [0, json.JSONEncoder().encode(
            {"vim":
                {
                    "vimInfoId": "111111",
                    "vimId": "12345678",
                    "interfaceInfo": {
                        "vimType": "vnf",
                        "apiVersion": "v1",
                        "protocolType": "None"},
                    "accessInfo": {
                        "tenant": "tenant1",
                        "username": "admin",
                        "password": "password"},
                    "interfaceEndpoint": "http://127.0.0.1/api/v1"},
                "zone": "",
                "addResource": {
                    "resourceDefinitionId": "xxxxx",
                    "vimId": "12345678",
                    "zoneId": "000"},
                "removeResource": "",
                "vimAssets": {
                    "computeResourceFlavour": {
                        "vimId": "12345678",
                        "vduId": "sdfasdf",
                        "vimFlavourId": "12"},
                    "softwareImage": {
                        "vimId": "12345678",
                        "imageName": "AAA",
                        "vimImageId": ""}},
                "additionalParam": ""
             }), "200"]

        mock_call_req.side_effect = [r1]
        response = self.client.post(
            "/api/ztevnfmdriver/v1/vnfs/lifecyclechangesnotification",
            data=json.dumps(notify_req_data),
            content_type='application/json')

        self.assertEqual(status.HTTP_200_OK, response.status_code)

        expect_resp_data = None
        self.assertEqual(expect_resp_data, response.data)

    @mock.patch.object(restcall, 'call_req')
    def test_scale(self, mock_call_req):
        job_info = {
            "jobId": "801",
            "vnfInstanceId": "101"
        }
        vnfm_info = {
            "userName": "admin",
            "vendor": "ZTE",
            "name": "ZTE_VNFM_237_62",
            "vimId": "516cee95-e8ca-4d26-9268-38e343c2e31e",
            "url": "http://192.168.237.165:2324",
            "certificateUrl": "",
            "version": "V1.0",
            "vnfmId": "b0797c9b-3da9-459c-b25c-3813e9d8fd70",
            "password": "admin",
            "type": "ztevnfmdriver",
            "createTime": "2016-10-31 11:08:39",
            "description": ""
        }

        ret = [0, json.JSONEncoder().encode(job_info), "202"]
        ret_vnfm = [0, json.JSONEncoder().encode(vnfm_info), "200"]
        mock_call_req.side_effect = [ret_vnfm, ret]

        vnfd_info = {
            "vnf_flavours": [
                {
                    "flavour_id": "flavour1",
                    "description": "",
                    "vdu_profiles": [
                        {
                            "vdu_id": "vdu1Id",
                            "instances_minimum_number": 1,
                            "instances_maximum_number": 4,
                            "local_affinity_antiaffinity_rule": [
                                {
                                    "affinity_antiaffinity": "affinity",
                                    "scope": "node",

                                }
                            ]
                        }
                    ],
                    "scaling_aspects": [
                        {
                            "id": "demo_aspect",
                            "name": "demo_aspect",
                            "description": "demo_aspect",
                            "associated_group": "elementGroup1",
                            "max_scale_level": 5
                        }
                    ]
                }
            ],
            "element_groups": [
                {
                    "group_id": "elementGroup1",
                    "description": "",
                    "properties": {
                        "name": "elementGroup1"
                    },
                    "members": [
                        "gsu_vm",
                        "pfu_vm"
                    ]
                }
            ]
        }

        scale_vnf_data = {
            "type": "SCALE_OUT",
            "aspectId": "demo_aspect",
            "numberOfSteps": "3",
            "additionalParam": {
                "vnfdModel": vnfd_info
            }
        }

        response = self.client.post(
            "/api/ztevnfmdriver/v1/100/vnfs/101/scale",
            data=json.dumps(scale_vnf_data),
            content_type='application/json')
        self.assertEqual(status.HTTP_202_ACCEPTED, response.status_code)
        self.assertDictEqual(job_info, response.data)

    @mock.patch.object(restcall, 'call_req')
    def test_heal(self, mock_call_req):
        job_info = {
            "jobId": "12234455",
            "vnfInstanceId": "10144445666"
        }
        vnfm_info = {
            "userName": "admin",
            "vendor": "ZTE",
            "name": "ZTE_VNFM_237_62",
            "vimId": "516cee95-e8ca-4d26-9268-38e343c2e31e",
            "url": "http://192.168.237.165:2324",
            "certificateUrl": "",
            "version": "V1.0",
            "vnfmId": "b0797c9b-3da9-459c-b25c-3813e9d8fd70",
            "password": "admin",
            "type": "ztevnfmdriver",
            "createTime": "2016-10-31 11:08:39",
            "description": ""
        }

        ret = [0, json.JSONEncoder().encode(job_info), "202"]
        ret_vnfm = [0, json.JSONEncoder().encode(vnfm_info), "200"]
        mock_call_req.side_effect = [ret_vnfm, ret]

        heal_vnf_data = {
            'action': 'vmReset',
            'affectedvm': [{
                'flavour': {

                },
                'extention': '',
                'vmid': '804cca71-9ae9-4511-8e30-d1387718caff',
                'changtype': 'changed',
                'vduid': 'vdu_100',
                'vmname': 'ZTE_SSS_111_PP_2_L'
            }],
            'lifecycleoperation': 'operate',
            'isgrace': 'force'
        }

        response = self.client.post("/api/ztevnfmdriver/v1/200/vnfs/201/heal", data=json.dumps(heal_vnf_data),
                                    content_type='application/json')

        self.assertEqual(status.HTTP_202_ACCEPTED, response.status_code)
        self.assertDictEqual(job_info, response.data)


notify_req_data = {"vnfinstanceid": "1",
                   "nfvoid": "3",
                   "extension": {"openo_notification": {"status": "finished",
                                                        "affectedService": [],
                                                        "affectedVnfc": [{"changeType": "added",
                                                                          "computeResource": {"resourceId": "e8ccc55a-3ebb-4e46-8260-dc4a1646ef4f",
                                                                                              "tenant": "ZTE_ONAP_PRO",
                                                                                              "vimId": "vmware_vio",
                                                                                              "resourceName": "ZTE_xGW_39_CDB_1"},
                                                                          "storageResource": [],
                                                                          "vnfcInstanceId": "17502154-c5bf-11e7-904d-fa163eee1ffe",
                                                                          "vduType": "CDB",
                                                                          "vduId": "VDU_S_CDB_51"},
                                                                         {"changeType": "added",
                                                                          "computeResource": {"resourceId": "a9dd6a73-76ee-4d07-9554-08f14c17261f",
                                                                                              "tenant": "ZTE_ONAP_PRO",
                                                                                              "vimId": "vmware_vio",
                                                                                              "resourceName": "ZTE_xGW_39_SLB_1"},
                                                                          "storageResource": [],
                                                                          "vnfcInstanceId": "1750d540-c5bf-11e7-904d-fa163eee1ffe",
                                                                          "vduType": "SLB",
                                                                          "vduId": "VDU_M_SLB_42"}],
                                                        "nfvoInstanceId": "3",
                                                        "affectedVirtualLink": [{"changeType": "added",
                                                                                 "virtualLinkInstanceId": "1753b60c-c5bf-11e7-904d-fa163eee1ffe",
                                                                                 "networkResource": {"resourceId": "c55e0788-3683-48a1-b88a-a0cb5e05bd44",
                                                                                                     "tenant": None,
                                                                                                     "vimId": "vmware_vio",
                                                                                                     "resourceName": "ZTE_VGW_MGT_NET39"},
                                                                                 "virtualLinkDescId": "ZTE_VGW_MGT_NET39_virtualLink",
                                                                                 "tenant": "ZTE_ONAP_PRO",
                                                                                 "subnetworkResource": {"resourceId": "33c8a03d-00c9-4c57-a348-26dae462b473",
                                                                                                        "tenant": None,
                                                                                                        "vimId": "vmware_vio",
                                                                                                        "resourceName": "ZTE_VGW_MGT_NET39_s"}},
                                                                                {"changeType": "added",
                                                                                 "virtualLinkInstanceId": "175472a4-c5bf-11e7-904d-fa163eee1ffe",
                                                                                 "networkResource": {"resourceId": "2d22b6e4-340b-45a8-8757-5206aa056b92",
                                                                                                     "tenant": None,
                                                                                                     "vimId": "vmware_vio",
                                                                                                     "resourceName": "ZTE_VGW_SERVICE_NET39"},
                                                                                 "virtualLinkDescId": "ZTE_VGW_SERVICE_NET39_virtualLink",
                                                                                 "tenant": "ZTE_ONAP_PRO",
                                                                                 "subnetworkResource": {"resourceId": "2ea2acc0-a4ed-44f8-9d31-9cdc9e3ebe62",
                                                                                                        "tenant": None,
                                                                                                        "vimId": "vmware_vio",
                                                                                                        "resourceName": "ZTE_VGW_SERVICE_NET39_s"}}],
                                                        "affectedVirtualStorage": [],
                                                        "jobId": "",
                                                        "affectedcapacity": {"vcp": "72",
                                                                             "vm": "9",
                                                                             "localStorage": "0",
                                                                             "sharedStorage": "288",
                                                                             "vMemory": "233472",
                                                                             "port": "27"},
                                                        "additionalParam": {"vmList": [{"vmName": "ZTE_xGW_39_CDB_1",
                                                                                        "vduId": "VDU_S_CDB_51"},
                                                                                       {"vmName": "ZTE_xGW_39_CDB_2",
                                                                                        "vduId": "VDU_S_CDB_51"},
                                                                                       {"vmName": "ZTE_xGW_39_SLB_1",
                                                                                        "vduId": "VDU_M_SLB_42"},
                                                                                       {"vmName": "ZTE_xGW_39_SLB_2",
                                                                                        "vduId": "VDU_M_SLB_42"},
                                                                                       {"vmName": "ZTE_xGW_39_GSU_1",
                                                                                        "vduId": "VDU_M_GSU_22"},
                                                                                       {"vmName": "ZTE_xGW_39_GSU_2",
                                                                                        "vduId": "VDU_M_GSU_22"},
                                                                                       {"vmName": "ZTE_xGW_39_MPU_1",
                                                                                        "vduId": "VDU_M_MPU_12"},
                                                                                       {"vmName": "ZTE_xGW_39_PFU_1",
                                                                                        "vduId": "VDU_M_PFU_32"},
                                                                                       {"vmName": "ZTE_xGW_39_PFU_2",
                                                                                        "vduId": "VDU_M_PFU_32"}]},
                                                        "nfInstanceId": "1",
                                                        "affectedCp": [{"changeType": "added",
                                                                        "cPInstanceId": "175767d4-c5bf-11e7-904d-fa163eee1ffe",
                                                                        "ownertype": 3,
                                                                        "cpdId": "CP_NO_0_CDB_ZTE_VGW_MGT_NET39",
                                                                        "portResource": {"resourceId": "3296b6d8-ebca-4d33-98f4-68d1bc63a3d0",
                                                                                         "tenant": "ZTE_ONAP_PRO",
                                                                                         "vimId": "vmware_vio",
                                                                                         "resourceName": "CP_ZTE_xGW_39_CDB_1_ZTE_VGW_MGT_NET39_su1"},
                                                                        "cpInstanceName": "CP_ZTE_xGW_39_CDB_1_ZTE_VGW_MGT_NET39_su1",
                                                                        "ownerid": "17502154-c5bf-11e7-904d-fa163eee1ffe",
                                                                        "virtualLinkInstanceId": "1753b60c-c5bf-11e7-904d-fa163eee1ffe"},
                                                                       {"changeType": "added",
                                                                        "cPInstanceId": "1758181e-c5bf-11e7-904d-fa163eee1ffe",
                                                                        "ownertype": 3,
                                                                        "cpdId": "CP_NO_1_CDB_ZTE_VGW_SERVICE_NET39",
                                                                        "portResource": {"resourceId": "5e277a18-94de-469a-a336-2c01ab46387e",
                                                                                         "tenant": "ZTE_ONAP_PRO",
                                                                                         "vimId": "vmware_vio",
                                                                                         "resourceName": "CP_ZTE_xGW_39_CDB_1_ZTE_VGW_SERVICE_NET39_su2"},
                                                                        "cpInstanceName": "CP_ZTE_xGW_39_CDB_1_ZTE_VGW_SERVICE_NET39_su2",
                                                                        "ownerid": "17502154-c5bf-11e7-904d-fa163eee1ffe",
                                                                        "virtualLinkInstanceId": "175472a4-c5bf-11e7-904d-fa163eee1ffe"}],
                                                        "vnfdmodule": {"volume_storages": [],
                                                                       "inputs": {},
                                                                       "vdus": [{"volume_storages": [],
                                                                                 "description": "CDB",
                                                                                 "vdu_id": "VDU_S_CDB_51",
                                                                                 "local_storages": ["local_disk_root_10GB",
                                                                                                    "local_disk_ephemeral_14GB"],
                                                                                 "nfv_compute": {"flavor_extra_specs": {"hw:cpu_policy": "dedicated",
                                                                                                                        "hw:mem_page_size": "large",
                                                                                                                        "hw:numa_nodes": 1,
                                                                                                                        "hw:cpu_max_sockets": 1},
                                                                                                 "mem_size": 8192,
                                                                                                 "num_cpus": 2},
                                                                                 "artifacts": [],
                                                                                 "dependencies": [],
                                                                                 "vls": ["ZTE_VGW_MGT_NET39_virtualLink",
                                                                                         "ZTE_VGW_SERVICE_NET39_virtualLink"],
                                                                                 "image_file": "image_51",
                                                                                 "cps": ["CP_ZTE_xGW_39_CDB_1_ZTE_VGW_MGT_NET39_su1",
                                                                                         "CP_ZTE_xGW_39_CDB_1_ZTE_VGW_SERVICE_NET39_su2"],
                                                                                 "properties": {"key_vd": True,
                                                                                                "support_scaling": True,
                                                                                                "vdu_type": "CDB",
                                                                                                "name": "ZTE_xGW_39_CDB_1",
                                                                                                "storage_policy": "Share_Service",
                                                                                                "inject_network_address": True,
                                                                                                "is_predefined": False,
                                                                                                "location_info": {"vimid": "",
                                                                                                                  "availability_zone": "",
                                                                                                                  "vdc": "OG_OrganizationDC",
                                                                                                                  "host": "",
                                                                                                                  "tenant": "",
                                                                                                                  "vapp": "xgw"},
                                                                                                "use_shared_vm": False,
                                                                                                "inject_data_list": [],
                                                                                                "allow_scale_updown": True,
                                                                                                "action": "ADD",
                                                                                                "watchdog": {},
                                                                                                "template_id": 51,
                                                                                                "manual_scale_select_vim": False,
                                                                                                "config_drive": True}},
                                                                                {"volume_storages": [],
                                                                                 "description": "SLB",
                                                                                 "vdu_id": "VDU_M_SLB_42",
                                                                                 "local_storages": ["local_disk_root_10GB",
                                                                                                    "local_disk_ephemeral_14GB"],
                                                                                 "nfv_compute": {"flavor_extra_specs": {"hw:cpu_policy": "dedicated",
                                                                                                                        "hw:mem_page_size": "large",
                                                                                                                        "hw:numa_nodes": 1,
                                                                                                                        "hw:cpu_max_sockets": 1},
                                                                                                 "mem_size": 24576,
                                                                                                 "num_cpus": 8},
                                                                                 "artifacts": [],
                                                                                 "dependencies": [],
                                                                                 "vls": ["ZTE_VGW_MGT_NET39_virtualLink",
                                                                                         "ZTE_VGW_SERVICE_NET39_virtualLink",
                                                                                         "ZTE_NET39_virtualLink",
                                                                                         "ZTE_VGW_GTP_NET39_virtualLink"],
                                                                                 "image_file": "image_51",
                                                                                 "cps": ["CP_ZTE_xGW_39_SLB_1_ZTE_VGW_MGT_NET39_su1",
                                                                                         "CP_ZTE_xGW_39_SLB_1_ZTE_VGW_SERVICE_NET39_su2",
                                                                                         "CP_ZTE_xGW_39_SLB_1_zte-net-subnet393",
                                                                                         "CP_ZTE_xGW_39_SLB_1_ZTE_VGW_GTP_NET39_su4"],
                                                                                 "properties": {"key_vd": True,
                                                                                                "support_scaling": False,
                                                                                                "vdu_type": "SLB",
                                                                                                "name": "ZTE_xGW_39_SLB_1",
                                                                                                "storage_policy": "Share_Service",
                                                                                                "inject_network_address": True,
                                                                                                "is_predefined": False,
                                                                                                "location_info": {"vimid": "",
                                                                                                                  "availability_zone": "",
                                                                                                                  "vdc": "OG_OrganizationDC",
                                                                                                                  "host": "",
                                                                                                                  "tenant": "",
                                                                                                                  "vapp": "xgw"},
                                                                                                "use_shared_vm": False,
                                                                                                "inject_data_list": [],
                                                                                                "allow_scale_updown": True,
                                                                                                "action": "ADD",
                                                                                                "watchdog": {},
                                                                                                "template_id": 42,
                                                                                                "manual_scale_select_vim": False,
                                                                                                "config_drive": True}}],
                                                                       "vcloud": [],
                                                                       "extvirtuallink": "",
                                                                       "server_groups": [],
                                                                       "image_files": [{"properties": {"vendor": "zte",
                                                                                                       "name": "ZXUN_xGW_CGSL_QCOW2_OP_V6.17.10.B17.ova",
                                                                                                       "image_extra_specs": {},
                                                                                                       "disk_format": "vmdk",
                                                                                                       "file_url": "SoftwareImages/ZXUN-xGW-CGSL-QCOW2-V6.17.10.B17-image.tar.gz",
                                                                                                       "container_type": "bare",
                                                                                                       "version": "V6.17.10.B17.ova"},
                                                                                        "image_file_id": "image_51",
                                                                                        "description": "xgw image file"}],
                                                                       "routers": [],
                                                                       "local_storages": [{"local_storage_id": "local_disk_ephemeral_38GB",
                                                                                           "description": "local_disk_ephemeral_38GB",
                                                                                           "properties": {"disk_type": "ephemeral",
                                                                                                          "size": 38}},
                                                                                          {"local_storage_id": "local_disk_root_10GB",
                                                                                           "description": "local_disk_root_10GB",
                                                                                           "properties": {"disk_type": "root",
                                                                                                          "size": 10}},
                                                                                          {"local_storage_id": "local_disk_ephemeral_14GB",
                                                                                           "description": "local_disk_ephemeral_14GB",
                                                                                           "properties": {"disk_type": "ephemeral",
                                                                                                          "size": 14}}],
                                                                       "vnf_flavours": [],
                                                                       "vnf_exposed": {"external_cps": [{"key_name": "ZTE_NET39_virtualLink",
                                                                                                         "cpd_id": "CP_NO_3_PFU_ZTE_NET39"},
                                                                                                        {"key_name": "ZTE_NET39_virtualLink1",
                                                                                                         "cpd_id": "CP_NO_2_PFU_ZTE_NET39"},
                                                                                                        {"key_name": "ZTE_VGW_GTP_NET39_virtualLink",
                                                                                                         "cpd_id": "CP_NO_3_SLB_ZTE_VGW_GTP_NET39"},
                                                                                                        {"key_name": "ZTE_NET39_virtualLink2",
                                                                                                         "cpd_id": "CP_NO_2_SLB_ZTE_NET39"},
                                                                                                        {"key_name": "provider-zte_virtualLink",
                                                                                                         "cpd_id": "CP_NO_2_MPU_provider-zte"}],
                                                                                       "forward_cps": [{"key_name": "ZTE_VGW_GTP_NET39_forwarder",
                                                                                                        "cpd_id": "CP_NO_3_SLB_ZTE_VGW_GTP_NET39"},
                                                                                                       {"key_name": "ZTE_NET39_forwarder1",
                                                                                                        "cpd_id": "CP_NO_2_PFU_ZTE_NET39"},
                                                                                                       {"key_name": "ZTE_NET39_forwarder2",
                                                                                                        "cpd_id": "CP_NO_3_PFU_ZTE_NET39"},
                                                                                                       {"key_name": "provider-zte_forwarder",
                                                                                                        "cpd_id": "CP_NO_2_MPU_provider-zte"},
                                                                                                       {"key_name": "ZTE_NET39_forwarder",
                                                                                                        "cpd_id": "CP_NO_2_SLB_ZTE_NET39"}]},
                                                                       "reserved_total": {"portnum": 27,
                                                                                          "vcpunum": 72,
                                                                                          "memorysize": 233472,
                                                                                          "shdsize": 288,
                                                                                          "isreserve": 0,
                                                                                          "vmnum": 9},
                                                                       "policies": [{"scaling": [{"description": "zte vgw vnf policy",
                                                                                                  "policy_id": "Policy_1",
                                                                                                  "targets": ["VDU_S_CDB_51",
                                                                                                              "VDU_M_CDB_52",
                                                                                                              "VDU_M_SLB_42",
                                                                                                              "VDU_M_GSU_22",
                                                                                                              "VDU_M_MPU_12",
                                                                                                              "VDU_L_MPU_13",
                                                                                                              "VDU_M_PFU_32",
                                                                                                              "VDU_L_PFU_33"],
                                                                                                  "properties": {"policy_file": "Policies/zte-vcn-vnf-policy.xml"}}],
                                                                                     "healing": []}],
                                                                       "plugins": [],
                                                                       "services": [],
                                                                       "vcenter": [],
                                                                       "cps": [{"vl_id": "ZTE_VGW_MGT_NET39_virtualLink",
                                                                                "description": "ZTE_xGW_39_CDB_1_ZTE_VGW_MGT_NET39_s",
                                                                                "vdu_id": "VDU_S_CDB_51",
                                                                                "properties": {"service_port_created": False,
                                                                                               "name": "ZTE_xGW_39_CDB_1_ZTE_VGW_MGT_NET39_s",
                                                                                               "allowed_address_pairs": [],
                                                                                               "bandwidth": 0,
                                                                                               "is_virtual": False,
                                                                                               "guest_os_mt": 1400,
                                                                                               "vnic_type": "normal",
                                                                                               "floating_ip_address": {},
                                                                                               "mac_address": "",
                                                                                               "port_security_enabled": False,
                                                                                               "ip_address": "192.168.39.247",
                                                                                               "order": 1,
                                                                                               "security_groups": [],
                                                                                               "bond": "none"},
                                                                                "cp_id": "CP_ZTE_xGW_39_CDB_1_ZTE_VGW_MGT_NET39_su1",
                                                                                "cpd_id": "CP_NO_0_CDB_ZTE_VGW_MGT_NET39"},
                                                                               {"vl_id": "ZTE_VGW_SERVICE_NET39_virtualLink",
                                                                                "description": "ZTE_xGW_39_CDB_1_ZTE_VGW_SERVICE_NET39_s",
                                                                                "vdu_id": "VDU_S_CDB_51",
                                                                                "properties": {"service_port_created": False,
                                                                                               "name": "ZTE_xGW_39_CDB_1_ZTE_VGW_SERVICE_NET39_s",
                                                                                               "allowed_address_pairs": [],
                                                                                               "bandwidth": 0,
                                                                                               "is_virtual": False,
                                                                                               "guest_os_mt": 1400,
                                                                                               "vnic_type": "normal",
                                                                                               "floating_ip_address": {},
                                                                                               "mac_address": "",
                                                                                               "port_security_enabled": False,
                                                                                               "ip_address": "192.168.40.247",
                                                                                               "order": 2,
                                                                                               "security_groups": [],
                                                                                               "bond": "none"},
                                                                                "cp_id": "CP_ZTE_xGW_39_CDB_1_ZTE_VGW_SERVICE_NET39_su2",
                                                                                "cpd_id": "CP_NO_1_CDB_ZTE_VGW_SERVICE_NET39"}],
                                                                       "vls": [{"route_external": False,
                                                                                "route_id": "",
                                                                                "vl_id": "ZTE_VGW_MGT_NET39_virtualLink",
                                                                                "description": "ZTE_VGW_MGT_NET39_s",
                                                                                "properties": {"gateway_ip": "",
                                                                                               "vendor": "ZTE",
                                                                                               "name": "ZTE_VGW_MGT_NET39_s",
                                                                                               "location_info": {"vdc": "OG_OrganizationDC",
                                                                                                                 "vimid": 1,
                                                                                                                 "tenant": "",
                                                                                                                 "vapp": "xgw"},
                                                                                               "start_ip": "",
                                                                                               "segmentation_id": "142",
                                                                                               "dns_nameservers": [],
                                                                                               "vds_name": "",
                                                                                               "mt": 1400,
                                                                                               "is_predefined": True,
                                                                                               "ip_version": 4,
                                                                                               "netmask": "255.255.255.0",
                                                                                               "end_ip": "",
                                                                                               "host_routes": [],
                                                                                               "vlan_transparent": False,
                                                                                               "physical_network": "physnet1",
                                                                                               "cidr": "192.168.39.0/24",
                                                                                               "dhcp_enabled": False,
                                                                                               "network_name": "ZTE_VGW_MGT_NET39",
                                                                                               "network_type": "vlan"}},
                                                                               {"route_external": False,
                                                                                "route_id": "",
                                                                                "vl_id": "ZTE_VGW_SERVICE_NET39_virtualLink",
                                                                                "description": "ZTE_VGW_SERVICE_NET39_s",
                                                                                "properties": {"gateway_ip": "",
                                                                                               "vendor": "ZTE",
                                                                                               "name": "ZTE_VGW_SERVICE_NET39_s",
                                                                                               "location_info": {"vdc": "OG_OrganizationDC",
                                                                                                                 "vimid": 1,
                                                                                                                 "tenant": "",
                                                                                                                 "vapp": "xgw"},
                                                                                               "start_ip": "",
                                                                                               "segmentation_id": "128",
                                                                                               "dns_nameservers": [],
                                                                                               "vds_name": "",
                                                                                               "mt": 1400,
                                                                                               "is_predefined": True,
                                                                                               "ip_version": 4,
                                                                                               "netmask": "255.255.255.0",
                                                                                               "end_ip": "",
                                                                                               "host_routes": [],
                                                                                               "vlan_transparent": False,
                                                                                               "physical_network": "physnet1",
                                                                                               "cidr": "192.168.40.0/24",
                                                                                               "dhcp_enabled": False,
                                                                                               "network_name": "ZTE_VGW_SERVICE_NET39",
                                                                                               "network_type": "vlan"}}],
                                                                       "element_groups": [],
                                                                       "metadata": {"plugin_info": "cn_plugin_3.0",
                                                                                    "vendor": "ZTE",
                                                                                    "is_shared": False,
                                                                                    "adjust_vnf_capacity": True,
                                                                                    "paas_project": "",
                                                                                    "description": "VMware",
                                                                                    "vnf_extend_type": "driver",
                                                                                    "domain_type": "CN",
                                                                                    "resview": "dync",
                                                                                    "script_info": "",
                                                                                    "service_category": "EPC",
                                                                                    "version": "ZTE",
                                                                                    "vnf_type": "SAE-GW",
                                                                                    "cross_dc": False,
                                                                                    "vmnumber_overquota_alarm": True,
                                                                                    "vnfd_version": "V00000001",
                                                                                    "id": "NFAR-ZTE-40-ZTE",
                                                                                    "name": 40}},
                                                        "operation": "instantiate",
                                                        "vnfmInstanceId": "31f8934e-c785-4fa5-9205-c5f374ada982"}},
                   "vimid": "vmware_vio",
                   "timestamp": "20171110105828",
                   "affectedcp": [{"changeType": "added",
                                   "cPInstanceId": "175767d4-c5bf-11e7-904d-fa163eee1ffe",
                                   "ownertype": 3,
                                   "cpdId": "CP_NO_0_CDB_ZTE_VGW_MGT_NET39",
                                   "portResource": {"resourceId": "3296b6d8-ebca-4d33-98f4-68d1bc63a3d0",
                                                    "tenant": "ZTE_ONAP_PRO",
                                                    "vimId": "vmware_vio",
                                                    "resourceName": "CP_ZTE_xGW_39_CDB_1_ZTE_VGW_MGT_NET39_su1"},
                                   "cpInstanceName": "CP_ZTE_xGW_39_CDB_1_ZTE_VGW_MGT_NET39_su1",
                                   "ownerid": "17502154-c5bf-11e7-904d-fa163eee1ffe",
                                   "virtualLinkInstanceId": "1753b60c-c5bf-11e7-904d-fa163eee1ffe"},
                                  {"changeType": "added",
                                   "cPInstanceId": "1758181e-c5bf-11e7-904d-fa163eee1ffe",
                                   "ownertype": 3,
                                   "cpdId": "CP_NO_1_CDB_ZTE_VGW_SERVICE_NET39",
                                   "portResource": {"resourceId": "5e277a18-94de-469a-a336-2c01ab46387e",
                                                    "tenant": "ZTE_ONAP_PRO",
                                                    "vimId": "vmware_vio",
                                                    "resourceName": "CP_ZTE_xGW_39_CDB_1_ZTE_VGW_SERVICE_NET39_su2"},
                                   "cpInstanceName": "CP_ZTE_xGW_39_CDB_1_ZTE_VGW_SERVICE_NET39_su2",
                                   "ownerid": "17502154-c5bf-11e7-904d-fa163eee1ffe",
                                   "virtualLinkInstanceId": "175472a4-c5bf-11e7-904d-fa163eee1ffe"}],
                   "vmlist": [{"VMNumber": 1,
                               "vdutype": "CDB",
                               "VMFlavor": "VDU_S_CDB_51",
                               "VMIDlist": [{"VMID": "e8ccc55a-3ebb-4e46-8260-dc4a1646ef4f",
                                             "VMName": "ZTE_xGW_39_CDB_1",
                                             "vimid": "vmware_vio",
                                             "tenant": "ZTE_ONAP_PRO"}]},
                              {"VMNumber": 1,
                               "vdutype": "SLB",
                               "VMFlavor": "VDU_M_SLB_42",
                               "VMIDlist": [{"VMID": "a9dd6a73-76ee-4d07-9554-08f14c17261f",
                                             "VMName": "ZTE_xGW_39_SLB_1",
                                             "vimid": "vmware_vio",
                                             "tenant": "ZTE_ONAP_PRO"}]}],
                   "EventType": 1,
                   "vnfmid": "31f8934e-c785-4fa5-9205-c5f374ada982",
                   "affectedvirtuallink ": [{"changeType": "added",
                                             "virtualLinkInstanceId": "1753b60c-c5bf-11e7-904d-fa163eee1ffe",
                                             "networkResource": {"resourceId": "c55e0788-3683-48a1-b88a-a0cb5e05bd44",
                                                                 "tenant": None,
                                                                 "vimId": "vmware_vio",
                                                                 "resourceName": "ZTE_VGW_MGT_NET39"},
                                             "virtualLinkDescId": "ZTE_VGW_MGT_NET39_virtualLink",
                                             "tenant": "ZTE_ONAP_PRO",
                                             "subnetworkResource": {"resourceId": "33c8a03d-00c9-4c57-a348-26dae462b473",
                                                                    "tenant": None,
                                                                    "vimId": "vmware_vio",
                                                                    "resourceName": "ZTE_VGW_MGT_NET39_s"}},
                                            {"changeType": "added",
                                             "virtualLinkInstanceId": "175472a4-c5bf-11e7-904d-fa163eee1ffe",
                                             "networkResource": {"resourceId": "2d22b6e4-340b-45a8-8757-5206aa056b92",
                                                                 "tenant": None,
                                                                 "vimId": "vmware_vio",
                                                                 "resourceName": "ZTE_VGW_SERVICE_NET39"},
                                             "virtualLinkDescId": "ZTE_VGW_SERVICE_NET39_virtualLink",
                                             "tenant": "ZTE_ONAP_PRO",
                                             "subnetworkResource": {"resourceId": "2ea2acc0-a4ed-44f8-9d31-9cdc9e3ebe62",
                                                                    "tenant": None,
                                                                    "vimId": "vmware_vio",
                                                                    "resourceName": "ZTE_VGW_SERVICE_NET39_s"}}]}
