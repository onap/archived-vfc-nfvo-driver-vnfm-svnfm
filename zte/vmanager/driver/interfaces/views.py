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

import inspect
import json
import logging
import os
import traceback

from drf_yasg import openapi
from drf_yasg.utils import swagger_auto_schema
from rest_framework import status
from rest_framework.response import Response
from rest_framework.views import APIView
from django.http import StreamingHttpResponse

from driver.interfaces.serializers import HealReqSerializer, InstScaleHealRespSerializer, ScaleReqSerializer, \
    NotifyReqSerializer, GrantRespSerializer, GrantReqSerializer, JobQueryRespSerializer, TerminateVnfRequestSerializer, \
    InstantiateVnfRequestSerializer, QueryVnfResponseSerializer, SubscribesRespSerializer, \
    SubscribeReqSerializer, SubscribeRespSerializer, VnfPkgsSerializer, NfvoInfoReqSerializer
from driver.pub.config.config import VNF_FTP
from driver.pub.utils import restcall
from driver.pub.utils.restcall import req_by_msb

CHUNK_SIZE = 1024 * 8

logger = logging.getLogger(__name__)


def load_json_file(file_name):
    json_file = os.path.join(os.path.dirname(__file__), "data/" + file_name)
    f = open(json_file)
    json_data = json.JSONDecoder().decode(f.read())
    f.close()
    return json_data


def read(file_path, start, end):
    fp = open(file_path, 'rb')
    fp.seek(start)
    pos = start
    while pos + CHUNK_SIZE < end:
        yield fp.read(CHUNK_SIZE)
        pos = fp.tell()
    yield fp.read(end - pos)


def parse_file_range(file_path, file_range):
    start, end = 0, os.path.getsize(file_path)
    if file_range:
        [start, end] = file_range.split('-')
        start, end = start.strip(), end.strip()
        start, end = int(start), int(end)
    return start, end


def fun_name():
    return "=================%s==================" % inspect.stack()[1][3]


def ignorcase_get(args, key):
    if not key:
        return ""
    if not args:
        return ""
    if key in args:
        return args[key]
    for old_key in args:
        if old_key.upper() == key.upper():
            return args[old_key]
    return ""


# Query vnfm_info from nslcm
def get_vnfminfo_from_nslcm(vnfmid):
    ret = req_by_msb("api/nslcm/v1/vnfms/%s" % vnfmid, "GET")
    return ret


# Query vnfd_info from nslcm
def vnfd_get(vnfpackageid):
    ret = req_by_msb("api/nslcm/v1/vnfpackage/%s" % vnfpackageid, "GET")
    return ret


# Query vnfpackage_info from nslcm
def vnfpackage_get(csarid):
    ret = req_by_msb("api/nslcm/v1/vnfpackage/%s" % csarid, "GET")
    return ret


