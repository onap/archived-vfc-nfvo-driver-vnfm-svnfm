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

package org.openo.nfvo.vnfmadapter.service.adapter.impl;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpStatus;
import org.openo.baseservice.roa.util.restclient.RestfulResponse;
import org.openo.baseservice.util.impl.SystemEnvVariablesFactory;
import org.openo.nfvo.vnfmadapter.common.DownloadCsarManager;
import org.openo.nfvo.vnfmadapter.common.VnfmException;
import org.openo.nfvo.vnfmadapter.common.servicetoken.VNFRestfulUtil;
import org.openo.nfvo.vnfmadapter.service.adapter.inf.IResourceManager;
import org.openo.nfvo.vnfmadapter.service.constant.Constant;
import org.openo.nfvo.vnfmadapter.service.constant.UrlConstant;
import org.openo.nfvo.vnfmadapter.service.csm.connect.ConnectMgrVnfm;
import org.openo.nfvo.vnfmadapter.service.csm.connect.HttpRequests;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.json.JSONArray;
import net.sf.json.JSONException;
import net.sf.json.JSONObject;

/**
 * Resource Manager adapter class.
 * .</br>
 *
 * @author
 * @version NFVO 0.5 Sep 13, 2016
 */
public class AdapterResourceManager implements IResourceManager {

    private static final Logger LOG = LoggerFactory.getLogger(AdapterResourceManager.class);

