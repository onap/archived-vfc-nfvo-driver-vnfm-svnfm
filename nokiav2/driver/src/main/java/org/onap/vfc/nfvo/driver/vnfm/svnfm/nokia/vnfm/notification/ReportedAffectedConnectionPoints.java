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
package org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.vnfm.notification;

import com.google.gson.annotations.SerializedName;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * The connection points reported by each operation
 */
public class ReportedAffectedConnectionPoints {
    @SerializedName("cbam_pre")
    private Set<ReportedAffectedCp> pre = new HashSet<>();
    @SerializedName("cbam_post")
    private Set<ReportedAffectedCp> post = new HashSet<>();

    /**
     * @return the connection points that were present after the operation has finished
     */
    public Set<ReportedAffectedCp> getPost() {
        return post;
    }

    /**
     * @param post the connection points that were present after the operation has finished
     */
    public void setPost(Set<ReportedAffectedCp> post) {
        this.post = post;
    }

    /**
     * @return the connection points that were present before the operation was started
     */
    public Set<ReportedAffectedCp> getPre() {
        return pre;
    }

    /**
     * @param pre the connection points that were present before the operation was started
     */
    public void setPre(Set<ReportedAffectedCp> pre) {
        this.pre = pre;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ReportedAffectedConnectionPoints that = (ReportedAffectedConnectionPoints) o;
        return Objects.equals(pre, that.pre) &&
                Objects.equals(post, that.post);
    }

    @Override
    public int hashCode() {

        return Objects.hash(pre, post);
    }

    @Override
    public String toString() {
        return "ReportedAffectedConnectionPoints{" +
                "pre=" + pre +
                ", post=" + post +
                '}';
    }
}