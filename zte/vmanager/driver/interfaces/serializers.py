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
        help_text="SDN Controller identification",
        required=True,
        max_length=255,
        allow_null=True)
    NatIpRange = serializers.CharField(
        help_text="Nat ip address range",
        required=True,
        max_length=255,
        allow_null=True)
    m6000_mng_ip = serializers.CharField(
        help_text="M6000 management ip address",
        required=True,
        max_length=255,
        allow_null=True)
    externalPluginManageNetworkName = serializers.CharField(
        help_text="External plugin management network name",
        required=True,
        max_length=255,
        allow_null=True)
    location = serializers.CharField(
        help_text="SDN controller location",
        required=True,
        max_length=255,
        allow_null=True)
    externalManageNetworkName = serializers.CharField(
        help_text="External Management Network Name",
        required=True,
        max_length=255,
        allow_null=True)
    sfc_data_network = serializers.CharField(
        help_text="SFC data plane network",
        required=True,
        max_length=255,
        allow_null=True)
    externalDataNetworkName = serializers.CharField(
        help_text="External data plane network name",
        required=True,
        max_length=255,
        allow_null=True)
    inputs = serializers.DictField(
        help_text='Input parameters',
        child=serializers.CharField(allow_blank=True),
        required=False,
        allow_null=True)


class InstantiateVnfRequestSerializer(serializers.Serializer):
    vnfInstanceName = serializers.CharField(
        help_text="Vnf instance name",
        required=True,
        max_length=255,
        allow_null=True)
    vnfPackageId = serializers.CharField(
        help_text="Vnf package identification",
        required=True,
        max_length=255,
        allow_null=True)
    vnfDescriptorId = serializers.CharField(
        help_text="Vnf descriptor identification",
        required=True,
        max_length=255,
        allow_null=True)
    additionalParam = AdditionalParamSerializer(
        help_text="Additional parameters",
        required=True,
        allow_null=True)


class TerminateVnfRequestSerializer(serializers.Serializer):
    terminationType = serializers.CharField(
        help_text="Termination type",
        required=True,
        max_length=255,
        allow_null=True)
    gracefulTerminationTimeout = serializers.IntegerField(
        help_text="Graceful termination timeout",
        default=120,
        required=False)


class VnfInfoSerializer(serializers.Serializer):
    vnfStatus = serializers.CharField(
        help_text="Vnf status",
        required=True,
        max_length=255,
        allow_null=True)


class QueryVnfResponseSerializer(serializers.Serializer):
    vnfInfo = VnfInfoSerializer(
        help_text="Vnf instance information",
        required=True)


class JobHistorySerializer(serializers.Serializer):
    status = serializers.CharField(
        help_text="Status of the job",
        required=True,
        allow_null=False)
    progress = serializers.IntegerField(
        help_text="Progress of the job",
        required=True,
        allow_null=False)
    statusDescription = serializers.CharField(
        help_text="Description of the job",
        required=False,
        allow_null=True)
    errorCode = serializers.CharField(
        help_text="Error code of the job",
        required=False,
        allow_blank=True)
    responseId = serializers.IntegerField(
        help_text="Response index of the job",
        required=True,
        allow_null=False)


class JobDescriptorSerializer(serializers.Serializer):
    status = serializers.CharField(
        help_text="Status of the job",
        required=True,
        allow_null=False)
    progress = serializers.IntegerField(
        help_text="Progress of the job",
        required=True,
        allow_null=False)
    statusDescription = serializers.CharField(
        help_text="Description of the job",
        required=False,
        allow_null=True)
    errorCode = serializers.CharField(
        help_text="Error code of the job",
        required=False,
        allow_blank=True)
    responseId = serializers.IntegerField(
        help_text="Response index of the job",
        required=True,
        allow_null=False)
    responseHistoryList = JobHistorySerializer(
        help_text="History information of the job",
        many=True)


class JobQueryRespSerializer(serializers.Serializer):
    jobId = serializers.CharField(
        help_text="UUID of the job",
        required=True,
        allow_null=False)
    responseDescriptor = JobDescriptorSerializer(
        help_text="Information of the job",
        required=False)


class GrantVmlistSerializer(serializers.Serializer):
    VMNumber = serializers.CharField(
        help_text="The number of virtual machine",
        max_length=255,
        required=False,
        allow_null=True)
    VMFlavor = serializers.CharField(
        help_text="The flavor of virtual machines",
        max_length=255,
        required=False,
        allow_null=True)
    vimid = serializers.CharField(
        help_text="The VIM identification that virtual machines belong to",
        max_length=255,
        required=True,
        allow_blank=True)
    tenant = serializers.CharField(
        help_text="The tenant that virtual machines belong to",
        max_length=255,
        required=False,
        allow_blank=True)


