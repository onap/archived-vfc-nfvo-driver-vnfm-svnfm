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
import traceback
import os

from rest_framework.decorators import api_view
from rest_framework.response import Response

from driver.pub.utils import restcall
from driver.pub.utils.restcall import req_by_msb
from driver.pub.config.config import VNF_FTP

logger = logging.getLogger(__name__)


def load_json_file(file_name):
    json_file = os.path.join(os.path.dirname(__file__), "data/" + file_name)
    f = open(json_file)
    json_data = json.JSONDecoder().decode(f.read())
    f.close()
    return json_data


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


def mapping_conv(keyword_map, rest_return):
    resp_data = {}
    for param in keyword_map:
        if keyword_map[param]:
            resp_data[keyword_map[param]] = ignorcase_get(rest_return, param)
    return resp_data


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


@api_view(http_method_names=['POST'])
def instantiate_vnf(request, *args, **kwargs):
    try:
        logger.debug("[%s] request.data=%s", fun_name(), request.data)
        vnfm_id = ignorcase_get(kwargs, "vnfmid")
        ret = get_vnfminfo_from_nslcm(vnfm_id)
        if ret[0] != 0:
            return Response(data={'error': ret[1]}, status=ret[2])
        vnfm_info = json.JSONDecoder().decode(ret[1])
        logger.debug("[%s] vnfm_info=%s", fun_name(), vnfm_info)
        vnf_package_id = ignorcase_get(request.data, "vnfPackageId")
        ret = vnfd_get(vnf_package_id)
        if ret[0] != 0:
            return Response(data={'error': ret[1]}, status=ret[2])
        vnfd_info = json.JSONDecoder().decode(ret[1])
        logger.debug("[%s] vnfd_info=%s", fun_name(), vnfd_info)
        csar_id = ignorcase_get(vnfd_info, "csarId")
        ret = vnfpackage_get(csar_id)
        if ret[0] != 0:
            return Response(data={'error': ret[1]}, status=ret[2])
        vnf_package_info = json.JSONDecoder().decode(ret[1])
        packageInfo = ignorcase_get(vnf_package_info, "packageInfo")
        logger.debug("[%s] packageInfo=%s", fun_name(), packageInfo)
        data = {}
        data["NFVOID"] = 1
        data["VNFMID"] = vnfm_id
        vnfdModel = json.loads(ignorcase_get(packageInfo, "vnfdModel"))
        metadata = ignorcase_get(vnfdModel, "metadata")
        vnfd_name = ignorcase_get(metadata, "name")
        # TODO  convert sdc vnf package to vnf vender package
        inputs = []
        if "SPGW" in vnfd_name.upper():
            data["VNFD"] = VNF_FTP + "SPGW"
            inputs = load_json_file("SPGW" + "_inputs.json")
        elif "MME" in vnfd_name.upper():
            data["VNFD"] = VNF_FTP + "MME"
            inputs = load_json_file("MME" + "_inputs.json")
        else:
            data["VNFD"] = ignorcase_get(packageInfo, "downloadUri")

        data["VNFURL"] = data["VNFD"]

        data["extension"] = {}
        for name, value in ignorcase_get(ignorcase_get(request.data, "additionalParam"), "inputs").items():
            inputs.append({"name": name, "value": value})

        data["extension"]["inputs"] = json.dumps(inputs)
        additionalParam = ignorcase_get(request.data, "additionalParam")
        data["extension"]["extVirtualLinks"] = ignorcase_get(additionalParam, "extVirtualLinks")
        data["extension"]["vnfinstancename"] = ignorcase_get(request.data, "vnfInstanceName")
        data["extension"]["vnfid"] = data["VNFD"]
        data["extension"]["multivim"] = 0
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
            return Response(data={'error': ret[1]}, status=ret[2])
        resp = json.JSONDecoder().decode(ret[1])
        resp_data = {
            "vnfInstanceId": ignorcase_get(resp, "VNFInstanceID"),
            "jobId": ignorcase_get(resp, "JobId")
        }
        logger.info("[%s]resp_data=%s", fun_name(), resp_data)
    except Exception as e:
        logger.error("Error occurred when instantiating VNF")
        raise e
    return Response(data=resp_data, status=ret[2])


