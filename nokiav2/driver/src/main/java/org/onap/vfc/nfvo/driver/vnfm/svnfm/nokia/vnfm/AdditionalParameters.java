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
package org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.vnfm;

import com.nokia.cbam.lcm.v32.model.*;
import com.nokia.cbam.lcm.v32.model.VimInfo.VimInfoTypeEnum;
import java.util.*;

/**
 * Represents the additional parameters to be sent during instantiation from VF-C to the driver
 */
public class AdditionalParameters {
    private VimInfoTypeEnum vimType;
    private String instantiationLevel;
    private List<VimComputeResourceFlavour> computeResourceFlavours = new ArrayList<>();
    private List<ZoneInfo> zones = new ArrayList<>();
    private List<VimSoftwareImage> softwareImages = new ArrayList<>();
    private List<ExtManagedVirtualLinkData> extManagedVirtualLinks = new ArrayList<>();
    private Map<String, List<NetworkAddress>> externalConnectionPointAddresses = new HashMap<>();
    private List<ExtVirtualLinkData> extVirtualLinks = new ArrayList<>();
    private List<VnfProperty> extensions = new ArrayList<>();
    private Object additionalParams;
    private String domain;

    public AdditionalParameters() {
        //only used through reflection (gson)
    }

    /**
     * @return the additional parameters of the instantiation
     */
    public Object getAdditionalParams() {
        return additionalParams;
    }

    /**
     * @param additionalParams the additional parameters of the instantiation
     */
    public void setAdditionalParams(Object additionalParams) {
        this.additionalParams = additionalParams;
    }

    /**
     * @return the type of the VIM
     */
    public VimInfoTypeEnum getVimType() {
        return vimType;
    }

    /**
     * @param vimType the type of the VIM
     */
    public void setVimType(VimInfoTypeEnum vimType) {
        this.vimType = vimType;
    }

    /**
     * @return the flavours to be used for the VNF
     */
    public List<VimComputeResourceFlavour> getComputeResourceFlavours() {
        return computeResourceFlavours;
    }

    /**
     * @param computeResourceFlavours the flavours to be used for the VNF
     */
    public void setComputeResourceFlavours(List<VimComputeResourceFlavour> computeResourceFlavours) {
        this.computeResourceFlavours = computeResourceFlavours;
    }

    /**
     * @return the images to be used
     */
    public List<VimSoftwareImage> getSoftwareImages() {
        return softwareImages;
    }

    /**
     * @param softwareImages the images to be used
     */
    public void setSoftwareImages(List<VimSoftwareImage> softwareImages) {
        this.softwareImages = softwareImages;
    }

    /**
     * @return the zones to be used for the VNF
     */
    public List<ZoneInfo> getZones() {
        return zones;
    }

    /**
     * @param zones the zones to be used for the VNF
     */
    public void setZones(List<ZoneInfo> zones) {
        this.zones = zones;
    }

    /**
     * @return the instantiation level of the VNF
     */
    public String getInstantiationLevel() {
        return instantiationLevel;
    }

    /**
     * @param instantiationLevel the instantiation level of the VNF
     */
    public void setInstantiationLevel(String instantiationLevel) {
        this.instantiationLevel = instantiationLevel;
    }

    /**
     * @return the externally managed internal virtual links
     */
    public List<ExtManagedVirtualLinkData> getExtManagedVirtualLinks() {
        return extManagedVirtualLinks;
    }

    /**
     * @param extManagedVirtualLinks the externally managed internal virtual links
     */
    public void setExtManagedVirtualLinks(List<ExtManagedVirtualLinkData> extManagedVirtualLinks) {
        this.extManagedVirtualLinks = extManagedVirtualLinks;
    }

    /**
     * @return a binding of the extenal connection points by identifier to it's network addresses to be used
     */
    public Map<String, List<NetworkAddress>> getExternalConnectionPointAddresses() {
        return externalConnectionPointAddresses;
    }

    /**
     * @param externalConnectionPointAddresses a binding of the extenal connection points by identifier to it's network addresses to be used
     */
    public void setExternalConnectionPointAddresses(Map<String, List<NetworkAddress>> externalConnectionPointAddresses) {
        this.externalConnectionPointAddresses = externalConnectionPointAddresses;
    }

    public List<ExtVirtualLinkData> getExtVirtualLinks() {
        return extVirtualLinks;
    }

    public void setExtVirtualLinks(List<ExtVirtualLinkData> extVirtualLinks) {
        this.extVirtualLinks = extVirtualLinks;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    /**
     * @return the extensions of the VNF modifiable attributes
     */
    public List<VnfProperty> getExtensions() {
        return extensions;
    }

    /**
     * @param extensions the extensions of the VNF modifiable attributes
     */
    public void setExtensions(List<VnfProperty> extensions) {
        this.extensions = extensions;
    }

    @Override
    //generated code. This is the recommended way to formulate equals
    @SuppressWarnings({"squid:S00122", "squid:S1067"})
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AdditionalParameters that = (AdditionalParameters) o;
        return vimType == that.vimType &&
                Objects.equals(domain, that.domain) &&
                Objects.equals(instantiationLevel, that.instantiationLevel) &&
                Objects.equals(computeResourceFlavours, that.computeResourceFlavours) &&
                Objects.equals(zones, that.zones) &&
                Objects.equals(softwareImages, that.softwareImages) &&
                Objects.equals(extManagedVirtualLinks, that.extManagedVirtualLinks) &&
                Objects.equals(externalConnectionPointAddresses, that.externalConnectionPointAddresses) &&
                Objects.equals(extVirtualLinks, that.extVirtualLinks) &&
                Objects.equals(extensions, that.extensions) &&
                Objects.equals(additionalParams, that.additionalParams);
    }

    @Override
    public int hashCode() {
        return Objects.hash(vimType, domain, instantiationLevel, computeResourceFlavours, zones, softwareImages, extManagedVirtualLinks, externalConnectionPointAddresses, extVirtualLinks, extensions, additionalParams);
    }

    @Override
    public String toString() {
        return "AdditionalParameters{" +
                "vimType=" + vimType +
                ", instantiationLevel='" + instantiationLevel + '\'' +
                ", computeResourceFlavours=" + computeResourceFlavours +
                ", zones=" + zones +
                ", softwareImages=" + softwareImages +
                ", extManagedVirtualLinks=" + extManagedVirtualLinks +
                ", externalConnectionPointAddresses=" + externalConnectionPointAddresses +
                ", extVirtualLinks=" + extVirtualLinks +
                ", extensions=" + extensions +
                ", additionalParams=" + additionalParams +
                ", domain='" + domain + '\'' +
                '}';
    }
}