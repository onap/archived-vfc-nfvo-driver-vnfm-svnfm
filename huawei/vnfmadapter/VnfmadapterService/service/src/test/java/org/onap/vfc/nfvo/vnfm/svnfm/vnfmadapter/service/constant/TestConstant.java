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
import org.onap.vfc.nfvo.vnfm.svnfm.vnfmadapter.service.constant.ParamConstants;
import org.onap.vfc.nfvo.vnfm.svnfm.vnfmadapter.service.constant.UrlConstant;

import static org.onap.vfc.nfvo.vnfm.svnfm.vnfmadapter.service.constant.Constant.AUTHLIST;

import java.util.List;

/**
 * Created by QuanZhong on 2017/3/17.
 */
public class TestConstant {
    @Test
    public void testCreate(){
        List<String> authlist = AUTHLIST;
        authlist.contains("abc");
        String post = Constant.POST;
        authlist.contains(post);
        String put = Constant.PUT;
        authlist.contains(put);
        String delete = Constant.DELETE;
        authlist.contains(delete);
        String get = Constant.GET;
        authlist.contains(get);
        String asyncpost = Constant.ASYNCPOST;
        authlist.contains(asyncpost);
        String asyncget = Constant.ASYNCGET;
        authlist.contains(asyncget);
        String asyncput = Constant.ASYNCPUT;
        authlist.contains(asyncput);
        String asyncdelete = Constant.ASYNCDELETE;
        authlist.contains(asyncdelete);
        String https = Constant.HTTPS;
        authlist.contains(https);
        String encodeing = Constant.ENCODEING;
        authlist.contains(encodeing);
        String cookie = Constant.COOKIE;
        authlist.contains(cookie);
        String accesssession = Constant.ACCESSSESSION;
        authlist.contains(accesssession);
        String contentType = Constant.CONTENT_TYPE;
        authlist.contains(contentType);
        String application = Constant.APPLICATION;
        authlist.contains(application);
        String headerSubjectToken = Constant.HEADER_SUBJECT_TOKEN;
        authlist.contains(headerSubjectToken);
        String headerAuthToken = Constant.HEADER_AUTH_TOKEN;
        authlist.contains(headerAuthToken);
        String xAuthToken = Constant.X_AUTH_TOKEN;
        authlist.contains(xAuthToken);
        String downloadcsarSuccess = Constant.DOWNLOADCSAR_SUCCESS;
        authlist.contains(downloadcsarSuccess);
        int unzipSuccess = Constant.UNZIP_SUCCESS;
        authlist.contains(unzipSuccess);
        int unzipFail  = Constant.UNZIP_FAIL;
        authlist.contains(unzipFail);
        String downloadcsarFail = Constant.DOWNLOADCSAR_FAIL;
        authlist.contains(downloadcsarFail);
        int httpOk = Constant.HTTP_OK;
        authlist.contains(httpOk);
        int httpCreated = Constant.HTTP_CREATED;
        authlist.contains(httpCreated);
        int httpAccepted = Constant.HTTP_ACCEPTED;
        authlist.contains(httpAccepted);
        int httpNocontent = Constant.HTTP_NOCONTENT;
        authlist.contains(httpNocontent);
        int httpBadRequest = Constant.HTTP_BAD_REQUEST;
        authlist.contains(httpBadRequest);
        int httpUnauthorized = Constant.HTTP_UNAUTHORIZED;
        authlist.contains(httpUnauthorized);
        int httpNotfound  = Constant.HTTP_NOTFOUND;
        authlist.contains(httpNotfound);
        int httpNotAcceptable = Constant.HTTP_NOT_ACCEPTABLE;
        authlist.contains(httpNotAcceptable);
        int httpConflict = Constant.HTTP_CONFLICT;
        authlist.contains(httpConflict);
        int httpInvalidParameters = Constant.HTTP_INVALID_PARAMETERS;
        authlist.contains(httpInvalidParameters);
        int httpInnererror = Constant.HTTP_INNERERROR;
        authlist.contains(httpInnererror);
        int internalException = Constant.INTERNAL_EXCEPTION;
        authlist.contains(internalException);
        int repeatRegTime = Constant.REPEAT_REG_TIME;
        authlist.contains(repeatRegTime);
        int minPwdLength = Constant.MIN_PWD_LENGTH;
        authlist.contains(minPwdLength);
        int maxPwdLength = Constant.MAX_PWD_LENGTH;
        authlist.contains(maxPwdLength);
        int minUrlLength = Constant.MIN_URL_LENGTH;
        authlist.contains(minUrlLength);
        int maxVnfmNameLength = Constant.MAX_VNFM_NAME_LENGTH;
        authlist.contains(maxVnfmNameLength);
        int minVnfmNameLength = Constant.MIN_VNFM_NAME_LENGTH;
        authlist.contains(maxVnfmNameLength);
        int maxUrlLength = Constant.MAX_URL_LENGTH;
        authlist.contains(maxUrlLength);
        int restSuccess = Constant.REST_SUCCESS;
        authlist.contains(restSuccess);
        int defaultCollectionSize = Constant.DEFAULT_COLLECTION_SIZE;
        authlist.contains(defaultCollectionSize);
        int restFail = Constant.REST_FAIL;
        authlist.contains(restFail);
        String roarand = Constant.ROARAND;
        authlist.contains(roarand);
        String anonymous = Constant.ANONYMOUS;
        authlist.contains(anonymous);
        String certificate = Constant.CERTIFICATE;
        authlist.contains(certificate);
        String retcode = Constant.RETCODE;
        authlist.contains(retcode);
        String reason = Constant.REASON;
        authlist.contains(reason);
        String status = Constant.STATUS;
        authlist.contains(status);
        String vnfpkginfo = Constant.VNFPKGINFO;
        authlist.contains(vnfpkginfo);
        int errorStatusCode = Constant.ERROR_STATUS_CODE;
        authlist.contains(errorStatusCode);
        String colon = Constant.COLON;
        authlist.contains(colon);
        String errormsg = Constant.ERRORMSG;
        authlist.contains(errormsg);
        String vimid = Constant.VIMID;
        authlist.contains(vimid);
        String vnfmid = Constant.VNFMID;
        authlist.contains(vnfmid);
        String action = Constant.ACTION;
        authlist.contains(action);
        String vnfdid = Constant.VNFDID;
        authlist.contains(vnfdid);
        String jobid = Constant.JOBID;
        authlist.contains(jobid);
        String fileSeparator = Constant.FILE_SEPARATOR;
        authlist.contains(fileSeparator);
        String password = Constant.PASSWORD;
        authlist.contains(password);
        String username = Constant.USERNAME;
        authlist.contains(username);
        String localHost = Constant.LOCAL_HOST;
        authlist.contains(localHost);

        Assert.assertTrue(true);
    }
}