@api_view(http_method_names=['POST'])
def terminate_vnf(request, *args, **kwargs):
    try:
        logger.debug("[%s] request.data=%s", fun_name(), request.data)
        vnfm_id = ignorcase_get(kwargs, "vnfmid")
        ret = get_vnfminfo_from_nslcm(vnfm_id)
        if ret[0] != 0:
            return Response(data={'error': ret[1]}, status=ret[2])
        vnfm_info = json.JSONDecoder().decode(ret[1])
        logger.debug("[%s] vnfm_info=%s", fun_name(), vnfm_info)
        ret = restcall.call_req(
            base_url=ignorcase_get(vnfm_info, "url"),
            user=ignorcase_get(vnfm_info, "userName"),
            passwd=ignorcase_get(vnfm_info, "password"),
            auth_type=restcall.rest_no_auth,
            resource="v1/vnfs/%s" % (ignorcase_get(kwargs, "vnfInstanceID")),
            method='delete',
            content=json.JSONEncoder().encode(request.data))
        if ret[0] != 0:
            return Response(data={'error': ret[1]}, status=ret[2])
        resp = json.JSONDecoder().decode(ret[1])
        resp_data = {
            "vnfInstanceId": ignorcase_get(resp, "VNFInstanceID"),
            "jobId": ignorcase_get(resp, "JobId")
        }
        logger.debug("[%s]resp_data=%s", fun_name(), resp_data)
    except Exception as e:
        logger.error("Error occurred when terminating VNF")
        raise e
    return Response(data=resp_data, status=ret[2])


vnf_detail_url = "v1/vnfs/%s"
vnf_detail_resp_mapping = {
    "VNFInstanseStatus": "status"
}


@api_view(http_method_names=['GET'])
def query_vnf(request, *args, **kwargs):
    try:
        logger.debug("[%s] request.data=%s", fun_name(), request.data)
        vnfm_id = ignorcase_get(kwargs, "vnfmid")
        ret = get_vnfminfo_from_nslcm(vnfm_id)
        if ret[0] != 0:
            return Response(data={'error': ret[1]}, status=ret[2])
        vnfm_info = json.JSONDecoder().decode(ret[1])
        logger.debug("[%s] vnfm_info=%s", fun_name(), vnfm_info)
        data = {}
        ret = restcall.call_req(
            base_url=ignorcase_get(vnfm_info, "url"),
            user=ignorcase_get(vnfm_info, "userName"),
            passwd=ignorcase_get(vnfm_info, "password"),
            auth_type=restcall.rest_no_auth,
            resource=vnf_detail_url % (ignorcase_get(kwargs, "vnfInstanceID")),
            method='get',
            content=json.JSONEncoder().encode(data))
        if ret[0] != 0:
            return Response(data={'error': ret[1]}, status=ret[2])
        resp = json.JSONDecoder().decode(ret[1])
        vnf_status = ignorcase_get(resp, "vnfinstancestatus")
        resp_data = {"vnfInfo": {"vnfStatus": vnf_status}}
        logger.debug("[%s]resp_data=%s", fun_name(), resp_data)
    except Exception as e:
        logger.error("Error occurred when querying VNF information.")
        raise e
    return Response(data=resp_data, status=ret[2])


# Get Operation Status
operation_status_url = '/v1/jobs/{jobId}?NFVOID={nfvoId}&VNFMID={vnfmId}&ResponseID={responseId}'
operation_status_resp_map = {
    "JobId": "jobId",
    "Status": "status",
    "Progress": "progress",
    "StatusDescription": "currentStep",
    "ErrorCode": "errorCode",
    "ResponseId": "responseId",
    "ResponseHistoryList": "responseHistoryList",
    "ResponseDescriptor": "responseDescriptor"
}


