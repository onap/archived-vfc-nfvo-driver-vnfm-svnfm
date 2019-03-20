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

from django.conf.urls import url

from driver.interfaces import views

urlpatterns = [
    url(r'^api/ztevnfmdriver/v1/(?P<vnfmid>[0-9a-zA-Z\-\_]+)/vnfs$', views.InstantiateVnf.as_view(), name='instantiate_vnf'),
    url(r'^api/ztevnfmdriver/v1/(?P<vnfmid>[0-9a-zA-Z\-\_]+)/vnfs/(?P<vnfInstanceId>[0-9a-zA-Z\-\_]+)/terminate$',
        views.TerminateVnf.as_view(), name='terminate_vnf'),
    url(r'^api/ztevnfmdriver/v1/(?P<vnfmid>[0-9a-zA-Z\-\_]+)/vnfs/(?P<vnfInstanceId>[0-9a-zA-Z\-\_]+)$',
        views.QueryVnf.as_view(), name='query_vnf'),
    url(r'^api/ztevnfmdriver/v1/(?P<vnfmid>[0-9a-zA-Z\-\_]+)/jobs/(?P<jobid>[0-9a-zA-Z\-\_]+)$',
        views.JobView.as_view(), name='operation_status'),
    url(r'^api/ztevnfmdriver/v1/resource/grant$', views.GrantVnf.as_view(), name='grantvnf'),
    url(r'^api/ztevnfmdriver/v1/vnfs/lifecyclechangesnotification$', views.Notify.as_view(), name='notify'),
    url(r'^api/ztevnfmdriver/v1/(?P<vnfmid>[0-9a-zA-Z\-\_]+)/vnfs/(?P<vnfInstanceId>[0-9a-zA-Z\-\_]+)/scale$',
        views.Scale.as_view(), name='scale'),
    url(r'^api/ztevnfmdriver/v1/(?P<vnfmid>[0-9a-zA-Z\-\_]+)/vnfs/(?P<vnfInstanceId>[0-9a-zA-Z\-\_]+)/heal$',
        views.Heal.as_view(), name='heal'),
    url(r'^api/ztevnfmdriver/v1/(?P<vnfmid>[0-9a-zA-Z\-\_]+)/vnfs/(?P<vnfInstanceId>[0-9a-zA-Z\-\_]+)/heal$',
        views.Heal.as_view(), name='heal'),
    url(r'^api/ztevnfmdriver/v1/subscribe/(?P<subscribeId>[0-9a-zA-Z\-\_]+)$', views.SubscribeDetail.as_view(), name='subscribe_detail'),
    url(r'^api/ztevnfmdriver/v1/subscribe$', views.Subscribe.as_view(), name='subscribe'),
    url(r'^api/ztevnfmdriver/v1/vnfpkgs$', views.VnfPkgs.as_view(), name='VnfPkgs'),
    url(r'^samples/$', views.SampleList.as_view(), name='samples')
]
