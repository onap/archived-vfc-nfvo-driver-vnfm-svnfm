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

package org.onap.vfc.nfvo.vnfm.svnfm.vnfmadapter.common.restclient;

/**
 * ROA exception handling parameters.
 * <br/>
 * <p>
 * </p>
 * 
 * @author
 * @version 28-May-2016
 */
public class ExceptionArgs {

    /**
     * Exception descriptions.
     */
    private String[] descArgs = null;

    /**
     * Exception reasons.
     */
    private String[] reasonArgs = null;

    /**
     * Exception detals.
     */
    private String[] detailArgs = null;

    /**
     * Exception advices.
     */
    private String[] adviceArgs = null;

    /**
     * Constructor<br/>
     * <p>
     * </p>
     * 
     * @since
     */
    public ExceptionArgs() {
        // default constructor.
    }

    /**
     * Constructor<br/>
     * <p>
     * </p>
     * 
     * @since
     * @param descArgs: descriptions.
     * @param reasonArgs: reasons.
     * @param detailArgs: details.
     * @param adviceArgs: advices.
     */
    public ExceptionArgs(final String[] descArgs, final String[] reasonArgs, final String[] detailArgs,
            final String[] adviceArgs) {
        this.descArgs = descArgs;
        this.reasonArgs = reasonArgs;
        this.detailArgs = detailArgs;
        this.adviceArgs = adviceArgs;
    }

    public String[] getDescArgs() {
        return descArgs;
    }

    public void setDescArgs(final String[] descArgs) {
        this.descArgs = descArgs;
    }

    public String[] getReasonArgs() {
        return reasonArgs;
    }

    public void setReasonArgs(final String[] reasonArgs) {
        this.reasonArgs = reasonArgs;
    }

    public String[] getDetailArgs() {
        return detailArgs;
    }

    public void setDetailArgs(final String[] detailArgs) {
        this.detailArgs = detailArgs;
    }

    public String[] getAdviceArgs() {
        return adviceArgs;
    }

    public void setAdviceArgs(final String[] adviceArgs) {
        this.adviceArgs = adviceArgs;
    }
}
