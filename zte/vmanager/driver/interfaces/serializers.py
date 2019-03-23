# Copyright 2018 ZTE Corporation.
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

from rest_framework import serializers


class AdditionalParamSerializer(serializers.Serializer):
    sdncontroller = serializers.CharField(
        help_text="sdncontroller",
        required=True,
        max_length=255,
        allow_null=True)
    NatIpRange = serializers.CharField(
        help_text="NatIpRange",
        required=True,
        max_length=255,
        allow_null=True)
    m6000_mng_ip = serializers.CharField(
        help_text="m6000_mng_ip",
        required=True,
        max_length=255,
        allow_null=True)
    externalPluginManageNetworkName = serializers.CharField(
        help_text="externalPluginManageNetworkName",
        required=True,
        max_length=255,
        allow_null=True)
    location = serializers.CharField(
        help_text="location",
        required=True,
        max_length=255,
        allow_null=True)
    externalManageNetworkName = serializers.CharField(
        help_text="externalManageNetworkName",
        required=True,
        max_length=255,
        allow_null=True)
    sfc_data_network = serializers.CharField(
        help_text="sfc_data_network",
        required=True,
        max_length=255,
        allow_null=True)
    externalDataNetworkName = serializers.CharField(
        help_text="externalDataNetworkName",
        required=True,
        max_length=255,
        allow_null=True)
    inputs = serializers.DictField(
        help_text='inputs',
        child=serializers.CharField(allow_blank=True),
        required=False,
        allow_null=True)


class InstantiateVnfRequestSerializer(serializers.Serializer):
    vnfInstanceName = serializers.CharField(
        help_text="vnfInstanceName",
        required=True,
        max_length=255,
        allow_null=True)
    vnfPackageId = serializers.CharField(
        help_text="vnfPackageId",
        required=True,
        max_length=255,
        allow_null=True)
    vnfDescriptorId = serializers.CharField(
        help_text="vnfDescriptorId",
        required=True,
        max_length=255,
        allow_null=True)
    additionalParam = AdditionalParamSerializer(
        help_text="additionalParam",
        required=True,
        allow_null=True)


class TerminateVnfRequestSerializer(serializers.Serializer):
    terminationType = serializers.CharField(
        help_text="terminationType",
        required=True,
        max_length=255,
        allow_null=True)
    gracefulTerminationTimeout = serializers.IntegerField(
        help_text="gracefulTerminationTimeout",
        default=120,
        required=False)


class VnfInfoSerializer(serializers.Serializer):
    vnfStatus = serializers.CharField(
        help_text="vnfStatus",
        required=True,
        max_length=255,
        allow_null=True)


class QueryVnfResponseSerializer(serializers.Serializer):
    vnfInfo = VnfInfoSerializer(
        help_text="vnfInfo",
        required=True)


class JobHistorySerializer(serializers.Serializer):
    status = serializers.CharField(
        help_text="Status of job",
        required=True,
        allow_null=False)
    progress = serializers.IntegerField(
        help_text="Progress of job",
        required=True,
        allow_null=False)
    statusDescription = serializers.CharField(
        help_text="Description of job",
        required=False,
        allow_null=True)
    errorCode = serializers.CharField(
        help_text="Error code of job",
        required=False,
        allow_blank=True)
    responseId = serializers.IntegerField(
        help_text="Response index of job",
        required=True,
        allow_null=False)


class JobDescriptorSerializer(serializers.Serializer):
    status = serializers.CharField(
        help_text="Status of job",
        required=True,
        allow_null=False)
    progress = serializers.IntegerField(
        help_text="Progress of job",
        required=True,
        allow_null=False)
    statusDescription = serializers.CharField(
        help_text="Description of job",
        required=False,
        allow_null=True)
    errorCode = serializers.CharField(
        help_text="Error code of job",
        required=False,
        allow_blank=True)
    responseId = serializers.IntegerField(
        help_text="Response index of job",
        required=True,
        allow_null=False)
    responseHistoryList = JobHistorySerializer(
        help_text="History of job",
        many=True)


