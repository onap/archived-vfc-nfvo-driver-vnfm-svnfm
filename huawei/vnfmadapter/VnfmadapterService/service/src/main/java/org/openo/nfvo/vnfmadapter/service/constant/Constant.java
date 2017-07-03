/*
 * Copyright 2016-2017 Huawei Technologies Co., Ltd.
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
package org.openo.nfvo.vnfmadapter.service.constant;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Provide constant value
 * <br/>
 * <p>
 * </p>
 *
 * @author
 * @version NFVO 0.5 Sep 3, 2016
 */
public class Constant {

    public static final String POST = "post";

    public static final String PUT = "put";

    public static final String DELETE = "delete";

    public static final String GET = "get";

    public static final String ASYNCPOST = "asyncPost";

    public static final String ASYNCGET = "asyncGet";

    public static final String ASYNCPUT = "asyncPut";

    public static final String ASYNCDELETE = "asyncDelete";

    public static final String ENCODEING = "utf-8";

    public static final String COOKIE = "Cookie";

    public static final String ACCESSSESSION = "accessSession";

    public static final String CONTENT_TYPE = "Content-Type";

    public static final String APPLICATION = "application/json";

    public static final String HEADER_SUBJECT_TOKEN = "X-Subject-Token";

    public static final String HEADER_AUTH_TOKEN = "accessSession";

    public static final String DOWNLOADCSAR_SUCCESS = "Success";

    public static final int UNZIP_SUCCESS = 0;

    public static final int UNZIP_FAIL = -1;

    public static final String DOWNLOADCSAR_FAIL = "FAIL";

    public static final int HTTP_OK = 200;

    public static final int HTTP_CREATED = 201;

    public static final int HTTP_ACCEPTED = 202;

    public static final int HTTP_NOCONTENT = 204;

    public static final int HTTP_BAD_REQUEST = 400;

    public static final int HTTP_UNAUTHORIZED = 401;

    public static final int HTTP_NOTFOUND = 404;

    public static final int HTTP_NOT_ACCEPTABLE = 406;

    public static final int HTTP_CONFLICT = 409;

    public static final int HTTP_INVALID_PARAMETERS = 415;

    public static final int HTTP_INNERERROR = 500;

    public static final List<String> AUTHLIST = Collections.unmodifiableList(Arrays.asList(Constant.ANONYMOUS, Constant.CERTIFICATE));

    public static final int INTERNAL_EXCEPTION = 600;

    public static final int REPEAT_REG_TIME = 60 * 1000;

    public static final int MIN_PWD_LENGTH = 6;

    public static final int MAX_PWD_LENGTH = 160;

    public static final int MIN_URL_LENGTH = 7;

    public static final int MAX_VNFM_NAME_LENGTH = 64;

    public static final int MIN_VNFM_NAME_LENGTH = 1;

    public static final int MAX_URL_LENGTH = 256;

    public static final int REST_SUCCESS = 1;

    public static final int DEFAULT_COLLECTION_SIZE = 10;

    public static final int REST_FAIL = -1;

    public static final String ROARAND = "?roarand=%s";

    public static final String ANONYMOUS = "Anonymous";

    public static final String CERTIFICATE = "Certificate";

    public static final String RETCODE = "retCode";

    public static final String STATUS = "status";

    public static final String VNFPKGINFO="vnfpkginfo.json";

    public static final int ERROR_STATUS_CODE = -1;
    
    public static final String COLON=":";

    private Constant() {
        //private constructor
    }
}
