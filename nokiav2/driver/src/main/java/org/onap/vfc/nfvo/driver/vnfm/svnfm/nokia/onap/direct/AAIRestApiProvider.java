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
package org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.onap.direct;

import com.google.common.annotations.VisibleForTesting;
import org.onap.aai.restclient.client.Headers;
import org.onap.aai.restclient.client.OperationResult;
import org.onap.aai.restclient.client.RestClient;
import org.onap.aai.restclient.enums.RestAuthenticationMode;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.onap.core.MsbApiProvider;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.spring.Conditions;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Conditional;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import javax.xml.bind.JAXBContext;
import java.io.ByteArrayOutputStream;
import java.io.StringReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import static com.google.common.collect.Lists.newArrayList;
import static javax.ws.rs.core.MediaType.APPLICATION_XML_TYPE;
import static org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.onap.core.SelfRegistrationManager.SERVICE_NAME;
import static org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.util.CbamUtils.buildFatalFailure;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * Responsible for providing access to AAI APIs.
 * Handles authentication and mandatory parameters.
 */
@Component
@Conditional(value = Conditions.UseForDirect.class)
public class AAIRestApiProvider {
    private static final String AAI_VERSION = "v11";
    private static Logger logger = getLogger(AAIRestApiProvider.class);
    private final MsbApiProvider msbApiProvider;
    @Value("${aaiUsername}")
    private String aaiUsername;
    @Value("${aaiPassword}")
    private String aaiPassword;

    @Autowired
    AAIRestApiProvider(MsbApiProvider msbApiProvider) {
        this.msbApiProvider = msbApiProvider;
    }

    /**
     * @param logger  the logger of the class that requests unmarshalling
     * @param service the AAI service of the request
     * @param url     the URL of the request after the base URL (ex. /cloud-infrastructure/...)
     * @param clazz   the class of the result
     * @param <T>     the type of the result
     * @return the result of the GET request
     */
    public <T> T get(Logger logger, AAIService service, String url, Class<T> clazz) {
        return expectSuccess(logger, buildClient().get(getBaseUrl(service.getServiceName()) + url, buildCommonHeaders(), APPLICATION_XML_TYPE), clazz, url);
    }

    /**
     * @param logger  the logger of the class that requests unmarshalling
     * @param service the AAI service of the request
     * @param url     the URL of the request after the base URL (ex. /cloud-infrastructure/...)
     * @param payload the payload of the request (non serialized)
     * @param clazz   the class of the result
     * @param <T>     the type of the result
     * @return the result of the PUT request
     */
    public <T, S> T put(Logger logger, AAIService service, String url, S payload, Class<T> clazz) {
        String marshalledContent = marshall(payload);
        OperationResult result = buildClient().put(getBaseUrl(service.getServiceName()) + url, marshalledContent, buildCommonHeaders(), APPLICATION_XML_TYPE, APPLICATION_XML_TYPE);
        return expectSuccess(logger, result, clazz, url);
    }

    /**
     * Execute a delete request on the given URL
     *
     * @param logger  the logger of the class that requests unmarshalling
     * @param service the AAI service of the request
     * @param url     the URL of the request after the base URL (ex. /cloud-infrastructure/...)
     */
    public void delete(Logger logger, AAIService service, String url) {
        buildClient().delete(getBaseUrl(service.getServiceName()) + url, buildCommonHeaders(), APPLICATION_XML_TYPE);
    }

    /**
     * @param serviceName the name of the AAI service on MSB
     * @return the base URL of the service
     */
    private String getBaseUrl(String serviceName) {
        return msbApiProvider.getMicroServiceUrl(serviceName, AAI_VERSION);
    }

    private <T> T expectSuccess(Logger logger, OperationResult result, Class<T> clazz, String url) {
        if (!result.wasSuccessful()) {
            if (result.getResultCode() == 404) {
                logger.debug("The resource at " + url + " does not exists");
                throw new NoSuchElementException("The resource at " + url + " does not exists");
            }
            throw buildFatalFailure(logger, "Bad response. Code: " + result.getResultCode() + " cause: " + result.getFailureCause());
        }
        if (clazz.isAssignableFrom(Void.class)) {
            return null;
        }
        return unmarshal(result.getResult(), clazz);
    }

    private <T> T unmarshal(String content, Class<T> clazz) {
        try {
            return (T) JAXBContext.newInstance(clazz).createUnmarshaller().unmarshal(new StringReader(content));
        } catch (Exception e) {
            throw buildFatalFailure(logger, "Unable to unmarshal content", e);
        }
    }

    private String marshall(Object object) {
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            JAXBContext.newInstance(object.getClass()).createMarshaller().marshal(object, bos);
            return bos.toString();
        } catch (Exception e) {
            throw buildFatalFailure(logger, "Unable to marshal content", e);
        }
    }

    /**
     * @return the common mandatory headers for AAI requests
     */
    private Map<String, List<String>> buildCommonHeaders() {
        Map<String, List<String>> headers = new HashMap<>();
        headers.put(Headers.ACCEPT, newArrayList(MediaType.APPLICATION_XML_VALUE));
        headers.put(Headers.FROM_APP_ID, newArrayList(SERVICE_NAME));
        return headers;
    }


    private RestClient buildClient() {
        return buildRawClient().basicAuthUsername(aaiUsername).basicAuthPassword(aaiPassword).authenticationMode(RestAuthenticationMode.SSL_BASIC);
    }

    @VisibleForTesting
    RestClient buildRawClient() {
        return new RestClient();
    }

    public enum AAIService {
        NETWORK {
            String getServiceName() {
                return "aai-network";
            }
        },
        ESR {
            String getServiceName() {
                return "aai-externalSystem";
            }
        },
        CLOUD {
            String getServiceName() {
                return "aai-cloudInfrastructure";
            }
        };

        abstract String getServiceName();
    }
}
