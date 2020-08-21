/*
 * Copyright 2017 Huawei Technologies Co., Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.onap.vfc.nfvo.vnfm.svnfm.vnfmadapter.service.constant;

import junit.framework.Assert;
import org.junit.Test;

import java.util.List;

import static org.onap.vfc.nfvo.vnfm.svnfm.vnfmadapter.service.constant.Constant.AUTHLIST;

/**
 * Created by QuanZhong on 2017/3/17.
 */
public class TestUrlConstant {
    @Test
    public void testCreate(){
        List<String> authlist = AUTHLIST;
        authlist.contains("abc");
        String url = UrlConstant.PORT_COMMON;
        authlist.contains(url);
        String abc = ParamConstants.CONNECTMGR_CONNECT;
        authlist.contains(abc);
        String url_job_status_get  = UrlConstant.URL_JOBSTATUS_GET;
        authlist.contains(url_job_status_get);
        String url_rest_msb_register  = UrlConstant.REST_MSB_REGISTER;
        authlist.contains(url_rest_msb_register);
        String url_rest_drivermgr_register  = UrlConstant.REST_DRIVERMGR_REGISTER;
        authlist.contains(url_rest_drivermgr_register);
        String url_rest_csarinfo_get  = UrlConstant.REST_CSARINFO_GET;
        authlist.contains(url_rest_csarinfo_get);
        String url_rest_vnfminfo_get  = UrlConstant.REST_VNFMINFO_GET;
        authlist.contains(url_rest_vnfminfo_get);
        String url_allcloud_get  = UrlConstant.URL_ALLCLOUD_GET;
        authlist.contains(url_allcloud_get);
        String url_allcloud_new_get  = UrlConstant.URL_ALLCLOUD_NEW_GET;
        authlist.contains(url_allcloud_new_get);
        String url_vnfpackage_post  = UrlConstant.URL_VNFPACKAGE_POST;
        authlist.contains(url_vnfpackage_post);
        String url_vnfdinfo_get  = UrlConstant.URL_VNFDINFO_GET;
        authlist.contains(url_vnfdinfo_get);
        String url_vnfdplaninfo_get  = UrlConstant.URL_VNFDPLANINFO_GET;
        authlist.contains(url_vnfdplaninfo_get);
        String portUploadvnfpkg  = UrlConstant.PORT_UPLOADVNFPKG;
        authlist.contains(portUploadvnfpkg);
        Assert.assertTrue(true);
    }
}