class InstantiateVnf(APIView):
    @swagger_auto_schema(
        request_body=InstantiateVnfRequestSerializer(),
        responses={
            status.HTTP_200_OK: InstScaleHealRespSerializer(),
            status.HTTP_500_INTERNAL_SERVER_ERROR: "Internal error"
        }
    )
    def post(self, request, vnfmid):
        try:
            logger.debug("[%s] request.data=%s", fun_name(), request.data)
            instantiateVnfRequestSerializer = InstantiateVnfRequestSerializer(data=request.data)
            if not instantiateVnfRequestSerializer.is_valid():
                raise Exception(instantiateVnfRequestSerializer.errors)

            ret = get_vnfminfo_from_nslcm(vnfmid)
            if ret[0] != 0:
                raise Exception(ret[1])

            vnfm_info = json.JSONDecoder().decode(ret[1])
            logger.debug("[%s] vnfm_info=%s", fun_name(), vnfm_info)
            vnf_package_id = ignorcase_get(instantiateVnfRequestSerializer.data, "vnfPackageId")
            ret = vnfd_get(vnf_package_id)
            if ret[0] != 0:
                raise Exception(ret[1])

            vnfd_info = json.JSONDecoder().decode(ret[1])
            logger.debug("[%s] vnfd_info=%s", fun_name(), vnfd_info)
            csar_id = ignorcase_get(vnfd_info, "csarId")
            ret = vnfpackage_get(csar_id)
            if ret[0] != 0:
                raise Exception(ret[1])

            vnf_package_info = json.JSONDecoder().decode(ret[1])
            packageInfo = ignorcase_get(vnf_package_info, "packageInfo")
            logger.debug("[%s] packageInfo=%s", fun_name(), packageInfo)
            logger.debug("VNF_FTP=%s", VNF_FTP)
            data = {
                "NFVOID": 1,
                "VNFMID": vnfmid,
                "vnfd_id": packageInfo.get("vnfdId"),
                "deployflavorid": "TODO",
                "extension": {},
                "inputs": []
            }

            additionalParam = ignorcase_get(instantiateVnfRequestSerializer.data, "additionalParam")
            for name, value in ignorcase_get(additionalParam, "inputs").items():
                data["inputs"].append({"key_name": name, "value": value, "type": "TODO"})

            logger.debug("[%s] call_req data=%s", fun_name(), data)

            ret = restcall.call_req(
                base_url=ignorcase_get(vnfm_info, "url"),
                user=ignorcase_get(vnfm_info, "userName"),
                passwd=ignorcase_get(vnfm_info, "password"),
                auth_type=restcall.rest_no_auth,
                resource="v1/vnfs",
                method='post',
                content=json.JSONEncoder().encode(data))

            logger.debug("[%s] call_req ret=%s", fun_name(), ret)
            if ret[0] != 0:
                raise Exception(ret[1])

            resp = json.JSONDecoder().decode(ret[1])
            resp_data = {
                "vnfInstanceId": ignorcase_get(resp, "VNFInstanceID"),
                "jobId": ignorcase_get(resp, "JobId")
            }
            logger.debug("[%s]resp_data=%s", fun_name(), resp_data)
            instRespSerializer = InstScaleHealRespSerializer(data=resp_data)
            if not instRespSerializer.is_valid():
                raise Exception(instRespSerializer.errors)

            logger.debug("[%s] instRespSerializer.data=%s", fun_name(), instRespSerializer.data)
            return Response(data=instRespSerializer.data, status=status.HTTP_200_OK)
        except Exception as e:
            logger.error("Error occurred when instantiating VNF,error:%s", e.message)
            logger.error(traceback.format_exc())
            return Response(data={'error': 'InstantiateVnf expection'}, status=status.HTTP_500_INTERNAL_SERVER_ERROR)


class TerminateVnf(APIView):
    @swagger_auto_schema(
        request_body=TerminateVnfRequestSerializer(),
        responses={
            status.HTTP_200_OK: InstScaleHealRespSerializer(),
            status.HTTP_500_INTERNAL_SERVER_ERROR: "Internal error"
        }
    )
    def post(self, request, vnfmid, vnfInstanceId):
        try:
            logger.debug("[%s] request.data=%s", fun_name(), request.data)
            logger.debug("vnfmid=%s, vnfInstanceId=%s", vnfmid, vnfInstanceId)
            terminate_vnf_request_serializer = TerminateVnfRequestSerializer(data=request.data)
            if not terminate_vnf_request_serializer.is_valid():
                raise Exception(terminate_vnf_request_serializer.errors)

            ret = get_vnfminfo_from_nslcm(vnfmid)
            if ret[0] != 0:
                raise Exception(ret[1])

            vnfm_info = json.JSONDecoder().decode(ret[1])
            logger.debug("[%s] vnfm_info=%s", fun_name(), vnfm_info)
            ret = restcall.call_req(
                base_url=ignorcase_get(vnfm_info, "url"),
                user=ignorcase_get(vnfm_info, "userName"),
                passwd=ignorcase_get(vnfm_info, "password"),
                auth_type=restcall.rest_no_auth,
                resource="v1/vnfs/%s?NFVOID=1&VNFMID=%s" % (vnfInstanceId, vnfmid),
                method='delete',
                content='{}')
            if ret[0] != 0:
                raise Exception(ret[1])

            resp = json.JSONDecoder().decode(ret[1])
            resp_data = {
                "vnfInstanceId": vnfInstanceId,
                "jobId": ignorcase_get(resp, "JobId")
            }
            logger.debug("[%s]resp_data=%s", fun_name(), resp_data)
            terminateRespSerializer = InstScaleHealRespSerializer(data=resp_data)
            if not terminateRespSerializer.is_valid():
                raise Exception(terminateRespSerializer.errors)
            return Response(data=terminateRespSerializer.data, status=status.HTTP_200_OK)
        except Exception as e:
            logger.error("Error occurred when terminating VNF,error: %s", e.message)
            logger.error(traceback.format_exc())
            return Response(data={'error': 'TerminateVnf expection'}, status=status.HTTP_500_INTERNAL_SERVER_ERROR)