class GrantReqSerializer(serializers.Serializer):
    nfvoid = serializers.CharField(
        help_text="NFVO identification",
        max_length=255,
        required=True,
        allow_null=True)
    vnfmid = serializers.CharField(
        help_text="VNFM identification",
        max_length=255,
        required=True,
        allow_null=True)
    vimid = serializers.CharField(
        help_text="VIM identification that VNF belongs to",
        max_length=255,
        required=True,
        allow_null=True)
    tenant = serializers.CharField(
        help_text="The tenant that VNF belongs to",
        max_length=255,
        required=False,
        allow_blank=True)
    vnfinstanceid = serializers.CharField(
        help_text="VNF instance identification",
        max_length=255,
        required=False,
        allow_null=True)
    operationright = serializers.CharField(
        help_text="VNF Operation right",
        max_length=255,
        required=False,
        allow_null=True)
    vmlist = GrantVmlistSerializer(
        help_text='Virtual machines of the VNF instance',
        required=False,
        many=True)
    exvimidlist = serializers.ListSerializer(
        help_text='Exclusive VIM identifications',
        child=serializers.CharField(allow_null=True),
        required=False)


class GrantRespSerializer(serializers.Serializer):
    vimid = serializers.CharField(
        help_text="VIM identification",
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
        help_text="Virtual machine identification",
        max_length=255,
        required=False,
        allow_null=True)
    VMName = serializers.CharField(
        help_text="Virtual machine name",
        max_length=255,
        required=False,
        allow_null=True)
    vimid = serializers.CharField(
        help_text="VIM identification",
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
        help_text="Virtual machine number",
        max_length=255,
        required=False,
        allow_null=True)
    vdutype = serializers.CharField(
        help_text="Virtual deployment unit type",
        max_length=255,
        required=False,
        allow_null=True)
    VMFlavor = serializers.CharField(
        help_text="Virtual machine flavor",
        max_length=255,
        required=False,
        allow_null=True)
    VMIDlist = serializers.ListSerializer(
        help_text='Virtual machine identification list',
        child=VMIDlistSerializer(
            help_text='Virtual machine identification',
            required=True,
            allow_null=True),
        required=False,
        allow_null=True)


class NotifyReqSerializer(serializers.Serializer):
    nfvoid = serializers.CharField(
        help_text="NFVO identification",
        max_length=255,
        required=True,
        allow_null=True)
    vnfmid = serializers.CharField(
        help_text="VNFM identification",
        max_length=255,
        required=True,
        allow_null=True)
    vimid = serializers.CharField(
        help_text="VIM identification",
        max_length=255,
        required=True,
        allow_null=True)
    timestamp = serializers.CharField(
        help_text="Time stamp",
        max_length=255,
        required=False,
        allow_blank=True)
    vnfinstanceid = serializers.CharField(
        help_text="VNF instance identification",
        max_length=255,
        required=False,
        allow_null=True)
    eventtype = serializers.CharField(
        help_text="Event type",
        max_length=255,
        required=False,
        allow_null=True)
    vmlist = NotifyVmlistSerializer(
        help_text='Virtual machine list',
        required=False,
        many=True)
    extension = serializers.DictField(
        help_text="Extension",
        child=serializers.DictField(
            allow_null=True),
        required=False,
        allow_null=True)
    affectedcp = serializers.ListSerializer(
        help_text='Affected connected points',
        child=serializers.DictField(
            allow_null=True),
        required=False)
    affectedvirtuallink = serializers.ListSerializer(
        help_text='Affected virtual links',
        child=serializers.DictField(
            allow_null=True),
        required=False)


class ScaleReqSerializer(serializers.Serializer):
    type = serializers.CharField(
        help_text="Scale type",
        max_length=255,
        required=True,
        allow_null=True)
    aspectId = serializers.CharField(
        help_text="Scale aspectId",
        max_length=255,
        required=True,
        allow_null=True)
    numberOfSteps = serializers.CharField(
        help_text="The number of steps",
        max_length=255,
        required=True,
        allow_null=True)
    additionalParam = serializers.DictField(
        help_text="Additional parameters",
        child=serializers.DictField(
            allow_null=True),
        required=False,
        allow_null=True)


class AffectedvmSerializer(serializers.Serializer):
    extention = serializers.CharField(
        help_text="Extension parameters",
        max_length=255,
        required=True,
        allow_blank=True)
    vmid = serializers.CharField(
        help_text="Virtual machine identification",
        max_length=255,
        required=True,
        allow_null=True)
    changtype = serializers.CharField(
        help_text="Chang type",
        max_length=255,
        required=True,
        allow_null=True)
    vduid = serializers.CharField(
        help_text="Virtual deployment unit identification",
        max_length=255,
        required=True,
        allow_null=True)
    vmname = serializers.CharField(
        help_text="Virtual machine name",
        max_length=255,
        required=True,
        allow_null=True)
    flavour = serializers.DictField(
        help_text="Virtual machine flavour",
        child=serializers.CharField(
            allow_blank=True),
        required=False,
        allow_null=True)