@api_view(http_method_names=['GET'])
def operation_status(request, *args, **kwargs):
    data = {}
    try:
        logger.debug("[%s] request.data=%s", fun_name(), request.data)
        vnfm_id = ignorcase_get(kwargs, "vnfmid")
        ret = get_vnfminfo_from_nslcm(vnfm_id)
        if ret[0] != 0:
            return Response(data={'error': ret[1]}, status=ret[2])
        vnfm_info = json.JSONDecoder().decode(ret[1])
        logger.debug("[%s] vnfm_info=%s", fun_name(), vnfm_info)
        ret = restcall.call_req(
            base_url=ignorcase_get(vnfm_info, 'url'),
            user=ignorcase_get(vnfm_info, 'userName'),
            passwd=ignorcase_get(vnfm_info, 'password'),
            auth_type=restcall.rest_no_auth,
            resource=operation_status_url.format(jobId=ignorcase_get(kwargs, 'jobid'), nfvoId=1,
                                                 vnfmId=ignorcase_get(kwargs, 'vnfmid'),
                                                 responseId=ignorcase_get(request.GET, 'responseId')),
            method='get',
            content=json.JSONEncoder().encode(data))

        if ret[0] != 0:
            return Response(data={'error': ret[1]}, status=ret[2])
        resp_data = json.JSONDecoder().decode(ret[1])
        logger.info("[%s]resp_data=%s", fun_name(), resp_data)
    except Exception as e:
        logger.error("Error occurred when getting operation status information.")
        raise e
    return Response(data=resp_data, status=ret[2])


# Grant VNF Lifecycle Operation
grant_vnf_url = 'api/nslcm/v1/ns/grantvnf'
grant_vnf_param_map = {
    "VNFMID": "",
    "NFVOID": "",
    "VIMID": "",
    "ExVIMIDList": "",
    "ExVIMID": "",
    "Tenant": "",
    "VNFInstanceID": "vnfInstanceId",
    "OperationRight": "",
    "VMList": "",
    "VMFlavor": "",
    "VMNumber": ""}


@api_view(http_method_names=['PUT'])
def grantvnf(request, *args, **kwargs):
    logger.info("=====grantvnf=====")
    try:
        resp_data = {}
        logger.info("req_data = %s", request.data)
        data = mapping_conv(grant_vnf_param_map, request.data)
        logger.info("grant_vnf_url = %s", grant_vnf_url)
        data["vnfDescriptorId"] = ""
        if ignorcase_get(request.data, "operationright") == 0:
            data["lifecycleOperation"] = "Instantiate"
            data["addresource"] = []
            for vm in ignorcase_get(request.data, "vmlist"):
                for i in range(int(ignorcase_get(vm, "vmnumber"))):
                    data["addresource"].append(
                        {"type": "vdu",
                         "resourceDefinitionId": i,
                         "vdu": ignorcase_get(vm, "vmflavor"),
                         "vimid": ignorcase_get(vm, "vimid"),
                         "tenant": ignorcase_get(vm, "tenant")
                         })

        data["additionalparam"] = {}
        data["additionalparam"]["vnfmid"] = ignorcase_get(request.data, "vnfmid")
        data["additionalparam"]["vimid"] = ignorcase_get(request.data, "vimid")
        data["additionalparam"]["tenant"] = ignorcase_get(request.data, "tenant")

        logger.info("data = %s", data)
        ret = req_by_msb(grant_vnf_url, "POST", content=json.JSONEncoder().encode(data))
        logger.info("ret = %s", ret)
        if ret[0] != 0:
            return Response(data={'error': ret[1]}, status=ret[2])
        resp = json.JSONDecoder().decode(ret[1])

        resp_data['vimid'] = ignorcase_get(resp['vim'], 'vimid')
        resp_data['tenant'] = ignorcase_get(ignorcase_get(resp['vim'], 'accessinfo'), 'tenant')

        logger.info("[%s]resp_data=%s", fun_name(), resp_data)
    except Exception as e:
        logger.error("Error occurred in Grant VNF.")
        raise e
    return Response(data=resp_data, status=ret[2])


# Notify LCM Events
notify_url = 'api/nslcm/v1/ns/{vnfmid}/vnfs/{vnfInstanceId}/Notify'
notify_param_map = {
    "NFVOID": "",
    "VNFMID": "VNFMID",
    "VIMID": "vimid",
    "VNFInstanceID": "vnfInstanceId",
    "TimeStamp": "",
    "EventType": "operation",
    "VMList": "",
    "VMFlavor": "",
    "VMNumber": "",
    "VMIDlist": "",
    "VMUUID": ""
}