    @Override
    public JSONObject uploadVNFPackage(JSONObject vnfpkg, Map<String, String> paramsMap) {
        JSONObject resultObj = new JSONObject();
        JSONObject csarTempObj = new JSONObject();

        try {
            // if upper layer do not provide vnfpackage info,then get the
            // vnfpackage info from JSON file.
            if(vnfpkg == null || vnfpkg.isEmpty()) {
                String vnfPkgInfo = readVfnPkgInfoFromJson();
                vnfpkg = JSONObject.fromObject(vnfPkgInfo); // NOSONAR
            }
        } catch(IOException e) {
            LOG.error("function=uploadVNFPackage", e);
        }

        // check if parameters are null.
        if(paramsMap == null || paramsMap.isEmpty()) {
            resultObj.put("reason", "csarid and vnfmid are null.");
            resultObj.put("retCode", Constant.REST_FAIL);
            return resultObj;
        }

        String csarid = paramsMap.get("csarid");
        String vnfmid = paramsMap.get("vnfmid");
        String vnfdid = "";
        String vnfDescriptorId = paramsMap.get("vnfDescriptorId");
        if(null == csarid || "".equals(csarid)) {
            resultObj.put("reason", "csarid is null.");
            resultObj.put("retCode", Constant.REST_FAIL);
            return resultObj;
        }
        if(null == vnfmid || "".equals(vnfmid)) {
            resultObj.put("reason", "vnfmid is null.");
            resultObj.put("retCode", Constant.REST_FAIL);
            return resultObj;
        }

        // obtain CSAR package info
        JSONObject csarobj = getVnfmCsarInfo(csarid);
        String downloadUri = "";
        if(Integer.valueOf(csarobj.get("retCode").toString()) == Constant.HTTP_OK) {
            LOG.info("get CSAR info successful.", csarobj.get("retCode"));
            downloadUri = csarobj.getString("downloadUri");
        } else {
            LOG.error("get CSAR info fail.", csarobj.get("retCode"));
            resultObj.put("reason", csarobj.get("reason").toString());
            resultObj.put("retCode", Constant.REST_FAIL);
            return resultObj;
        }

        csarTempObj = vnfpkg.getJSONObject("template");
        String csarfilepath = csarTempObj.getString("csar_file_path");
        String csarfilename = csarTempObj.getString("csar_file_name");

        // download csar package and save in location.
        JSONObject downloadObject =
                downloadCsar(downloadUri, csarfilepath + System.getProperty("file.separator") + csarfilename);

        if(Integer.valueOf(downloadObject.get("retCode").toString()) != Constant.REST_SUCCESS) {
            LOG.error("download CSAR fail.", downloadObject.get("retCode"));
            resultObj.put("reason", downloadObject.get("reason").toString());
            resultObj.put("retCode", downloadObject.get("retCode").toString());
            return resultObj;
        }
        LOG.info("download CSAR successful.", downloadObject.get("retCode"));

        // unzip csar package to location.
        JSONObject unzipObject =
                unzipCSAR(csarfilepath + System.getProperty("file.separator") + csarfilename, csarfilepath);

        if(Integer.valueOf(unzipObject.get("retCode").toString()) != Constant.REST_SUCCESS) {
            LOG.error("unzip CSAR fail.", unzipObject.get("retCode"));
            resultObj.put("reason", unzipObject.get("reason").toString());
            resultObj.put("retCode", unzipObject.get("retCode").toString());
            return resultObj;
        }
        LOG.info("unzip CSAR successful.", unzipObject.get("retCode"));

        Map<String, String> vnfmMap = new HashMap<>();
        vnfmMap.put("url", String.format(UrlConstant.REST_VNFMINFO_GET, vnfmid));
        vnfmMap.put("methodType", Constant.GET);

        // get VNFM connection info
        JSONObject vnfmObject = getVnfmConnInfo(vnfmMap);
        if(Integer.valueOf(vnfmObject.get("retCode").toString()) != Constant.HTTP_OK) {
            LOG.error("get Vnfm Connection Info fail.", vnfmObject.get("retCode"));
            resultObj.put("reason", vnfmObject.get("reason").toString());
            resultObj.put("retCode", vnfmObject.get("retCode").toString());
            return resultObj;
        }
        LOG.info("get Vnfm Connection Info successful.", vnfmObject.get("retCode"));

        String vnfmUrl = vnfmObject.getString("url");
        String userName = vnfmObject.getString("userName");
        String password = vnfmObject.getString("password");

        // build VNFM connection and get token
        ConnectMgrVnfm mgrVcmm = new ConnectMgrVnfm();

        JSONObject connObject = new JSONObject();
        connObject.put("url", vnfmUrl);
        connObject.put("userName", userName);
        connObject.put("password", password);
        if(Constant.HTTP_OK != mgrVcmm.connect(vnfmObject, Constant.CERTIFICATE)) {
            LOG.error("get Access Session fail.");
            resultObj.put(Constant.RETCODE, Constant.HTTP_INNERERROR);
            resultObj.put("reason", "connect fail.");
            return resultObj;
        }
        LOG.info("get Access Session successful.");
        String connToken = mgrVcmm.getAccessSession();

        // get vim_id
        JSONObject cloudObject = getAllCloud(vnfmUrl, connToken);
        String vimId = "";

        if(!cloudObject.isEmpty() && cloudObject.get(Constant.RETCODE).equals(HttpStatus.SC_OK)) {
            LOG.info("get all cloud successful.", cloudObject.get(Constant.RETCODE));
            vimId = cloudObject.getString("vim_id");
        } else {
            LOG.error("get all cloud fail.", cloudObject.get(Constant.RETCODE));
            return cloudObject;
        }

        // upload VNF package
        csarTempObj.put("vim_id", vimId);
        vnfpkg.put("template", csarTempObj);

        JSONObject uploadPkgObject = upload(vnfpkg, vnfmUrl, connToken);
        LOG.info("uploadPkgObject:" + uploadPkgObject);
        if(vnfdid == null || "".equals(vnfdid.trim())) {
            JSONObject vnfdConf = readVnfdIdInfoFromJson();
            LOG.info("vnfdConf=" + vnfdConf);
            if(vnfdConf.containsKey(vnfDescriptorId)) {
                vnfdid = vnfdConf.getString(vnfDescriptorId);
            }
        }
        LOG.info("set vnfdId=" + vnfdid);

        // get vnfd version
        String vnfdVersion = "";

        JSONObject vnfdVerObject =
                getVnfdVersion(vnfmUrl, String.format(UrlConstant.URL_VNFDINFO_GET, vnfdid), connToken);
        LOG.info("vnfdVerObject:" + vnfdVerObject);
        if(!vnfdVerObject.isEmpty() && vnfdVerObject.get(Constant.RETCODE).equals(HttpStatus.SC_OK)) {
            LOG.info("get vnfd version successful.", vnfdVerObject.get(Constant.RETCODE));
            JSONArray verArr = vnfdVerObject.getJSONArray("templates");
            JSONObject verTmpObj = verArr.getJSONObject(0);

            vnfdVersion = verTmpObj.getString("vnfdVersion");
        } else {
            LOG.error("get vnfd version fail.", vnfdVerObject.get(Constant.RETCODE));
            return vnfdVerObject;
        }

        // get vnfd plan info
        String planName = "";
        String planId = "";

        JSONObject vnfdPlanInfo = getVNFDPlanInfo(vnfmUrl, vnfdid, connToken);
        LOG.info("vnfdPlanInfo:" + vnfdPlanInfo);
        JSONObject inputsObj = new JSONObject();
        if(!vnfdPlanInfo.isEmpty() && vnfdPlanInfo.get(Constant.RETCODE).equals(HttpStatus.SC_OK)) {
            LOG.info("get vnfd plan info successful.", vnfdPlanInfo.get(Constant.RETCODE));
            JSONObject planTmpObj = vnfdPlanInfo.getJSONObject("template");
            String templateName = planTmpObj.getString("template_name").trim();
            JSONArray topoTmpObj = planTmpObj.getJSONArray("topology_template");

            JSONObject planObj = topoTmpObj.getJSONObject(0);
            if("VNFD_vUGW".equals(templateName)) {
                for(int i = 0; i < topoTmpObj.size(); i++) {
                    String name = topoTmpObj.getJSONObject(i).getString("plan_name").trim();
                    if("Normal_E9K".equals(name)) {
                        planObj = topoTmpObj.getJSONObject(i);
                    }
                }
            }

            planName = planObj.getString("plan_name");
            planId = planObj.getString("plan_id");
            if(planObj.containsKey("inputs")) {
                JSONArray inputs = planObj.getJSONArray("inputs");
                for(int i = 0; i < inputs.size(); i++) {
                    JSONObject obj = inputs.getJSONObject(i);
                    obj.put("value", obj.getString("default"));
                }
                inputsObj.put("inputs", inputs);
                inputsObj.put("External_network", new JSONArray());
            }
        } else {
            LOG.error("get vnfd plan info fail.", vnfdPlanInfo.get(Constant.RETCODE));
            return vnfdPlanInfo;
        }

        // return values
        resultObj.put("retCode", Constant.HTTP_OK);
        resultObj.put("vnfdId", vnfdid);
        resultObj.put("vnfdVersion", vnfdVersion);
        resultObj.put("planName", planName);
        resultObj.put("planId", planId);
        resultObj.put("parameters", inputsObj);
        LOG.info("resultObj:" + resultObj.toString());

        return resultObj;
    }

