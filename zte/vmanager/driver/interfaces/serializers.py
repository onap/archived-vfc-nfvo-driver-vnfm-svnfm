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


class AffectedvmSerializer(serializers.Serializer):
    extention = serializers.CharField(
        help_text="extention",
        max_length=255,
        required=True, allow_blank=True)
    vmid = serializers.CharField(
        help_text="vmid",
        max_length=255,
        required=True,
        allow_null=False)
    changtype = serializers.CharField(
        help_text="changtype",
        max_length=255,
        required=False,
        allow_null=True)
    vduid = serializers.CharField(
        help_text="vduid",
        max_length=255,
        required=True,
        allow_null=False)
    vmname = serializers.CharField(
        help_text="vmname",
        max_length=255,
        required=False,
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
        allow_null=True)
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
    affectedvm = AffectedvmSerializer(help_text='affectedvm', many=True)


class HealRespSerializer(serializers.Serializer):
    jobid = serializers.CharField(
        help_text="jobid",
        max_length=255,
        required=True,
        allow_null=True)
    nfInstanceId = serializers.CharField(
        help_text="nfInstanceId",
        max_length=255,
        required=True,
        allow_null=False)