class QueryVnf(APIView):
    @swagger_auto_schema(
        responses={
            status.HTTP_200_OK: QueryVnfResponseSerializer(),
            status.HTTP_500_INTERNAL_SERVER_ERROR: "Internal error"
        }
    )
    def get(self, request, vnfmid, vnfInstanceId):
        try:
            logger.debug("[%s] request.data=%s", fun_name(), request.data)
            ret = get_vnfminfo_from_nslcm(vnfmid)
            if ret[0] != 0:
                raise Exception(ret[1])

            vnfm_info = json.JSONDecoder().decode(ret[1])
            logger.debug("[%s] vnfm_info=%s", fun_name(), vnfm_info)
            ret = restcall.call_req(
                base_url=ignorcase_get(vnfm_info, "url"),
                user=ignorcase_get(vnfm_info, "userName"),
                passwd=ignorcase_get(vnfm_info, "password"),
                auth_type=restcall.rest_no_auth,
                resource="v1/vnfs/%s" % vnfInstanceId,
                method='get',
                content=json.JSONEncoder().encode({}))
            if ret[0] != 0:
                raise Exception(ret[1])

            resp = json.JSONDecoder().decode(ret[1])
            vnf_status = ignorcase_get(resp, "vnfinstancestatus")
            resp_data = {"vnfInfo": {"vnfStatus": vnf_status}}
            logger.debug("[%s]resp_data=%s", fun_name(), resp_data)
            queryVnfResponseSerializer = QueryVnfResponseSerializer(data=resp_data)
            if not queryVnfResponseSerializer.is_valid():
                raise Exception(queryVnfResponseSerializer.errors)
            return Response(data=queryVnfResponseSerializer.data, status=status.HTTP_200_OK)
        except Exception as e:
            logger.error("Error occurred when querying VNF information,error:%s", e.message)
            logger.error(traceback.format_exc())
            return Response(data={'error': 'QueryVnf expection'}, status=status.HTTP_500_INTERNAL_SERVER_ERROR)


class JobView(APIView):
    @swagger_auto_schema(
        manual_parameters=[
            openapi.Parameter('responseId',
                              openapi.IN_QUERY,
                              "responseId",
                              type=openapi.TYPE_INTEGER
                              ),
        ],
        responses={
            status.HTTP_200_OK: JobQueryRespSerializer(),
            status.HTTP_500_INTERNAL_SERVER_ERROR: "Internal error"
        }
    )
    def get(self, request, vnfmid, jobid):
        try:
            logger.debug("[%s] request.data=%s", fun_name(), request.data)
            ret = get_vnfminfo_from_nslcm(vnfmid)
            if ret[0] != 0:
                raise Exception(ret[1])

            vnfm_info = json.JSONDecoder().decode(ret[1])
            logger.debug("[%s] vnfm_info=%s", fun_name(), vnfm_info)
            operation_status_url = '/v1/jobs/{jobId}?NFVOID={nfvoId}&VNFMID={vnfmId}&ResponseID={responseId}'
            responseId = ignorcase_get(request.GET, 'responseId')
            query_url = operation_status_url.format(jobId=jobid, nfvoId=1, vnfmId=vnfmid, responseId=responseId)
            ret = restcall.call_req(
                base_url=ignorcase_get(vnfm_info, 'url'),
                user=ignorcase_get(vnfm_info, 'userName'),
                passwd=ignorcase_get(vnfm_info, 'password'),
                auth_type=restcall.rest_no_auth,
                resource=query_url,
                method='get',
                content={})

            if ret[0] != 0:
                raise Exception(ret[1])

            resp_data = json.JSONDecoder().decode(ret[1])
            logger.debug("[%s]resp_data=%s", fun_name(), resp_data)
            jobQueryRespSerializer = JobQueryRespSerializer(data=resp_data)
            if not jobQueryRespSerializer.is_valid():
                raise Exception(jobQueryRespSerializer.errors)

            return Response(data=jobQueryRespSerializer.data, status=status.HTTP_200_OK)
        except Exception as e:
            logger.error("Error occurred when getting operation status information,error:%s", e.message)
            logger.error(traceback.format_exc())
            return Response(data={'error': 'QueryJob expection'}, status=status.HTTP_500_INTERNAL_SERVER_ERROR)