class JobQueryRespSerializer(serializers.Serializer):
    jobId = serializers.CharField(
        help_text="UUID of job",
        required=True,
        allow_null=False)
    responseDescriptor = JobDescriptorSerializer(
        help_text="Descriptor of job",
        required=False)


class GrantVmlistSerializer(serializers.Serializer):
    VMNumber = serializers.CharField(
        help_text="VMNumber",
        max_length=255,
        required=False,
        allow_null=True)
    VMFlavor = serializers.CharField(
        help_text="VMFlavor",
        max_length=255,
        required=False,
        allow_null=True)
    vimid = serializers.CharField(
        help_text="vimid",
        max_length=255,
        required=True,
        allow_blank=True)
    tenant = serializers.CharField(
        help_text="tenant",
        max_length=255,
        required=False,
        allow_blank=True)


class GrantReqSerializer(serializers.Serializer):
    nfvoid = serializers.CharField(
        help_text="nfvoid",
        max_length=255,
        required=True,
        allow_null=True)
    vnfmid = serializers.CharField(
        help_text="vnfmid",
        max_length=255,
        required=True,
        allow_null=True)
    vimid = serializers.CharField(
        help_text="vimid",
        max_length=255,
        required=True,
        allow_null=True)
    tenant = serializers.CharField(
        help_text="tenant",
        max_length=255,
        required=False,
        allow_blank=True)
    vnfinstanceid = serializers.CharField(
        help_text="vnfinstanceid",
        max_length=255,
        required=False,
        allow_null=True)
    operationright = serializers.CharField(
        help_text="operationright",
        max_length=255,
        required=False,
        allow_null=True)
    vmlist = GrantVmlistSerializer(
        help_text='vmlist',
        required=False,
        many=True)
    exvimidlist = serializers.ListSerializer(
        help_text='exvimidlist',
        child=serializers.CharField(allow_null=True),
        required=False)


class GrantRespSerializer(serializers.Serializer):
    vimid = serializers.CharField(
        help_text="vimid",
        max_length=255,
        required=True,
        allow_null=True)
    tenant = serializers.CharField(
        help_text="tenant",
        max_length=255,
        required=True,
        allow_null=True)


class VMIDlistSerializer(serializers.Serializer):
    VMID = serializers.CharField(
        help_text="VMID",
        max_length=255,
        required=False,
        allow_null=True)
    VMName = serializers.CharField(
        help_text="VMName",
        max_length=255,
        required=False,
        allow_null=True)
    vimid = serializers.CharField(
        help_text="vimid",
        max_length=255,
        required=False,
        allow_null=True)
    tenant = serializers.CharField(
        help_text="tenant",
        max_length=255,
        required=False,
        allow_null=True)


class NotifyVmlistSerializer(serializers.Serializer):
    VMNumber = serializers.CharField(
        help_text="VMNumber",
        max_length=255,
        required=False,
        allow_null=True)
    vdutype = serializers.CharField(
        help_text="vdutype",
        max_length=255,
        required=False,
        allow_null=True)
    VMFlavor = serializers.CharField(
        help_text="VMFlavor",
        max_length=255,
        required=False,
        allow_null=True)
    VMIDlist = serializers.ListSerializer(
        help_text='VMIDlist',
        child=VMIDlistSerializer(help_text='VMIDlist', required=True, allow_null=True),
        required=False,
        allow_null=True)