@api_view(http_method_names=['POST'])
def notify(request, *args, **kwargs):
    try:
        logger.info("[%s]req_data = %s", fun_name(), request.data)
        data = mapping_conv(notify_param_map, request.data)
        logger.info("[%s]data = %s", fun_name(), data)

        data["status"] = "result"
        data["jobId"] = "notMust"
        data["affectedVnfc"] = []
        data["affectedVl"] = []
        data["affectedVirtualStorage"] = []
        data["affectedCp"] = []

        extension = ignorcase_get(request.data, "extension")
        openo_notification = ignorcase_get(extension, "openo_notification")
        if openo_notification:
            affectedvnfcs = ignorcase_get(openo_notification, "affectedVnfc")
            affectedvls = ignorcase_get(openo_notification, "affectedvirtuallink")
            affectedcps = ignorcase_get(openo_notification, "affectedCp")
            vnfdmodule = ignorcase_get(openo_notification, "vnfdmodule")
        else:
            affectedvnfcs = ignorcase_get(ignorcase_get(request.data, "extension"), "affectedvnfc")
            affectedvls = ignorcase_get(ignorcase_get(request.data, "extension"), "affectedvl")
            affectedcps = ignorcase_get(ignorcase_get(request.data, "extension"), "affectedcp")
            vnfdmodule = ignorcase_get(ignorcase_get(request.data, "extension"), "vnfdmodule")

        data["vnfdmodule"] = vnfdmodule

        for affectedvnfc in affectedvnfcs:
            data["affectedVnfc"].append({
                "vnfcInstanceId": ignorcase_get(affectedvnfc, "vnfcInstanceId"),
                "vduId": ignorcase_get(affectedvnfc, "vduId"),
                "changeType": ignorcase_get(affectedvnfc, "changeType"),
                "vimid": ignorcase_get(ignorcase_get(affectedvnfc, "computeResource"), "vimId"),
                "vmId": ignorcase_get(ignorcase_get(affectedvnfc, "computeResource"), "resourceId"),
                "vmName": ignorcase_get(ignorcase_get(affectedvnfc, "computeResource"), "resourceName")
            })

        for affectedvl in affectedvls:
            data["affectedVl"].append({
                "vlInstanceId": ignorcase_get(affectedvl, "virtuallinkinstanceid"),
                "vimid": ignorcase_get(ignorcase_get(affectedvl, "networkresource"), "vimid"),
                "vldid": ignorcase_get(affectedvl, "virtuallinkdescid"),
                "vllid": ignorcase_get(ignorcase_get(affectedvl, "networkresource"), "resourceid"),
                "vlName": ignorcase_get(ignorcase_get(affectedvl, "networkresource"), "resourcename")
            })

        for affectedcp in affectedcps:
            data["affectedCp"].append(affectedcp)

        ret = req_by_msb(notify_url.format(vnfmid=ignorcase_get(data, 'VNFMID'),
                                           vnfInstanceId=ignorcase_get(data, 'vnfinstanceid')),
                         "POST", content=json.JSONEncoder().encode(data))

        logger.info("[%s]data = %s", fun_name(), ret)
        if ret[0] != 0:
            return Response(data={'error': ret[1]}, status=ret[2])
    except Exception as e:
        logger.error("Error occurred in LCM notification.")
        logger.error(traceback.format_exc())
        raise e
    return Response(data=None, status=ret[2])


nf_scaling_url = '/v1/vnfs/{vnfInstanceID}/scale'


