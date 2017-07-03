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

package org.openo.nfvo.vnfmadapter.service.api.internalsvc.impl;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;

import org.openo.baseservice.util.impl.SystemEnvVariablesFactory;
import org.openo.nfvo.vnfmadapter.service.adapter.impl.VnfmAdapter2DriverManager;
import org.openo.nfvo.vnfmadapter.service.adapter.inf.IVnfmAdapter2DriverManager;
import org.openo.nfvo.vnfmadapter.service.api.internalsvc.inf.IVnfmAdapter2DriverMgrService;
import org.openo.nfvo.vnfmadapter.service.constant.Constant;
import org.openo.nfvo.vnfmadapter.service.constant.UrlConstant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.json.JSONObject;

/**
 * <br>
 * <p>
 * </p>
 * 
 * @author
 * @version NFVO 0.5 Jan 23, 2017
 */
public class VnfmAdapter2DriverMgrService implements IVnfmAdapter2DriverMgrService {

    private static final Logger LOG = LoggerFactory.getLogger(VnfmAdapter2DriverMgrService.class);

    public static final String VNFMADAPTER2DRIVERMGR = "vnfmadapter2drivermgr.json";

    @Override
    public void register() {
        // set URL and mothedtype
        Map<String, String> paramsMap = new HashMap<>();
        paramsMap.put("url", UrlConstant.REST_DRIVERMGR_REGISTER);
        paramsMap.put("methodType", Constant.POST);

        // get vim adapter info and raise registration
        try {
            String adapterInfo = readVnfmAdapterInfoFromJson();
            if(!"".equals(adapterInfo)) {
                JSONObject adapterObject = JSONObject.fromObject(adapterInfo);
                RegisterVnfm2DriverMgrThread vnfmAdapterThread =
                        new RegisterVnfm2DriverMgrThread(paramsMap, adapterObject);
                Executors.newSingleThreadExecutor().submit(vnfmAdapterThread);
            } else {
                LOG.error("vnfmadapter2drivermgr info is null,please check!");
            }

        } catch(IOException e) {
            LOG.error("Failed to read vnfmadapter2drivermgr info! " + e.getMessage(), e);
        }

    }

    /**
     * Retrieve VIM driver information.
     * 
     * @return
     * @throws IOException
     */
    public static String readVnfmAdapterInfoFromJson() throws IOException {
        InputStream ins = null;
        BufferedInputStream bins = null;
        String fileContent = "";

        String fileName = SystemEnvVariablesFactory.getInstance().getAppRoot() + System.getProperty("file.separator")
                + "etc" + System.getProperty("file.separator") + "adapterInfo" + System.getProperty("file.separator")
                + VNFMADAPTER2DRIVERMGR;

        try {
            ins = new FileInputStream(fileName);
            bins = new BufferedInputStream(ins);

            byte[] contentByte = new byte[ins.available()];
            int num = bins.read(contentByte);

            if(num > 0) {
                fileContent = new String(contentByte);
            }
        } catch(FileNotFoundException e) {
            LOG.error(fileName + "is not found!", e);
        } finally {
            if(ins != null) {
                ins.close();
            }
            if(bins != null) {
                bins.close();
            }
        }

        return fileContent;
    }

    private static class RegisterVnfm2DriverMgrThread implements Runnable {

        // Thread lock Object
        private final Object lockObject = new Object();

        private IVnfmAdapter2DriverManager adapter2DriverMgr = new VnfmAdapter2DriverManager();

        // url and mothedtype
        private Map<String, String> paramsMap;

        // driver body
        private JSONObject adapterInfo;

        public RegisterVnfm2DriverMgrThread(Map<String, String> paramsMap, JSONObject adapterInfo) {
            this.paramsMap = paramsMap;
            this.adapterInfo = adapterInfo;
        }

        @Override
        public void run() {
            LOG.info("start register vnfmadapter to Driver Manager", RegisterVnfm2DriverMgrThread.class);

            if(paramsMap == null || adapterInfo == null) {
                LOG.error("parameter is null,please check!", RegisterVnfm2DriverMgrThread.class);
                return;
            }

            // catch Runtime Exception
            try {
                sendRequest(paramsMap, adapterInfo);
            } catch(RuntimeException e) {
                LOG.error(e.getMessage(), e);
            }

        }

        private void sendRequest(Map<String, String> paramsMap, JSONObject driverInfo) {
            JSONObject resultObj = adapter2DriverMgr.registerDriver(paramsMap, driverInfo);

            if(Integer.valueOf(resultObj.get("retCode").toString()) == Constant.HTTP_CREATED) {
                LOG.info("Vnfmadapter has now Successfully Registered to the Driver Manager!");
            } else {
                LOG.error("Vnfmadapter failed to  Register to the Driver Manager! Reason:"
                        + resultObj.get("reason").toString() + " retCode:" + resultObj.get("retCode").toString());

                // if registration fails,wait one minute and try again
                try {
                    synchronized(lockObject) {
                        lockObject.wait(Constant.REPEAT_REG_TIME);
                    }
                } catch(InterruptedException e) {
                    LOG.error(e.getMessage(), e);
                }

                sendRequest(this.paramsMap, this.adapterInfo);
            }

        }

    }

    @Override
    public void unregister() {
        // unregister
    }

}