    private JSONObject sendRequest(Map<String, String> paramsMap) {
        JSONObject resultObj = new JSONObject();
        RestfulResponse rsp = VNFRestfulUtil.getRemoteResponse(paramsMap, "");
        if(null == rsp) {
            LOG.error("function=sendRequest,  RestfulResponse is null");
            resultObj.put("reason", "RestfulResponse is null.");
            resultObj.put("retCode", Constant.REST_FAIL);
            return resultObj;
        }
        String resultCreate = rsp.getResponseContent();

        if(rsp.getStatus() == Constant.HTTP_OK) {
            LOG.warn("function=sendRequest, msg= status={}, result={}.", rsp.getStatus(), resultCreate);
            resultObj = JSONObject.fromObject(resultCreate);
            resultObj.put("retCode", Constant.HTTP_OK);
            return resultObj;
        } else {
            LOG.error("function=sendRequest, msg=ESR return fail,status={}, result={}.", rsp.getStatus(), resultCreate);
            resultObj.put("reason", "ESR return fail.");
        }
        resultObj.put("retCode", Constant.REST_FAIL);
        return resultObj;
    }

    @Override
    public JSONObject getVnfmCsarInfo(String csarid) {
        JSONObject resultObj = new JSONObject();

        if(null == csarid || "".equals(csarid)) {
            resultObj.put("reason", "csarid is null.");
            resultObj.put("retCode", Constant.REST_FAIL);
            return resultObj;
        }

        Map<String, String> paramsMap = new HashMap<String, String>();

        paramsMap.put("url", String.format(UrlConstant.REST_CSARINFO_GET, csarid));
        paramsMap.put("methodType", Constant.GET);

        return this.sendRequest(paramsMap);
    }

    @Override
    public JSONObject downloadCsar(String url, String filePath) {
        JSONObject resultObj = new JSONObject();

        if(url == null || "".equals(url)) {
            resultObj.put("reason", "url is null.");
            resultObj.put("retCode", Constant.REST_FAIL);
            return resultObj;
        }
        if(filePath == null || "".equals(filePath)) {
            resultObj.put("reason", "downloadUrl filePath is null.");
            resultObj.put("retCode", Constant.REST_FAIL);
            return resultObj;
        }

        String status = DownloadCsarManager.download(url, filePath);

        if(Constant.DOWNLOADCSAR_SUCCESS.equals(status)) {
            resultObj.put("reason", "download csar file successfully.");
            resultObj.put("retCode", Constant.REST_SUCCESS);
        } else {
            resultObj.put("reason", "download csar file failed.");
            resultObj.put("retCode", Constant.REST_FAIL);
        }
        return resultObj;
    }

