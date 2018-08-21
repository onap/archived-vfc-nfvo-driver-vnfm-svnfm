/*
 * Copyright 2018 Huawei Technologies Co., Ltd.
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

package org.onap.vfc.nfvo.vnfm.svnfm.vnfmadapter.service.csm.vnf;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.onap.vfc.nfvo.vnfm.svnfm.vnfmadapter.common.restclient.SystemEnvVariablesFactory;
import org.onap.vfc.nfvo.vnfm.svnfm.vnfmadapter.service.constant.Constant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.json.JSONArray;
import net.sf.json.JSONException;
import net.sf.json.JSONObject;

/**
 * <br>
 * <p>
 * </p>
 * 
 * @author
 * @version     VFC 1.0  2018年5月17日
 */
public abstract class ScaleManager {

    private static final Logger LOG = LoggerFactory.getLogger(ScaleManager.class);

    private static String VM_IDS_PATH =
            SystemEnvVariablesFactory.getInstance().getAppRoot() + System.getProperty(Constant.FILE_SEPARATOR) + "etc"
                    + System.getProperty(Constant.FILE_SEPARATOR) + "vnfpkginfo"
                    + System.getProperty(Constant.FILE_SEPARATOR) + "vms" + System.getProperty(Constant.FILE_SEPARATOR);

    public static void beforeScaleOut(JSONObject queryVms, String vnfId) {
        try {
            JSONArray vms = queryVms.getJSONObject("data").getJSONArray("vms");
            writeVmIdsToFile(vnfId, vms);
        } catch(JSONException e) {
            LOG.error("function=beforeScaleOut, msg=recode current vms JSONException", e);
        }
    }

    public static JSONArray beforeScaleIn(JSONObject queryVms, String vnfId) {
        JSONArray vmList = new JSONArray();
        try {
            JSONArray vms = queryVms.getJSONObject("data").getJSONArray("vms");
            JSONArray recodeVms = readVmIdsFile(vnfId);
            if(!recodeVms.isEmpty()) {
                for(int i = 0; i < vms.size(); i++) {
                    JSONObject obj = vms.getJSONObject(i);
                    String vmId = obj.getString("id");
                    if(isScaleOutVm(recodeVms, vmId)) {
                        JSONObject vmIdObj = new JSONObject();
                        vmIdObj.put("vm_id", vmId);
                        vmList.add(vmIdObj);
                    }
                }
            }
        } catch(JSONException e) {
            LOG.error("function=beforeScaleIn, msg=recode current vms JSONException", e);
        }
        return vmList;
    }

    private static boolean isScaleOutVm(JSONArray recodeVms, String vmId) {
        for(int i = 0; i < recodeVms.size(); i++) {
            JSONObject obj = recodeVms.getJSONObject(i);
            String oldVmId = obj.getString("id");
            if(oldVmId.equalsIgnoreCase(vmId)) {
                return false;
            }
        }
        return true;
    }

    private static String getVmIdsFilePath(String vnfId) {
        return VM_IDS_PATH + vnfId + ".json";
    }

    private static void writeVmIdsToFile(String vnfId, JSONArray vms) {
        String filePath = getVmIdsFilePath(vnfId);
        try {
            File destFile = FileUtils.getFile(filePath);

            File parentFile = destFile.getParentFile();
            if(parentFile != null) {
                if(!parentFile.mkdirs() && !parentFile.isDirectory()) {
                    throw new IOException("Destination '" + parentFile + "' directory cannot be created");
                }
            }
            if(!destFile.exists()) {
                destFile.createNewFile();
            }
            try(FileOutputStream outStream = new FileOutputStream(destFile)) {
                outStream.write(vms.toString().getBytes());
            }
        } catch(IOException e) {
            LOG.error("function=writeVmIdsToFile, msg=write vms to file ioexception, e : {}", e);
        }
    }

    private static JSONArray readVmIdsFile(String vnfId) {
        String filePath = getVmIdsFilePath(vnfId);

        String fileContent = "";
        try(InputStream ins = FileUtils.openInputStream(FileUtils.getFile(filePath));
            BufferedInputStream bins = new BufferedInputStream(ins);) {
            byte[] contentByte = new byte[ins.available()];
            int num = bins.read(contentByte);

            if(num > 0) {
                fileContent = new String(contentByte);
            }

            if(StringUtils.isNotEmpty(fileContent)) {
                return JSONArray.fromObject(fileContent);
            }
        } catch(IOException e) {
            LOG.error("function=readVmIdsFile, msg=read vms from file IOException, filePath : {}" + filePath, " Load File Exception : " + e);
        } catch(JSONException e) {
            LOG.error("function=readVmIdsFile, msg=read vms from file JSONException, fileContent : {}", fileContent, " JSON Exception : " + e);
        }
        return new JSONArray();
    }
}
