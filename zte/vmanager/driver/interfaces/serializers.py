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


class VmlistSerializer(serializers.Serializer):
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
    VMIDlist = VMIDlistSerializer(help_text='VMIDlist', required=False, many=True)


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
    vmlist = VmlistSerializer(help_text='vmlist', required=False, many=True)


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
    affectedvm = AffectedvmSerializer(help_text='affectedvm', required=True, many=True)


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