class GrantVnf(APIView):
    @swagger_auto_schema(
        request_body=GrantReqSerializer(),
        responses={
            status.HTTP_201_CREATED: GrantRespSerializer(),
            status.HTTP_500_INTERNAL_SERVER_ERROR: 'Internal error'
        }
    )
    def put(self, request):
        logger.debug("=====GrantVnf=====")
        try:
            logger.debug("request.data = %s", request.data)
            grantReqSerializer = GrantReqSerializer(data=request.data)
            if not grantReqSerializer.is_valid():
                raise Exception(grantReqSerializer.errors)

            logger.debug("grantReqSerializer.data = %s", grantReqSerializer.data)
            req_data = {
                "vnfInstanceId": ignorcase_get(grantReqSerializer.data, "vnfistanceid"),
                "vnfDescriptorId": "",
                "addresource": [],
                "additionalparam": {
                    "vnfmid": ignorcase_get(grantReqSerializer.data, "vnfmid"),
                    "vimid": ignorcase_get(grantReqSerializer.data, "vimid"),
                    "tenant": ignorcase_get(grantReqSerializer.data, "tenant")
                }
            }
            if ignorcase_get(grantReqSerializer.data, "operationright") == 0:
                req_data["lifecycleOperation"] = "Instantiate"
                for vm in ignorcase_get(grantReqSerializer.data, "vmlist"):
                    for i in range(int(ignorcase_get(vm, "VMNumber"))):
                        req_data["addresource"].append(
                            {
                                "type": "vdu",
                                "resourceDefinitionId": i,
                                "vdu": ignorcase_get(vm, "VMFlavor"),
                                "vimid": ignorcase_get(vm, "vimid"),
                                "tenant": ignorcase_get(vm, "tenant")})

            logger.debug("req_data=%s", req_data)
            ret = req_by_msb('api/nslcm/v1/ns/grantvnf', "POST", content=json.JSONEncoder().encode(req_data))
            logger.info("ret = %s", ret)
            if ret[0] != 0:
                raise Exception(ret[1])

            resp = json.JSONDecoder().decode(ret[1])
            resp_data = {
                'vimid': ignorcase_get(resp['vim'], 'vimid'),
                'tenant': ignorcase_get(ignorcase_get(resp['vim'], 'accessinfo'), 'tenant')
            }
            logger.debug("[%s]resp_data=%s", fun_name(), resp_data)
            grantRespSerializer = GrantRespSerializer(data=resp_data)
            if not grantRespSerializer.is_valid():
                raise Exception(grantRespSerializer.errors)

            logger.debug("grantRespSerializer.data=%s", grantRespSerializer.data)
            return Response(data=grantRespSerializer.data, status=status.HTTP_201_CREATED)
        except Exception as e:
            logger.error("Error occurred in Grant VNF, error: %s", e.message)
            logger.error(traceback.format_exc())
            return Response(data={'error': 'Grant expection'}, status=status.HTTP_500_INTERNAL_SERVER_ERROR)