    @Override
    public JSONObject getAllCloud(String url, String conntoken) {
        JSONObject resultObj = new JSONObject();
        JSONArray resArray = new JSONArray();

        if(url == null || url.equals("")) {
            url = "http://127.0.0.1:31943";
        }

        // get vim_id
        HttpMethod httpMethodCloud = null;
        try {
            httpMethodCloud =
                    new HttpRequests.Builder(Constant.CERTIFICATE).setUrl(url.trim(), UrlConstant.URL_ALLCLOUD_NEW_GET)
                            .addHeader(Constant.HEADER_AUTH_TOKEN, conntoken).setParams("").get().execute();

            int statusCode = httpMethodCloud.getStatusCode();

            String result = httpMethodCloud.getResponseBodyAsString();
            LOG.info(result);
            if(statusCode == HttpStatus.SC_OK) {
                JSONObject vimInfo = JSONObject.fromObject(result);
                resArray = vimInfo.getJSONArray("vim_info");
                resultObj = resArray.getJSONObject(0);
                resultObj.put(Constant.RETCODE, statusCode);
            } else {
                LOG.error("uploadVNFPackage get allcloud failed, code:" + statusCode + " re:" + result);
                resultObj.put(Constant.RETCODE, statusCode);
                resultObj.put("reason", "get allcloud failed. code:" + statusCode + " re:" + result);
                return resultObj;
            }
        } catch(JSONException e) {
            LOG.error("function=uploadVNFPackage, msg=uploadVNFPackage get allcloud JSONException e={}.", e);
            resultObj.put(Constant.RETCODE, Constant.HTTP_INNERERROR);
            resultObj.put("reason", "get allcloud failed and JSONException." + e.getMessage());
            return resultObj;
        } catch(VnfmException e) {
            LOG.error("function=uploadVNFPackage, msg=uploadVNFPackage get allcloud VnfmException e={}.", e);
            resultObj.put(Constant.RETCODE, Constant.HTTP_INNERERROR);
            resultObj.put("reason", "get allcloud failed and VnfmException." + e.getMessage());
            return resultObj;
        } catch(IOException e) {
            LOG.error("function=uploadVNFPackage, msg=uploadVNFPackage get allcloud IOException e={}.", e);
            resultObj.put(Constant.RETCODE, Constant.HTTP_INNERERROR);
            resultObj.put("reason", "get allcloud failed and IOException." + e.getMessage());
            return resultObj;
        }
        return resultObj;
    }

    /**
     * Upload vnfpackage<br>
     *
     * @param vnfpackage
     * @param vnfmurl
     * @param conntoken
     * @return
     * @since NFVO 0.5
     */
    public JSONObject upload(JSONObject vnfpackage, String vnfmurl, String conntoken) {
        JSONObject resultObj = new JSONObject();
        HttpMethod httpMethodVnf = null;

        try {
            httpMethodVnf = new HttpRequests.Builder(Constant.CERTIFICATE)
                    .setUrl(vnfmurl.trim(), UrlConstant.URL_VNFPACKAGE_POST).setParams(vnfpackage.toString())
                    .addHeader(Constant.HEADER_AUTH_TOKEN, conntoken).post().execute();

            int statusCodeUp = httpMethodVnf.getStatusCode();

            String resultUp = httpMethodVnf.getResponseBodyAsString();

            if(statusCodeUp == HttpStatus.SC_CREATED || statusCodeUp == HttpStatus.SC_OK) {
                LOG.info("uploadVNFPackage upload VNF package successful, code:" + statusCodeUp + " re:" + resultUp);
                resultObj = JSONObject.fromObject(resultUp);
                resultObj.put(Constant.RETCODE, statusCodeUp);
            } else {
                LOG.error("uploadVNFPackage upload VNF package failed, code:" + statusCodeUp + " re:" + resultUp);
                resultObj.put(Constant.RETCODE, statusCodeUp);
                resultObj.put("data", "upload VNF package failed, code:" + statusCodeUp + " re:" + resultUp);
                return resultObj;
            }
        } catch(JSONException e) {
            LOG.error("function=uploadVNFPackage, msg=uploadVNFPackage upload VNF package JSONException e={}.", e);
            resultObj.put(Constant.RETCODE, Constant.HTTP_INNERERROR);
            resultObj.put("reason", "upload VNF package failed and JSONException." + e.getMessage());
            return resultObj;
        } catch(VnfmException e) {
            LOG.error("function=uploadVNFPackage, msg=uploadVNFPackage upload VNF package VnfmException e={}.", e);
            resultObj.put(Constant.RETCODE, Constant.HTTP_INNERERROR);
            resultObj.put("reason", "upload VNF package failed and VnfmException." + e.getMessage());
            return resultObj;
        } catch(IOException e) {
            LOG.error("function=uploadVNFPackage, msg=uploadVNFPackage upload VNF package IOException e={}.", e);
            resultObj.put(Constant.RETCODE, Constant.HTTP_INNERERROR);
            resultObj.put("reason", "upload VNF package failed and IOException." + e.getMessage());
            return resultObj;
        }
        return resultObj;
    }

