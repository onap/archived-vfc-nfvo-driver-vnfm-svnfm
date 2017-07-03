/*
 * Copyright (c) 2016, Huawei Technologies Co., Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openo.nfvo.vnfmadapter.mocoserver;

import org.openo.sdno.testframework.http.model.HttpRequest;
import org.openo.sdno.testframework.http.model.HttpResponse;
import org.openo.sdno.testframework.http.model.HttpRquestResponse;
import org.openo.sdno.testframework.moco.MocoHttpServer;
import org.openo.sdno.testframework.moco.responsehandler.MocoResponseHandler;

/**
 * <br/>
 * <p>
 * </p>
 * 
 * @author
 * @version NFVO 0.5 Aug 2, 2016
 */
public class VnfmAdapterSuccessServer extends MocoHttpServer {

    private static final String GET_ALL_CLOUD_FILE =
            "src/integration-test/resources/vnfmadapter/mocoserver/getallcloudinfo.json";

    private static final String ADD_AUTH_INFO_FILE =
            "src/integration-test/resources/vnfmadapter/mocoserver/addauthinfo.json";

    private static final String GRANT_VNF_RES_FILE =
            "src/integration-test/resources/vnfmadapter/mocoserver/grantvnfresource.json";

    private static final String GET_CSAR_INFO_FILE =
            "src/integration-test/resources/vnfmadapter/mocoserver/getcsarinfo.json";

    private static final String GET_VNFM_INFO_FILE =
            "src/integration-test/resources/vnfmadapter/mocoserver/getvnfminfo.json";

    private static final String UPLOAD_VNF_INFO_FILE =
            "src/integration-test/resources/vnfmadapter/mocoserver/uploadvnfpackage.json";

    private static final String GET_VNFD_VER_FILE =
            "src/integration-test/resources/vnfmadapter/mocoserver/getvnfdversion.json";

    private static final String GET_VNFD_PLAN_FILE =
            "src/integration-test/resources/vnfmadapter/mocoserver/getvnfdplaninfo.json";

    private static final String REMOVE_VNF_FILE =
            "src/integration-test/resources/vnfmadapter/mocoserver/removevnf.json";

    private static final String GET_VNF_FILE =
            "src/integration-test/resources/vnfmadapter/mocoserver/getvnf.json";

    private static final String GET_AUTH_INFO_FILE =
            "src/integration-test/resources/vnfmadapter/mocoserver/getauthinfo.json";

    public VnfmAdapterSuccessServer() {
        super();
    }

    public VnfmAdapterSuccessServer(int port) {
        super(port);
    }

    @Override
    public void addRequestResponsePairs() {
        this.addRequestResponsePair(GET_ALL_CLOUD_FILE);
        this.addRequestResponsePair(ADD_AUTH_INFO_FILE);
        this.addRequestResponsePair(GRANT_VNF_RES_FILE);
        this.addRequestResponsePair(GET_CSAR_INFO_FILE);
        this.addRequestResponsePair(GET_VNFM_INFO_FILE);
        this.addRequestResponsePair(UPLOAD_VNF_INFO_FILE);
        this.addRequestResponsePair(GET_VNFD_VER_FILE);
        this.addRequestResponsePair(GET_VNFD_PLAN_FILE);
        this.addRequestResponsePair(REMOVE_VNF_FILE);
        this.addRequestResponsePair(GET_VNF_FILE);
        this.addRequestResponsePair(GET_AUTH_INFO_FILE);
    }

    private class CreateVimResponseHandler extends MocoResponseHandler {

        @Override
        public void processRequestandResponse(HttpRquestResponse httpObject) {
        	System.out.println("***********************");
        	System.out.println(httpObject);
        	System.out.println("***********************");
            HttpRequest httpRequest = httpObject.getRequest();
            HttpResponse httpResponse = httpObject.getResponse();
        }
    }

}