class Notify(APIView):
    @swagger_auto_schema(
        request_body=NotifyReqSerializer(),
        responses={
            status.HTTP_200_OK: 'Successfully',
            status.HTTP_500_INTERNAL_SERVER_ERROR: 'Internal error'
        }
    )
    def post(self, request):
        try:
            logger.debug("[%s]request.data = %s", fun_name(), request.data)
            notifyReqSerializer = NotifyReqSerializer(data=request.data)
            if not notifyReqSerializer.is_valid():
                raise Exception(notifyReqSerializer.errors)

            logger.debug("[%s]notifyReqSerializer.data = %s", fun_name(), notifyReqSerializer.data)
            req_data = {
                "status": "result",
                "vnfInstanceId": ignorcase_get(notifyReqSerializer.data, "vnfinstanceid"),
                "vnfmId": ignorcase_get(notifyReqSerializer.data, "vnfmid"),
                "vimId": ignorcase_get(notifyReqSerializer.data, "vimid"),
                "operation": ignorcase_get(notifyReqSerializer.data, "EventType"),
                "jobId": "notMust",
                "affectedVl": [],
                "affectedCp": [],
                "affectedVirtualStorage": [],
                "affectedVnfc": [],
            }

            extension = ignorcase_get(notifyReqSerializer.data, "extension")
            openo_notification = ignorcase_get(extension, "openo_notification")
            if openo_notification:
                affectedvnfcs = ignorcase_get(openo_notification, "affectedVnfc")
                affectedvls = ignorcase_get(openo_notification, "affectedvirtuallink")
                affectedcps = ignorcase_get(openo_notification, "affectedCp")
                vnfdmodule = ignorcase_get(openo_notification, "vnfdmodule")
            else:
                affectedvnfcs = ignorcase_get(ignorcase_get(notifyReqSerializer.data, "extension"), "affectedvnfc")
                affectedvls = ignorcase_get(ignorcase_get(notifyReqSerializer.data, "extension"), "affectedvl")
                affectedcps = ignorcase_get(ignorcase_get(notifyReqSerializer.data, "extension"), "affectedcp")
                vnfdmodule = ignorcase_get(ignorcase_get(notifyReqSerializer.data, "extension"), "vnfdmodule")

            req_data["vnfdmodule"] = vnfdmodule

            for affectedvnfc in affectedvnfcs:
                req_data["affectedVnfc"].append({
                    "vnfcInstanceId": ignorcase_get(affectedvnfc, "vnfcInstanceId"),
                    "vduId": ignorcase_get(affectedvnfc, "vduId"),
                    "changeType": ignorcase_get(affectedvnfc, "changeType"),
                    "vimId": ignorcase_get(ignorcase_get(affectedvnfc, "computeResource"), "vimId"),
                    "vmId": ignorcase_get(ignorcase_get(affectedvnfc, "computeResource"), "resourceId"),
                    "vmName": ignorcase_get(ignorcase_get(affectedvnfc, "computeResource"), "resourceName")
                })

            for affectedvl in affectedvls:
                req_data["affectedVl"].append({
                    "vlInstanceId": ignorcase_get(affectedvl, "virtualLinkInstanceId"),
                    "changeType": ignorcase_get(affectedvl, "changeType"),
                    "vimId": ignorcase_get(ignorcase_get(affectedvl, "networkResource"), "vimId"),
                    "vldId": ignorcase_get(affectedvl, "virtuallinkdescid"),
                    "networkResource": {
                        "resourceType": "network",
                        "resourceId": ignorcase_get(ignorcase_get(affectedvl, "networkresource"), "resourceid"),
                        "resourceName": ignorcase_get(ignorcase_get(affectedvl, "networkresource"), "resourcename")
                    }
                })

            for affectedcp in affectedcps:
                req_data["affectedCp"].append(affectedcp)

            vnfmid = ignorcase_get(req_data, 'vnfmId')
            vnfInstanceId = ignorcase_get(req_data, 'vnfinstanceid')
            notify_url = 'api/nslcm/v1/ns/%s/vnfs/%s/Notify' % (vnfmid, vnfInstanceId)
            logger.debug("notify_url = %s", notify_url)
            logger.debug("req_data = %s", req_data)
            ret = req_by_msb(notify_url, "POST", content=json.JSONEncoder().encode(req_data))

            logger.debug("[%s]data = %s", fun_name(), ret)
            if ret[0] != 0:
                raise Exception(ret[1])

            return Response(data=None, status=status.HTTP_200_OK)
        except Exception as e:
            logger.error("Error occurred in LCM notification,error: %s", e.message)
            logger.error(traceback.format_exc())
            return Response(data={'error': 'Notify expection'}, status=status.HTTP_500_INTERNAL_SERVER_ERROR)


