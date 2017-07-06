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
        """
        Initate_VNF
        """
        vnfm_info = {u'userName': u'admin',
                     u'vendor': u'ZTE',
                     u'name': u'ZTE_VNFM_237_62',
                     u'vimId': u'516cee95-e8ca-4d26-9268-38e343c2e31e',
                     u'url': u'http://192.168.237.165:2324',
                     u'certificateUrl': u'',
                     u'version': u'V1.0',
                     u'vnfmId': u'b0797c9b-3da9-459c-b25c-3813e9d8fd70',
                     u'password': u'admin',
                     u'type': u'ztevmanagerdriver',
                     u'createTime': u'2016-10-31 11:08:39',
                     u'description': u''}
        vnfd_info = {u'vnfInstanceInfo': [{u'vnfInstanceId': u'59b79a9f-9e37-4f6c-acaf-5f41d9cb3f76',
                                           u'vnfInstanceName': u'VFW_59b79a9f-9e37-4f6c-acaf-5f41d9cb3f76'},
                                          {u'vnfInstanceId': u'6c5e4bd3-e8a6-42d8-a7a4-53a8ef74c6ac',
                                           u'vnfInstanceName': u'VFW_6c5e4bd3-e8a6-42d8-a7a4-53a8ef74c6ac'},
                                          {u'vnfInstanceId': u'930de5c9-8043-41df-ace8-ede2262a3713',
                                           u'vnfInstanceName': u'VFW_930de5c9-8043-41df-ace8-ede2262a3713'},
                                          {u'vnfInstanceId': u'c677a305-a7bd-4baf-9eee-c383c588bb3c',
                                           u'vnfInstanceName': u'VFW_c677a305-a7bd-4baf-9eee-c383c588bb3c'},
                                          {u'vnfInstanceId': u'e708e5c5-bdf4-436a-b928-826887806d82',
                                           u'vnfInstanceName': u'VFW_e708e5c5-bdf4-436a-b928-826887806d82'}],
                     u'csarId': u'd852e1be-0aac-48f1-b1a4-cd825f6cdf9a',
                     u'imageInfo': [
                         {u'status': u'Enable', u'index': u'0',
                          u'vimId': u'516cee95-e8ca-4d26-9268-38e343c2e31e',
                          u'fileName': u'VFW_IMAGE_VCPE_ZTE',
                          u'vimUser': u'admin',
                          u'imageId': u'd2b73154-0414-466a-a1e6-51b9461b753a',
                          u'tenant': u'admin'}],
                     u'packageInfo': {u'usageState': u'NotInUse',
                                      u'onBoardState': u'onBoarded',
                                      u'name': u'VFW',
                                      u'format': u'yaml',
                                      u'provider': u'ZTE',
                                      u'vnfdProvider': u'zte',
                                      u'vnfdId': u'vcpe_vfw_zte_1_0',
                                      u'deletionPending': False,
                                      u'version': u'v1.0',
                                      u'vnfVersion': u'1.0',
                                      u'vnfdVersion': u'1.0.0',
                                      u'processState': u'normal',
                                      u'modifyTime': u'2016-10-31 16:21:32',
                                      u'downloadUri': u'http://192.168.233.226:80/',
                                      u'operationalState': u'Disabled',
                                      u'createTime': u'2016-10-31 16:21:11',
                                      u'size': u'12.1 MB'}}
        packageInfo = {u'usageState': u'NotInUse',
                       u'onBoardState': u'onBoarded',
                       u'name': u'VFW',
                       u'format': u'yaml',
                       u'provider': u'ZTE',
                       u'vnfdProvider': u'zte',
                       u'vnfdId': u'vcpe_vfw_zte_1_0',
                       u'deletionPending': False,
                       u'version': u'v1.0',
                       u'vnfVersion': u'1.0',
                       u'vnfdVersion': u'1.0.0',
                       u'processState': u'normal',
                       u'modifyTime': u'2016-10-31 16:21:32',
                       u'downloadUri': u'http://192.168.233.226:80/files/catalog-http/NFAR/ZTE/VFW/v1.0/VFW.csar',
                       u'operationalState': u'Disabled',
                       u'createTime': u'2016-10-31 16:21:11', u'size': u'12.1 MB'}

        ret = [0, json.JSONEncoder().encode({"vnfInstanceId":"8",
                                             "jobid":"NF-CREATE-8-b384535c-9f45-11e6-8749-fa163e91c2f9"}),
               '200']

        r1 = [0, json.JSONEncoder().encode(vnfm_info), "200"]

        r2 = [0, json.JSONEncoder().encode(vnfd_info), "200"]

        r3 = [0, json.JSONEncoder().encode(packageInfo), "200"]


        mock_call_req.side_effect = [r1, r2, r3, ret]

        req_data = {'vnfInstanceName': 'VFW_f88c0cb7-512a-44c4-bd09-891663f19367',
                    'vnfPackageId': 'd852e1be-0aac-48f1-b1a4-cd825f6cdf9a',
                    'vnfDescriptorId': 'vcpe_vfw_zte_1_0',
                    'additionalParam': {'sdncontroller': 'e4d637f1-a4ec-4c59-8b20-4e8ab34daba9',
                                        'NatIpRange': '192.167.0.10-192.168.0.20',
                                        'm6000_mng_ip': '192.168.11.11',
                                        'externalPluginManageNetworkName': 'plugin_net_2014',
                                        'location': '516cee95-e8ca-4d26-9268-38e343c2e31e',
                                        'externalManageNetworkName': 'mng_net_2017',
                                        'sfc_data_network': 'sfc_data_net_2016',
                                        'externalDataNetworkName': 'Flow_out_net',
                                        'inputs':{}}}

        response = self.client.post("/openoapi/ztevnfm/v1/ztevnfmid/vnfs",
                                    data=json.dumps(req_data), content_type="application/json")
        self.assertEqual(str(status.HTTP_200_OK), response.status_code)
        expect_resp_data = {"jobid": "NF-CREATE-8-b384535c-9f45-11e6-8749-fa163e91c2f9", "vnfInstanceId": "8"}
        self.assertEqual(expect_resp_data, response.data)

    @mock.patch.object(restcall, 'call_req')
    def test_terminate_vnf__002(self, mock_call_req):
        """
        Terminate_VNF
        """
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

        r2 = [0, json.JSONEncoder().encode({"vnfInstanceId": "1", "JobId": "1"}), "200"]
        mock_call_req.side_effect = [r1, r2]

        response = self.client.post("/openoapi/ztevnfm/v1/ztevnfmid/vnfs/vbras_innstance_id/terminate")

        self.assertEqual(str(status.HTTP_200_OK), response.status_code)
        expect_resp_data = {"jobid": "1", "vnfInstanceId": "1"}
        self.assertEqual(expect_resp_data, response.data)

    @mock.patch.object(restcall, 'call_req')
    def test_query_vnf_003(self, mock_call_req):
        """
        Query_VNF
        """
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

        response = self.client.get("/openoapi/ztevnfm/v1/ztevnfmid/vnfs/vbras_innstance_id")

        self.assertEqual(str(status.HTTP_200_OK), response.status_code)

        expect_resp_data = {"vnfInfo": {"vnfStatus": "1"}}
        self.assertEqual(expect_resp_data, response.data)

    @mock.patch.object(restcall, 'call_req')
    def test_operation_status_004(self, mock_call_req):
        """
        Operation_status
        """
        vnfm_info = {u'userName': u'admin',
                     u'vendor': u'ZTE',
                     u'name': u'ZTE_VNFM_237_62',
                     u'vimId': u'516cee95-e8ca-4d26-9268-38e343c2e31e',
                     u'url': u'http://192.168.237.165:2324',
                     u'certificateUrl': u'',
                     u'version': u'V1.0',
                     u'vnfmId': u'b0797c9b-3da9-459c-b25c-3813e9d8fd70',
                     u'password': u'admin',
                     u'type': u'ztevmanagerdriver',
                     u'createTime': u'2016-10-31 11:08:39',
                     u'description': u''}
        resp_body = {"responsedescriptor":
                         {"status": "processing", "responsehistorylist": [
                             {"status": "error",
                              "progress": 255,
                              "errorcode": "",
                              "responseid": 20,
                              "statusdescription": "'JsonParser' object has no attribute 'parser_info'"}],
                          "responseid": 21,
                          "errorcode": "",
                          "progress": 40,
                          "statusdescription": "Create nf apply resource failed"},
                     "jobid": "NF-CREATE-11-ec6c2f2a-9f48-11e6-9405-fa163e91c2f9"}
        r1 = [0, json.JSONEncoder().encode(vnfm_info), '200']
        r2 = [0, json.JSONEncoder().encode(resp_body), '200']
        mock_call_req.side_effect = [r1, r2]
        response = self.client.get("/openoapi/ztevmanagerdriver/v1/{vnfmid}/jobs/{jobid}?responseId={responseId}".
            format(
            vnfmid=vnfm_info["vnfmId"],
            jobid=resp_body["jobid"],
            responseId=resp_body["responsedescriptor"]["responseid"]))

        self.assertEqual(str(status.HTTP_200_OK), response.status_code)

        expect_resp_data = resp_body
        self.assertDictEqual(expect_resp_data, response.data)

    @mock.patch.object(restcall, 'call_req')
    def test_grantvnf_005(self, mock_call_req):
        """
        Grant_VNF
        """
        ret = [0,
               '{"vim":{"accessinfo":{"tenant":"admin"},"vimid":"516cee95-e8ca-4d26-9268-38e343c2e31e"}}',
               '201']

        req_data = {
            "vnfmid": "13232222",
            "nfvoid": "03212234",
            "vimid": "12345678",
            "exvimidlist ":
                ["exvimid"],
            "tenant": " tenant1",
            "vnfistanceid": "1234",
            "operationright": "0",
            "vmlist": [
                {
                    "vmflavor": "SMP",
                    "vmnumber": "3"},
                {
                    "vmflavor": "CMP",
                    "vmnumber": "3"}
            ]}

        mock_call_req.return_value = ret
        response = self.client.put("/openoapi/ztevmanagerdriver/v1/resource/grant",
                                   data=json.dumps(req_data), content_type='application/json')

        self.assertEqual(str(status.HTTP_201_CREATED), response.status_code)

        expect_resp_data = {
            "vimid": "516cee95-e8ca-4d26-9268-38e343c2e31e",
            "tenant": "admin"}
        self.assertDictEqual(expect_resp_data, response.data)

    @mock.patch.object(restcall, 'call_req')
    def test_notify_006(self, mock_call_req):
        """
        Notification
        """
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

        r2 = [0, json.JSONEncoder().encode(
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

        req_data = {
            "nfvoid": "1",
            "vnfmid": "876543211",
            "vimid": "6543211",
            "timestamp": "1234567890",
            "vnfinstanceid": "1",
            "eventtype": "0",
            "vmlist":
                [
                    {
                        "vmflavor": "SMP",
                        "vmnumber": "3",
                        "vmidlist ": ["vmuuid"]},
                    {
                        "vmflavor": "CMP",
                        "vmnumber": "3",
                        "vmidlist ": ["vmuuid"]}]}
        mock_call_req.side_effect = [r1, r2]
        response = self.client.post("/openoapi/ztevmanagerdriver/v1/vnfs/lifecyclechangesnotification",
                                    data=json.dumps(req_data), content_type='application/json')

        self.assertEqual(str(status.HTTP_200_OK), response.status_code)

        expect_resp_data = None
        self.assertEqual(expect_resp_data, response.data)

    '''
    @mock.patch.object(restcall, 'call_req')
    def test_scale(self,mock_call_req):
        job_info = {"jobid":"801","nfInstanceId":"101"}
        vnfm_info = {u'userName': u'admin',
                     u'vendor': u'ZTE',
                     u'name': u'ZTE_VNFM_237_62',
                     u'vimId': u'516cee95-e8ca-4d26-9268-38e343c2e31e',
                     u'url': u'http://192.168.237.165:2324',
                     u'certificateUrl': u'',
                     u'version': u'V1.0',
                     u'vnfmId': u'b0797c9b-3da9-459c-b25c-3813e9d8fd70',
                     u'password': u'admin',
                     u'type': u'ztevmanagerdriver',
                     u'createTime': u'2016-10-31 11:08:39',
                     u'description': u''}

        ret = [0, json.JSONEncoder().encode(job_info), "202"]
        ret_vnfm = [0,json.JSONEncoder().encode(job_info), "200"]
        mock_call_req.side_effect = [ret_vnfm, ret]

        vnfd_info = {
            "vnf_flavours":[
                {
                    "flavour_id":"flavour1",
                    "description":"",
                    "vdu_profiles":[
                        {
                            "vdu_id":"vdu1Id",
                            "instances_minimum_number": 1,
                            "instances_maximum_number": 4,
                            "local_affinity_antiaffinity_rule":[
                                {
                                    "affinity_antiaffinity":"affinity",
                                    "scope":"node",
                                }
                            ]
                        }
                    ],
                    "scaling_aspects":[
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
                      "properties":{
                          "name": "elementGroup1",
                      },
                      "members": ["gsu_vm","pfu_vm"],
                  }
            ]
        }

        scale_vnf_data = {
            "type":"SCALE_OUT",
            "aspectId":"demo_aspect",
            "numberOfSteps":"3",
            "additionalParam":{
                "vnfdModel":vnfd_info
            }
        }


        response = self.client.post("/openoapi/ztevnfm/v1/vnfmid/vnfs/101/scale",
                                   data=json.dumps(scale_vnf_data), content_type='application/json')

        self.assertEqual(str(status.HTTP_202_ACCEPTED), response.status_code)

        expect_resp_data = {"jobid":"801","nfInstanceId":"101"}
        self.assertDictEqual(expect_resp_data, response.data)
    '''
