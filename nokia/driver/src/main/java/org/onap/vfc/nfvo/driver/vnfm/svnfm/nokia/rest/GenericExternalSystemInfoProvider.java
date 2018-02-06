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
package org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.rest;

import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.api.VimInfoProvider;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.api.VnfmInfoProvider;
import org.onap.vnfmdriver.model.VnfmInfo;
import org.slf4j.Logger;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.env.Environment;

import java.util.concurrent.TimeUnit;

import static com.google.common.cache.CacheBuilder.newBuilder;
import static java.lang.Long.valueOf;
import static org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.util.CbamUtils.fatalFailure;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * Responsible for providing access to external systems
 */
abstract public class GenericExternalSystemInfoProvider extends IpMappingProvider implements VnfmInfoProvider, VimInfoProvider, InitializingBean {
    private static Logger logger = getLogger(GenericExternalSystemInfoProvider.class);
    private final Environment environment;
    private LoadingCache<String, VnfmInfo> vnfmInfoCache;

    public GenericExternalSystemInfoProvider(Environment environment) {
        super(environment);
        this.environment = environment;
    }

    /**
     * After the Bean has been initialized the IP mapping and the VMFM cache is initialized
     * It is done in this phase because the logic requires the the @Value anoted fields to
     * be specified
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        vnfmInfoCache = newBuilder().expireAfterWrite(environment.getProperty(VNFM_INFO_CACHE_EVICTION_IN_MS, Long.class, valueOf(DEFAULT_CACHE_EVICTION_TIMEOUT_IN_MS)), TimeUnit.MILLISECONDS).concurrencyLevel(1).build(new CacheLoader<String, VnfmInfo>() {
            @Override
            public VnfmInfo load(String vnfmId) throws Exception {
                logger.info("Quering VNFM info from source with " + vnfmId + " identifier");
                return queryVnfmInfoFromSource(vnfmId);
            }
        });
    }

    /*
     * @param vnfmId the identifier of the VNFM
     * @return the cached VNFM
     */
    public VnfmInfo getVnfmInfo(String vnfmId) {
        try {
            return vnfmInfoCache.get(vnfmId);
        } catch (Exception e) {
            throw fatalFailure(logger, "Unable to query VNFM info for " + vnfmId, e);
        }
    }

    /**
     * Load the information related to the VNFM from the remote source
     *
     * @param vnfmId the identifier of the VNFM
     * @return the description of the VNFM
     */
    public abstract VnfmInfo queryVnfmInfoFromSource(String vnfmId);
}