class Scale(APIView):
    @swagger_auto_schema(
        request_body=ScaleReqSerializer(),
        responses={
            status.HTTP_202_ACCEPTED: InstScaleHealRespSerializer(),
            status.HTTP_500_INTERNAL_SERVER_ERROR: "Internal error"
        }
    )
    def post(self, request, vnfmid, vnfInstanceId):
        logger.debug("====scale_vnf===")
        try:
            logger.debug("request.data = %s", request.data)
            logger.debug("requested_url = %s", request.get_full_path())
            scaleReqSerializer = ScaleReqSerializer(data=request.data)
            if not scaleReqSerializer.is_valid():
                raise Exception(scaleReqSerializer.errors)

            ret = get_vnfminfo_from_nslcm(vnfmid)
            if ret[0] != 0:
                raise Exception(ret[1])

            vnfm_info = json.JSONDecoder().decode(ret[1])
            scale_type = ignorcase_get(scaleReqSerializer.data, "type")
            aspect_id = ignorcase_get(scaleReqSerializer.data, "aspectId")
            number_of_steps = ignorcase_get(scaleReqSerializer.data, "numberOfSteps")
            data = {
                'vnfmid': vnfmid,
                'nfvoid': 1,
                'scaletype': '0' if scale_type == 'SCALE_OUT' else '1',
                'vmlist': [{
                    'VMNumber': number_of_steps,
                    'VMFlavor': aspect_id
                }],
                'extension': ''
            }

            logger.debug("data = %s", data)
            ret = restcall.call_req(
                base_url=ignorcase_get(vnfm_info, "url"),
                user=ignorcase_get(vnfm_info, "userName"),
                passwd=ignorcase_get(vnfm_info, "password"),
                auth_type=restcall.rest_no_auth,
                resource='/v1/vnfs/{vnfInstanceID}/scale'.format(vnfInstanceID=vnfInstanceId),
                method='put',  # POST
                content=json.JSONEncoder().encode(data))
            logger.debug("ret=%s", ret)
            if ret[0] != 0:
                raise Exception('scale error')

            scaleRespSerializer = InstScaleHealRespSerializer(data=json.JSONDecoder().decode(ret[1]))
            if not scaleRespSerializer.is_valid():
                raise Exception(scaleRespSerializer.errors)

            logger.debug("scaleRespSerializer.data=%s", scaleRespSerializer.data)
            return Response(data=scaleRespSerializer.data, status=status.HTTP_202_ACCEPTED)
        except Exception as e:
            logger.error("Error occurred when scaling VNF,error:%s", e.message)
            logger.error(traceback.format_exc())
            return Response(data={'error': 'Scale expection'}, status=status.HTTP_500_INTERNAL_SERVER_ERROR)


