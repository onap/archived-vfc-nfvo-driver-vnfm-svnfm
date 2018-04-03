/*
 * Copyright 2016-2017, Nokia Corporation
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

package org.onap.vfc.nfvo.driver.vnfm.svnfm.adaptor;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import org.onap.vfc.nfvo.driver.vnfm.svnfm.cbam.bo.CBAMCreateVnfRequest;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.cbam.bo.CBAMHealVnfRequest;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.cbam.bo.CBAMInstantiateVnfRequest;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.cbam.bo.CBAMScaleVnfRequest;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.cbam.bo.CBAMTerminateVnfRequest;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.common.util.CommonUtil;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.constant.ScaleDirection;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.constant.ScaleType;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nslcm.bo.NslcmGrantVnfResponse;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nslcm.bo.entity.GrantInfo;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nslcm.bo.entity.VimComputeResourceFlavour;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.vnfmdriver.bo.HealVnfRequest;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.vnfmdriver.bo.InstantiateVnfRequest;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.vnfmdriver.bo.ScaleVnfRequest;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.vnfmdriver.bo.TerminateVnfRequest;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;

@Component
public class Driver2CbamRequestConverter {

	public CBAMCreateVnfRequest createReqConvert(InstantiateVnfRequest driverRequest) {
		CBAMCreateVnfRequest request = new CBAMCreateVnfRequest();

		request.setVnfdId(driverRequest.getVnfdId());
		request.setName(driverRequest.getVnfInstanceName());
		request.setDescription(driverRequest.getVnfInstanceDescription());
		return request;
	}

	public CBAMInstantiateVnfRequest instantiateRequestConvert(InstantiateVnfRequest driverRequest,
			NslcmGrantVnfResponse nslc, GrantInfo grant, VimComputeResourceFlavour vimco) throws IOException {
        Gson gson = new Gson();
		String inputJson = readcbamInputInfoFromJsonFile();
		return gson.fromJson(inputJson, CBAMInstantiateVnfRequest.class);
	}

	private String readcbamInputInfoFromJsonFile() throws IOException {
		String filePath = "/etc/vnfpkginfo/cbam_input.json";
		return CommonUtil.getJsonStrFromFile(filePath);
	}

	public CBAMTerminateVnfRequest terminateReqConvert(TerminateVnfRequest driverRequest) {
		CBAMTerminateVnfRequest request = new CBAMTerminateVnfRequest();
//		request.setTerminationType(driverRequest.getTerminationType().toUpperCase());
		request.setTerminationType("FORCEFUL");
		request.setGracefulTerminationTimeout(driverRequest.getGracefulTerminationTimeout());
		return request;
	}

	public CBAMHealVnfRequest healReqConvert() {
		CBAMHealVnfRequest request = new CBAMHealVnfRequest();
		request.setCause("");
		request.setAdditionalParams("");
		return request;
	}

	public CBAMScaleVnfRequest scaleReqconvert(ScaleVnfRequest driverRequest) {
		CBAMScaleVnfRequest request = new CBAMScaleVnfRequest();
		if (driverRequest.getType().equals(ScaleType.SCALE_OUT)) {
			request.setType(ScaleDirection.OUT);
		} else {
			request.setType(ScaleDirection.IN);
		}
		request.setAspectId(driverRequest.getAspectId());
		request.setNumberOfSteps(driverRequest.getNumberOfSteps());
		request.setAdditionalParams(driverRequest.getAdditionalParam());
		return request;
	}
	
//	public static void main(String[] argv) throws IOException {
//		Gson gson = new Gson();
//		String filePath = "D:\\cbam_input.json";
//		String inputJson = readcbamInputInfo(filePath);
//		CBAMInstantiateVnfRequest request = gson.fromJson(inputJson, CBAMInstantiateVnfRequest.class);
//		System.out.println(gson.toJson(request));
//	}
//	
//	public static String readcbamInputInfo(String filePath) throws IOException {
//		InputStream ins = null;
//        BufferedInputStream bins = null;
//        String fileContent = "";
//        String fileName = filePath;
//
//        try {
//            ins = new FileInputStream(fileName);
//            bins = new BufferedInputStream(ins);
//
//            byte[] contentByte = new byte[ins.available()];
//            int num = bins.read(contentByte);
//
//            if(num > 0) {
//                fileContent = new String(contentByte);
//            }
//        } catch(FileNotFoundException e) {
//        	e.printStackTrace();;
//        } finally {
//            if(ins != null) {
//                ins.close();
//            }
//            if(bins != null) {
//                bins.close();
//            }
//        }
//		return fileContent;
//	}
}
