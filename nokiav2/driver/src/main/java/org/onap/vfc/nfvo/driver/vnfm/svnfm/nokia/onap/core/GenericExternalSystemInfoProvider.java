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
package org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.onap.core;

import com.google.common.base.Splitter;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.api.VimInfoProvider;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.api.VnfmInfoProvider;
import org.onap.vnfmdriver.model.VnfmInfo;
import org.slf4j.Logger;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.env.Environment;

import static java.lang.Long.valueOf;

import static com.google.common.cache.CacheBuilder.newBuilder;
import static org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.util.CbamUtils.SEPARATOR;
import static org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.util.CbamUtils.buildFatalFailure;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * Responsible for providing access to core systems
 */
public abstract class GenericExternalSystemInfoProvider extends IpMappingProvider implements VnfmInfoProvider, VimInfoProvider, InitializingBean {
    /**
     * The name of the VNFM info cache eviction in the properties file
     */
    public static final String VNFM_INFO_CACHE_EVICTION_IN_MS = "vnfmInfoCacheEvictionInMs";
    /**
     * The default VNFM info cache eviction in milliseconds
     */
    public static final int DEFAULT_CACHE_EVICTION_TIMEOUT_IN_MS = 10 * 60 * 1000;
    private static Logger logger = getLogger(GenericExternalSystemInfoProvider.class);
    private final Environment environment;
    private LoadingCache<String, VnfmInfo> vnfmInfoCache;

    public GenericExternalSystemInfoProvider(Environment environment) {
        super(environment);
        this.environment = environment;
    }

    public static VnfmUrls convert(VnfmInfo vnfmInfo) {
        ArrayList<String> urls = Lists.newArrayList(Splitter.on(SEPARATOR).split(vnfmInfo.getUrl()));
        return new VnfmUrls(urls.get(0), urls.get(1), urls.get(2), urls.get(3));
    }

    public static VnfmCredentials convertToCredentials(VnfmInfo vnfmInfo) {
        ArrayList<String> userNames = Lists.newArrayList(Splitter.on(SEPARATOR).split(vnfmInfo.getUserName()));
        ArrayList<String> passwords = Lists.newArrayList(Splitter.on(SEPARATOR).split(vnfmInfo.getPassword()));
        return new VnfmCredentials(userNames.get(0), passwords.get(0), userNames.get(1), passwords.get(1));
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
                logger.info("Querying VNFM info from source with " + vnfmId + " identifier");
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
            throw buildFatalFailure(logger, "Unable to query VNFM info for " + vnfmId, e);
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