class Heal(APIView):
    @swagger_auto_schema(
        request_body=HealReqSerializer(),
        responses={
            status.HTTP_202_ACCEPTED: InstScaleHealRespSerializer(),
            status.HTTP_500_INTERNAL_SERVER_ERROR: "Internal error"
        }
    )
    def post(self, request, vnfmid, vnfInstanceId):
        logger.debug("====heal_vnf===")
        try:
            logger.debug("request.data = %s", request.data)
            logger.debug("requested_url = %s", request.get_full_path())
            healReqSerializer = HealReqSerializer(data=request.data)
            if not healReqSerializer.is_valid():
                raise Exception(healReqSerializer.errors)

            logger.debug("healReqSerializer.data = %s", healReqSerializer.data)
            logger.debug("vnfmid = %s", vnfmid)
            ret = get_vnfminfo_from_nslcm(vnfmid)
            if ret[0] != 0:
                raise Exception(ret[1])

            vnfm_info = json.JSONDecoder().decode(ret[1])
            req_data = {
                "action": ignorcase_get(healReqSerializer.data, 'action'),
                "lifecycleoperation": "operate",
                "isgrace": "force",
                "affectedvm": [],
            }
            affectedvm = ignorcase_get(healReqSerializer.data, 'affectedvm')
            if isinstance(affectedvm, list):
                req_data['affectedvm'] = affectedvm
            else:
                req_data['affectedvm'].append(affectedvm)

            logger.debug("req_data = %s", req_data)
            ret = restcall.call_req(
                base_url=ignorcase_get(vnfm_info, "url"),
                user=ignorcase_get(vnfm_info, "userName"),
                passwd=ignorcase_get(vnfm_info, "password"),
                auth_type=restcall.rest_no_auth,
                resource='/api/v1/nf_m_i/nfs/{vnfInstanceID}/vms/operation'.format(vnfInstanceID=vnfInstanceId),
                method='post',
                content=json.JSONEncoder().encode(req_data))
            logger.debug("ret=%s", ret)
            if ret[0] != 0:
                raise Exception('heal error')

            healRespSerializer = InstScaleHealRespSerializer(data=json.JSONDecoder().decode(ret[1]))
            if not healRespSerializer.is_valid():
                raise Exception(healRespSerializer.errors)

            logger.debug("healRespSerializer.data=%s", healRespSerializer.data)
            return Response(data=healRespSerializer.data, status=status.HTTP_202_ACCEPTED)
        except Exception as e:
            logger.error("Error occurred when healing VNF,error:%s", e.message)
            logger.error(traceback.format_exc())
            return Response(data={'error': 'Heal expection'}, status=status.HTTP_500_INTERNAL_SERVER_ERROR)


def get_vdus(nf_model, aspect_id):
    associated_group = ''
    members = []
    vnf_flavours = nf_model['vnf_flavours']
    for vnf_flaour in vnf_flavours:
        scaling_aspects = vnf_flaour['scaling_aspects']
        for aspect in scaling_aspects:
            if aspect_id == aspect['id']:
                associated_group = aspect['associated_group']
                break
    if not associated_group:
        logger.error('Cannot find the corresponding element group')
        raise Exception('Cannot find the corresponding element group')
    for element_group in nf_model['element_groups']:
        if element_group['group_id'] == associated_group:
            members = element_group['members']
    if not members:
        logger.error('Cannot find the corresponding members')
        raise Exception('Cannot find the corresponding members')
    return members


class SampleList(APIView):
    @swagger_auto_schema(
        responses={
            status.HTTP_200_OK: 'Successfully'})
    def get(self, request):
        logger.debug("get")
        return Response({"status": "active"})


class Subscribe(APIView):
    @swagger_auto_schema(
        responses={
            status.HTTP_200_OK: SubscribesRespSerializer(),
            status.HTTP_500_INTERNAL_SERVER_ERROR: "Internal error"
        }
    )
    def get(self, request):
        logger.debug("====Subscribe get====")
        resp_data = {
            "subscriptions": [{
                "subscribeid": "cdbddb00-452c-11e9-91e8-acc860114657",
                "filter": [{
                    "vendor": "ZTE",
                    "type": "vCPE",
                }],
                "notificationuri": " https://127.0.0.1:80/v2/vnfm/vnfds/notification",
            }]
        }
        return Response(data=resp_data, status=status.HTTP_200_OK)

    @swagger_auto_schema(
        request_body=SubscribeReqSerializer(),
        responses={
            status.HTTP_201_CREATED: SubscribeRespSerializer(),
            status.HTTP_500_INTERNAL_SERVER_ERROR: "Internal error"
        }
    )
    def post(self, request):
        logger.debug("====Subscribe post====")
        resp_data = {"subscribeid": "cdbddb00-452c-11e9-91e8-acc860114657"}
        return Response(data=resp_data, status=status.HTTP_201_CREATED)


