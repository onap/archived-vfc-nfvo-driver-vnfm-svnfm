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

package com.nokia.vfcadaptor.constant;

public class CommonConstants {
	public static final String HTTP_ERROR_DESC_500 = "Internal Server Error";
	
	
	public static final String CONTENT_TYPE = "Content-Type";
	public static final String ACCEPT = "Accept";
	
	public static final String AUTH = "auth";
	public static final String AUTHORIZATION = "Authorization";
	public static final String UTF_8 = "utf-8";
	
	//Nslcm path
	public static final String RetrieveNvfmListPath = "/nslcm/vl/vnfms/%s";
	public static final String NslcmGrantPath = "/nslcm/v1/ns/grantvnf";
	public static final String NslcmNotifyPath = "/nslcm/v1/vnfs/%s/Notify";
	
	//Catalog path
	public static final String RetrieveVnfPackagePath = "/catalog/v1/vnfpackages/%s";
	
	//CBAM -- Nokia VNFM path
	public static final String CBAM_TOKEN_KEY = "access_token";
	public static final String RetrieveCbamTokenPath="/auth/realms/cbam/protocol/openid-connect/token";
	public static final String RetrieveCbamTokenPostStr="grant_type=%s&client_id=%s&client_secret=%s";
	public static final String CbamCreateVnfPath="/vnfs";
	public static final String CbamInstantiateVnfPath="/vnfs/%s/instantiate";
	public static final String CbamQueryVnfPath="/vnfs/%s";
	public static final String CbamDeleteVnfPath="/vnfs/%s";
	public static final String CbamTerminateVnfPath="/vnfs/%s/terminate";
	public static final String CbamGetOperStatusPath="/operation_executions/%s";
	public static final String CbamScaleVnfPath = "/vnfs/%s/scale";
	public static final String CbamHealVnfPath="/vnfs/%s/heal";
	
	public static final String NSLCM_OPERATION_INSTANTIATE = "Instantiate";
	public static final String NSLCM_OPERATION_SCALE_OUT = "Scaleout";
	public static final String NSLCM_OPERATION_SCALE_IN = "Scalein";
	public static final String NSLCM_OPERATION_SCALE_UP = "Scaleup";
	public static final String NSLCM_OPERATION_SCALE_DOWN = "Scaledown";
	public static final String NSLCM_OPERATION_SCALE_TERMINATE = "Terminal";
}