    /**
     * Find vnfd version.<br>
     *
     * @param prefixUrl
     * @param serviceUrl
     * @return
     * @since NFVO 0.5
     */
    public JSONObject getVnfdVersion(String prefixUrl, String serviceUrl, String conntoken) {
        JSONObject resultObj = new JSONObject();
        HttpMethod httpMethodVnfd = null;
        try {
            httpMethodVnfd = new HttpRequests.Builder(Constant.CERTIFICATE).setUrl(prefixUrl.trim(), serviceUrl)
                    .setParams("").addHeader(Constant.HEADER_AUTH_TOKEN, conntoken).get().execute();

            int statusCodeVnfd = httpMethodVnfd.getStatusCode();

            String resultVnfd = httpMethodVnfd.getResponseBodyAsString();
            LOG.info("getVnfdVersion result:" + resultVnfd);
            if(statusCodeVnfd == HttpStatus.SC_OK) {
                resultObj = JSONObject.fromObject(resultVnfd);
                resultObj.put(Constant.RETCODE, statusCodeVnfd);
            } else {
                LOG.error("uploadVNFPackage vnfd version failed, code:" + statusCodeVnfd + " re:" + resultVnfd);
                resultObj.put(Constant.RETCODE, statusCodeVnfd);
                resultObj.put("data", "get vnfd version failed, code:" + statusCodeVnfd + " re:" + resultVnfd);
                return resultObj;
            }
        } catch(JSONException e) {
            LOG.error("function=uploadVNFPackage, msg=uploadVNFPackage get vnfd version JSONException e={}.", e);
            resultObj.put(Constant.RETCODE, Constant.HTTP_INNERERROR);
            resultObj.put("reason", "get vnfd version failed and JSONException." + e.getMessage());
            return resultObj;
        } catch(VnfmException e) {
            LOG.error("function=uploadVNFPackage, msg=uploadVNFPackage get vnfd version VnfmException e={}.", e);
            resultObj.put(Constant.RETCODE, Constant.HTTP_INNERERROR);
            resultObj.put("reason", "get vnfd version failed and VnfmException." + e.getMessage());
            return resultObj;
        } catch(IOException e) {
            LOG.error("function=uploadVNFPackage, msg=uploadVNFPackage get vnfd version IOException e={}.", e);
            resultObj.put(Constant.RETCODE, Constant.HTTP_INNERERROR);
            resultObj.put("reason", "get vnfd version failed and IOException." + e.getMessage());
            return resultObj;
        }
        return resultObj;
    }

    /**
     * Find VNFM connection information.<br>
     *
     * @param paramsMap
     * @return
     * @since NFVO 0.5
     */
    public JSONObject getVnfmConnInfo(Map<String, String> paramsMap) {
        return this.sendRequest(paramsMap);
    }