class HealReqSerializer(serializers.Serializer):
    action = serializers.CharField(
        help_text="Heal action",
        max_length=255,
        required=True,
        allow_null=False)
    lifecycleoperation = serializers.CharField(
        help_text="Life cycle operation",
        max_length=255,
        required=True,
        allow_null=False)
    isgrace = serializers.CharField(
        help_text="Whether the operation is grace or not",
        max_length=255,
        required=False,
        allow_null=True)
    affectedvm = serializers.ListSerializer(
        help_text='Affected virtual machine list',
        child=AffectedvmSerializer(
            help_text='Affected virtual machine',
            required=True,
            allow_null=True),
        required=True,
        allow_null=True)


class InstScaleHealRespSerializer(serializers.Serializer):
    jobId = serializers.CharField(
        help_text="The job identification",
        max_length=255,
        required=True,
        allow_null=True)
    vnfInstanceId = serializers.CharField(
        help_text="The vnf instance id",
        max_length=255,
        required=True,
        allow_null=True)


class SubscribeFilterSerializer(serializers.Serializer):
    vendor = serializers.CharField(
        help_text="The VNF vendor",
        max_length=255,
        required=True,
        allow_null=True)
    type = serializers.CharField(
        help_text="The subscription type",
        max_length=255,
        required=True,
        allow_null=True)


class SubscribeSerializer(serializers.Serializer):
    subscribeid = serializers.CharField(
        help_text="Subscription identification",
        max_length=255,
        required=True,
        allow_null=True)
    notificationuri = serializers.CharField(
        help_text="The notification URI",
        max_length=255,
        required=True,
        allow_null=True)
    filter = SubscribeFilterSerializer(
        help_text="The subscription filter",
        many=True,
        required=False,
        allow_null=True)


class SubscribesRespSerializer(serializers.Serializer):
    child = SubscribeSerializer()


class SubscribeReqSerializer(serializers.Serializer):
    nfvoid = serializers.CharField(
        help_text="NFVO identification",
        max_length=255,
        required=True,
        allow_null=True)
    vnfmid = serializers.CharField(
        help_text="VNFM identification",
        max_length=255,
        required=True,
        allow_null=True)
    notificationuri = serializers.CharField(
        help_text="The Notification URI",
        max_length=255,
        required=True,
        allow_null=True)
    filter = SubscribeFilterSerializer(
        help_text="The subscription filter",
        many=True,
        required=False,
        allow_null=True)


class SubscribeRespSerializer(serializers.Serializer):
    subscribeid = serializers.CharField(
        help_text="The subscription identification",
        max_length=255,
        required=True,
        allow_null=True)


class VnfPkgSerializer(serializers.Serializer):
    packageid = serializers.CharField(
        help_text="The package identification",
        max_length=255,
        required=False,
        allow_null=True)
    vendor = serializers.CharField(
        help_text="The VNF vendor",
        max_length=255,
        required=False,
        allow_null=True)
    type = serializers.CharField(
        help_text="THe VNF package type",
        max_length=255,
        required=False,
        allow_null=True)
    vnfdfile = serializers.CharField(
        help_text="The VNFD file",
        max_length=255,
        required=False,
        allow_null=True)
    imagefiles = serializers.ListSerializer(
        help_text='The image file list',
        child=serializers.CharField(
            help_text='The image file',
            required=True),
        required=False,
        allow_null=True)
    swfiles = serializers.ListSerializer(
        help_text='The software file list',
        child=serializers.CharField(
            help_text='THe software file',
            required=True),
        required=False,
        allow_null=True)
    description = serializers.CharField(
        help_text="The VNF package description",
        max_length=255,
        required=False,
        allow_null=True)


class VnfPkgsSerializer(serializers.Serializer):
    data = VnfPkgSerializer(
        help_text="The vnf package",
        many=True,
        required=False,
        allow_null=True)


class NfvoInfoReqSerializer(serializers.Serializer):
    nfvoid = serializers.CharField(
        help_text="NFVO identification",
        max_length=255,
        required=False,
        allow_null=True)
    vnfmid = serializers.CharField(
        help_text="VNFM identification",
        max_length=255,
        required=False,
        allow_null=True)
    nfvourl = serializers.CharField(
        help_text="NFVO URL",
        max_length=255,
        required=False,
        allow_null=True)