class SubscribeDetail(APIView):
    @swagger_auto_schema(
        responses={
            status.HTTP_204_NO_CONTENT: "None",
            status.HTTP_500_INTERNAL_SERVER_ERROR: "Internal error"
        }
    )
    def delete(self, request, subscribeId):
        logger.debug("====SubscribeDetail delete %s====", subscribeId)
        return Response(status=status.HTTP_204_NO_CONTENT)


class VnfPkgs(APIView):
    @swagger_auto_schema(
        responses={
            status.HTTP_200_OK: VnfPkgsSerializer(),
            status.HTTP_500_INTERNAL_SERVER_ERROR: "Internal error"
        }
    )
    def get(self, request):
        logger.debug("====VnfPkgs get====")
        resp_data = {
            "data": [{
                "packageid": "924fc980-4530-11e9-ae68-acc860114657",
                "vendor": "ZTE",
                "type": "vCPE",
                "vnfdfile": "MRP6600_FS_SRIOV_4NIC_200W.zip",
                "imagefiles": ["MRP6600_FS_SRIOV_MRPISU_IMGV500R008C20SPC030T.tar"],
                "swfiles": ["MRP6600_SRV_V500R008C20SPC030T.tar"],
                "description": "This is a service for vCPE.",
            }]
        }
        return Response(data=resp_data, status=status.HTTP_200_OK)


class VnfPkg(APIView):
    @swagger_auto_schema(
        responses={
            status.HTTP_200_OK: "File stream for vnf pkg file",
            status.HTTP_500_INTERNAL_SERVER_ERROR: "Internal error"
        }
    )
    def get(self, request, packageId, fileName):
        logger.debug("====VnfPkg get====%s, %s", packageId, fileName)
        file_range = request.META.get('RANGE')
        logger.debug('file_range: %s' % file_range)
        # TODO: get filepath
        local_file_path = fileName
        start, end = parse_file_range(local_file_path, file_range)
        file_iterator = read(local_file_path, start, end)
        return StreamingHttpResponse(file_iterator, status=status.HTTP_200_OK)


class NfvoInfo(APIView):
    @swagger_auto_schema(
        request_body=NfvoInfoReqSerializer(),
        responses={
            status.HTTP_200_OK: "Update successfully",
            status.HTTP_500_INTERNAL_SERVER_ERROR: "Internal error"
        }
    )
    def put(self, request, vnfmid):
        logger.debug("====NfvoInfo put====%s", vnfmid)
        req_data = {
            "nfvoid": request.data.get("nfvoid", "1"),
            "vnfmid": vnfmid,
            "nfvourl": request.data.get("nfvourl", "http://127.0.0.1:80")
        }
        ret = get_vnfminfo_from_nslcm(vnfmid)
        if ret[0] != 0:
            return Response(data={'error': ret[1]}, status=status.HTTP_500_INTERNAL_SERVER_ERROR)

        vnfm_info = json.JSONDecoder().decode(ret[1])
        logger.debug("[%s] vnfm_info=%s", fun_name(), vnfm_info)
        ret = restcall.call_req(
            base_url=ignorcase_get(vnfm_info, "url"),
            user=ignorcase_get(vnfm_info, "userName"),
            passwd=ignorcase_get(vnfm_info, "password"),
            auth_type=restcall.rest_no_auth,
            resource="v1/nfvo/info",
            method='put',
            content=json.dumps(req_data))
        if ret[0] != 0:
            return Response(data={'error': ret[1]}, status=status.HTTP_500_INTERNAL_SERVER_ERROR)
        logger.debug("update nfvo info successfully.")
        return Response(data={}, status=status.HTTP_200_OK)


class HealthCheckView(APIView):
    @swagger_auto_schema(
        responses={
            status.HTTP_200_OK: 'Active'})
    def get(self, request, format=None):
        logger.debug("HealthCheck")
        return Response({"status": "active"})
