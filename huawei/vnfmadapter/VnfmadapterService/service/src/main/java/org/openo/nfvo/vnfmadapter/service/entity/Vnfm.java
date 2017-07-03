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

package org.openo.nfvo.vnfmadapter.service.entity;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * Provide database class
 * <br/>
 * <p>
 * </p>
 *
 * @author
 * @version NFVO 0.5 Sep 3, 2016
 */
public class Vnfm {

    private String id;

    private String version;

    private String vnfdId;

    private String vnfPackageId;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getVnfdId() {
        return vnfdId;
    }

    public void setVnfdId(String vnfdId) {
        this.vnfdId = vnfdId;
    }

    public String getVnfPackageId() {
        return vnfPackageId;
    }

    public void setVnfPackageId(String vnfPackageId) {
        this.vnfPackageId = vnfPackageId;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if(this == obj) {
            return true;
        }
        if(obj == null) {
            return false;
        }
        if(!(obj instanceof Vnfm)) {
            return false;
        }
        if(getClass() != obj.getClass()) {
            return false;
        }
        Vnfm other = (Vnfm)obj;
        if(id == null) {
            if(other.id != null) {
                return false;
            }
        } else if(!id.equals(other.id)) {
            return false;
        }
        return true;
    }
}