    @Override
    public JSONObject getVNFDPlanInfo(String url, String vnfdid, String conntoken) {
        JSONObject resultObj = new JSONObject();

        HttpMethod httpMethodPlan = null;
        try {
            httpMethodPlan = new HttpRequests.Builder(Constant.CERTIFICATE)
                    .setUrl(url.trim(), String.format(UrlConstant.URL_VNFDPLANINFO_GET, vnfdid)).setParams("")
                    .addHeader(Constant.HEADER_AUTH_TOKEN, conntoken).get().execute();

            int statusCode = httpMethodPlan.getStatusCode();

            String result = httpMethodPlan.getResponseBodyAsString();
            LOG.info("getVNFDPlanInfo result=" + result);
            if(statusCode == HttpStatus.SC_OK) {
                resultObj = JSONObject.fromObject(result);
                resultObj.put(Constant.RETCODE, statusCode);
            } else {
                LOG.error("uploadVNFPackage get VNFDPlanInfo failed, code:" + statusCode + " re:" + result);
                resultObj.put(Constant.RETCODE, statusCode);
                resultObj.put("reason", "get VNFDPlanInfo failed. code:" + statusCode + " re:" + result);
                return resultObj;
            }
        } catch(JSONException e) {
            LOG.error("function=uploadVNFPackage, msg=uploadVNFPackage get VNFDPlanInfo JSONException e={}.", e);
            resultObj.put(Constant.RETCODE, Constant.HTTP_INNERERROR);
            resultObj.put("reason", "get VNFDPlanInfo failed and JSONException." + e.getMessage());
            return resultObj;
        } catch(VnfmException e) {
            LOG.error("function=uploadVNFPackage, msg=uploadVNFPackage get VNFDPlanInfo VnfmException e={}.", e);
            resultObj.put(Constant.RETCODE, Constant.HTTP_INNERERROR);
            resultObj.put("reason", "get VNFDPlanInfo failed and VnfmException." + e.getMessage());
            return resultObj;
        } catch(IOException e) {
            LOG.error("function=uploadVNFPackage, msg=uploadVNFPackage get VNFDPlanInfo IOException e={}.", e);
            resultObj.put(Constant.RETCODE, Constant.HTTP_INNERERROR);
            resultObj.put("reason", "get VNFDPlanInfo failed and IOException." + e.getMessage());
            return resultObj;
        }
        return resultObj;
    }

    /**
     * Get VNF package information.<br>
     *
     * @return
     * @throws IOException
     * @since NFVO 0.5
     */
    public static String readVfnPkgInfoFromJson() throws IOException {
        InputStream ins = null;
        BufferedInputStream bins = null;
        String fileContent = "";

        String fileName = SystemEnvVariablesFactory.getInstance().getAppRoot() + System.getProperty("file.separator")
                + "etc" + System.getProperty("file.separator") + "vnfpkginfo" + System.getProperty("file.separator")
                + Constant.VNFPKGINFO;

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

    private static JSONObject readVnfdIdInfoFromJson() {
        JSONObject jsonObject = new JSONObject();
        InputStream ins = null;
        BufferedInputStream bins = null;
        String fileContent = "";

        String fileName = SystemEnvVariablesFactory.getInstance().getAppRoot() + System.getProperty("file.separator")
                + "etc" + System.getProperty("file.separator") + "vnfpkginfo" + System.getProperty("file.separator")
                + "vnfd_ids.json";

        try {
            ins = new FileInputStream(fileName);
            bins = new BufferedInputStream(ins);

            byte[] contentByte = new byte[ins.available()];
            int num = bins.read(contentByte);

            if(num > 0) {
                fileContent = new String(contentByte);
            }
            if(fileContent != null) {
                jsonObject = JSONObject.fromObject(fileContent).getJSONObject("vnfdIds");
            }
            if(ins != null) {
                ins.close();
            }
            if(bins != null) {
                bins.close();
            }
        } catch(Exception e) {
            LOG.error(fileName + " read error!", e);
        } finally {

        }

        return jsonObject;
    }

    /*
     * unzip CSAR packge
     * @param fileName filePath
     * @return
     */
    public JSONObject unzipCSAR(String fileName, String filePath) {
        JSONObject resultObj = new JSONObject();

        if(fileName == null || "".equals(fileName)) {
            resultObj.put("reason", "fileName is null.");
            resultObj.put("retCode", Constant.REST_FAIL);
            return resultObj;
        }
        if(filePath == null || "".equals(filePath)) {
            resultObj.put("reason", "unzipCSAR filePath is null.");
            resultObj.put("retCode", Constant.REST_FAIL);
            return resultObj;
        }

        int status = DownloadCsarManager.unzipCSAR(fileName, filePath);

        if(Constant.UNZIP_SUCCESS == status) {
            resultObj.put("reason", "unzip csar file successfully.");
            resultObj.put("retCode", Constant.REST_SUCCESS);
        } else {
            resultObj.put("reason", "unzip csar file failed.");
            resultObj.put("retCode", Constant.REST_FAIL);
        }
        return resultObj;
    }

}