class NotifyReqSerializer(serializers.Serializer):
    nfvoid = serializers.CharField(
        help_text="nfvoid",
        max_length=255,
        required=True,
        allow_null=True)
    vnfmid = serializers.CharField(
        help_text="vnfmid",
        max_length=255,
        required=True,
        allow_null=True)
    vimid = serializers.CharField(
        help_text="vimid",
        max_length=255,
        required=True,
        allow_null=True)
    timestamp = serializers.CharField(
        help_text="timestamp",
        max_length=255,
        required=False,
        allow_blank=True)
    vnfinstanceid = serializers.CharField(
        help_text="vnfinstanceid",
        max_length=255,
        required=False,
        allow_null=True)
    eventtype = serializers.CharField(
        help_text="eventtype",
        max_length=255,
        required=False,
        allow_null=True)
    vmlist = NotifyVmlistSerializer(
        help_text='vmlist',
        required=False,
        many=True)
    extension = serializers.DictField(
        help_text="extension",
        child=serializers.DictField(allow_null=True),
        required=False,
        allow_null=True)
    affectedcp = serializers.ListSerializer(
        help_text='affectedcp',
        child=serializers.DictField(allow_null=True),
        required=False)
    affectedvirtuallink = serializers.ListSerializer(
        help_text='affectedvirtuallink',
        child=serializers.DictField(allow_null=True),
        required=False)


class ScaleReqSerializer(serializers.Serializer):
    type = serializers.CharField(
        help_text="type",
        max_length=255,
        required=True,
        allow_null=True)
    aspectId = serializers.CharField(
        help_text="aspectId",
        max_length=255,
        required=True,
        allow_null=True)
    numberOfSteps = serializers.CharField(
        help_text="numberOfSteps",
        max_length=255,
        required=True,
        allow_null=True)
    additionalParam = serializers.DictField(
        help_text="additionalParam",
        child=serializers.DictField(allow_null=True),
        required=False,
        allow_null=True)


class AffectedvmSerializer(serializers.Serializer):
    extention = serializers.CharField(
        help_text="extention",
        max_length=255,
        required=True,
        allow_blank=True)
    vmid = serializers.CharField(
        help_text="vmid",
        max_length=255,
        required=True,
        allow_null=True)
    changtype = serializers.CharField(
        help_text="changtype",
        max_length=255,
        required=True,
        allow_null=True)
    vduid = serializers.CharField(
        help_text="vduid",
        max_length=255,
        required=True,
        allow_null=True)
    vmname = serializers.CharField(
        help_text="vmname",
        max_length=255,
        required=True,
        allow_null=True)
    flavour = serializers.DictField(
        help_text="flavour",
        child=serializers.CharField(allow_blank=True),
        required=False,
        allow_null=True)


class HealReqSerializer(serializers.Serializer):
    action = serializers.CharField(
        help_text="action",
        max_length=255,
        required=True,
        allow_null=False)
    lifecycleoperation = serializers.CharField(
        help_text="lifecycleoperation",
        max_length=255,
        required=True,
        allow_null=False)
    isgrace = serializers.CharField(
        help_text="isgrace",
        max_length=255,
        required=False,
        allow_null=True)
    affectedvm = serializers.ListSerializer(
        help_text='affectedvm',
        child=AffectedvmSerializer(help_text='affectedvm', required=True, allow_null=True),
        required=True,
        allow_null=True)


class InstScaleHealRespSerializer(serializers.Serializer):
    jobId = serializers.CharField(
        help_text="jobid",
        max_length=255,
        required=True,
        allow_null=True)
    vnfInstanceId = serializers.CharField(
        help_text="nfInstanceId",
        max_length=255,
        required=True,
        allow_null=True)


class SubscribeFilterSerializer(serializers.Serializer):
    vendor = serializers.CharField(
        help_text="vendor",
        max_length=255,
        required=True,
        allow_null=True)
    type = serializers.CharField(
        help_text="type",
        max_length=255,
        required=True,
        allow_null=True)


class SubscribeSerializer(serializers.Serializer):
    subscribeid = serializers.CharField(
        help_text="subscribeid",
        max_length=255,
        required=True,
        allow_null=True)
    notificationuri = serializers.CharField(
        help_text="notificationuri",
        max_length=255,
        required=True,
        allow_null=True)
    filter = SubscribeFilterSerializer(
        help_text="filter",
        many=True,
        required=False,
        allow_null=True)


class SubscribesRespSerializer(serializers.Serializer):
    child = SubscribeSerializer()
