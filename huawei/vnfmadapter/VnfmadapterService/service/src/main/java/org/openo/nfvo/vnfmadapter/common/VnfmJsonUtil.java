/*
 * Copyright 2016 Huawei Technologies Co., Ltd.
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

package org.openo.nfvo.vnfmadapter.common;
import java.io.IOException;
import java.io.InputStream;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.json.JSONArray;
import net.sf.json.JSONException;
import net.sf.json.JSONObject;
import net.sf.json.util.JSONTokener;

/**
 * VNFM JSON utils.</br>
 *
 * @author
 * @version     NFVO 0.5  Sep 10, 2016
 */
public final class VnfmJsonUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(VnfmJsonUtil.class);

    private VnfmJsonUtil(){
        //private constructor
    }

    /**
     * Get the JSON string from input http context.
     * <br/>
     *
     * @param vnfReq HttpServletRequest
     * @return
     * @since  NFVO 0.5
     */
    @SuppressWarnings("unchecked")
    public static <T> T getJsonFromContexts(HttpServletRequest vnfReq) {
        try {
            InputStream vnfInput = vnfReq.getInputStream();
            String vnfJsonStr = IOUtils.toString(vnfInput);
            JSONTokener vnfJsonTokener = new JSONTokener(vnfJsonStr);

            if(vnfJsonTokener.nextClean() == Character.codePointAt("{", 0)) {
                return (T)JSONObject.fromObject(vnfJsonStr);
            }

            vnfJsonTokener.back();

            if(vnfJsonTokener.nextClean() == Character.codePointAt("[", 0)) {
                return (T)JSONArray.fromObject(vnfJsonStr);
            }
        } catch(IOException e) {
            LOGGER.error("function=getJsonFromContext, msg=IOException occurs, e={}.", e);
        } catch(JSONException e) {
            LOGGER.error("function=getJsonFromContext, msg=JSONException occurs, e={}.", e);
        }

        return null;
    }
}