@api_view(http_method_names=['POST'])
def scale(request, *args, **kwargs):
    logger.info("====scale_vnf===")
    try:
        logger.info("request.data = %s", request.data)
        logger.info("requested_url = %s", request.get_full_path())
        vnfm_id = ignorcase_get(kwargs, "vnfmid")
        nf_instance_id = ignorcase_get(kwargs, "vnfInstanceId")
        ret = get_vnfminfo_from_nslcm(vnfm_id)
        if ret[0] != 0:
            return Response(data={'error': ret[1]}, status=ret[2])
        vnfm_info = json.JSONDecoder().decode(ret[1])
        scale_type = ignorcase_get(request.data, "type")
        aspect_id = ignorcase_get(request.data, "aspectId")
        number_of_steps = ignorcase_get(request.data, "numberOfSteps")
        # extension = ignorcase_get(request.data, "additionalParam")
        # vnfd_model = ignorcase_get(extension, "vnfdModel")
        data = {
            'vnfmid': vnfm_id,
            'nfvoid': 1,
            'scaletype': '0' if scale_type == 'SCALE_OUT' else '1',
            'vmlist': [{'VMNumber': number_of_steps, 'VMFlavor': aspect_id}],
            'extension': ''
        }
        '''
        for vdu_id in get_vdus(vnfd_model, aspect_id):
            data['vmlist'].append({
                "VMFlavor": vdu_id,
                "VMNumber": number_of_steps
            })
        '''
        logger.info("data = %s", data)
        ret = restcall.call_req(
            base_url=ignorcase_get(vnfm_info, "url"),
            user=ignorcase_get(vnfm_info, "userName"),
            passwd=ignorcase_get(vnfm_info, "password"),
            auth_type=restcall.rest_no_auth,
            resource=nf_scaling_url.format(vnfInstanceID=nf_instance_id),
            method='put',  # POST
            content=json.JSONEncoder().encode(data))
        logger.info("ret=%s", ret)
        if ret[0] != 0:
            return Response(data={'error': 'scale error'}, status=ret[2])
        resp_data = json.JSONDecoder().decode(ret[1])
        logger.info("resp_data=%s", resp_data)
    except Exception as e:
        logger.error("Error occurred when scaling VNF,error:%s", e.message)
        logger.error(traceback.format_exc())
        return Response(data={'error': 'scale expection'}, status='500')
    return Response(data=resp_data, status=ret[2])


nf_healing_url = '/api/v1/nf_m_i/nfs/{vnfInstanceID}/vms/operation'


@api_view(http_method_names=['POST'])
def heal(request, *args, **kwargs):
    logger.info("====heal_vnf===")
    try:
        logger.info("request.data = %s", request.data)
        logger.info("requested_url = %s", request.get_full_path())
        vnfm_id = ignorcase_get(kwargs, "vnfmid")
        nf_instance_id = ignorcase_get(kwargs, "vnfInstanceId")
        ret = get_vnfminfo_from_nslcm(vnfm_id)
        if ret[0] != 0:
            return Response(data={'error': ret[1]}, status=ret[2])
        vnfm_info = json.JSONDecoder().decode(ret[1])
        data = {}
        data['action'] = ignorcase_get(request.data, 'action')
        affectedvm = ignorcase_get(request.data, 'affectedvm')
        data['affectedvm'] = []
        if isinstance(affectedvm, list):
            data['affectedvm'] = affectedvm
        else:
            data['affectedvm'].append(affectedvm)
        data['lifecycleoperation'] = 'operate'
        data['isgrace'] = 'force'

        logger.info("data = %s", data)
        ret = restcall.call_req(
            base_url=ignorcase_get(vnfm_info, "url"),
            user=ignorcase_get(vnfm_info, "userName"),
            passwd=ignorcase_get(vnfm_info, "password"),
            auth_type=restcall.rest_no_auth,
            resource=nf_healing_url.format(vnfInstanceID=nf_instance_id),
            method='post',
            content=json.JSONEncoder().encode(data))
        logger.info("ret=%s", ret)
        if ret[0] != 0:
            return Response(data={'error': 'heal error'}, status=ret[2])
        resp_data = json.JSONDecoder().decode(ret[1])
        logger.info("resp_data=%s", resp_data)
    except Exception as e:
        logger.error("Error occurred when healing VNF,error:%s", e.message)
        logger.error(traceback.format_exc())
        return Response(data={'error': 'heal expection'}, status='500')
    return Response(data=resp_data, status=ret[2])


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


@api_view(http_method_names=['GET'])
def samples(request, *args, **kwargs):
    return Response(data={"status": "ok"})
