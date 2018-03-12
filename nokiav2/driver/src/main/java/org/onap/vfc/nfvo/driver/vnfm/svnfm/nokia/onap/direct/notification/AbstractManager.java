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
package org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.onap.direct.notification;

import com.google.gson.Gson;
import org.onap.aai.domain.yang.v11.ObjectFactory;
import org.onap.aai.domain.yang.v11.Relationship;
import org.onap.aai.domain.yang.v11.RelationshipData;
import org.onap.aai.domain.yang.v11.RelationshipList;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.onap.direct.AAIRestApiProvider;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.vnfm.CbamRestApiProvider;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.vnfm.DriverProperties;
import org.slf4j.Logger;

import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

import static com.google.common.collect.Iterables.find;
import static org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.util.CbamUtils.SEPARATOR;

/**
 * Handles the common management of changing entities in AAI
 */
abstract class AbstractManager {
    protected static final ObjectFactory OBJECT_FACTORY = new ObjectFactory();
    protected final AAIRestApiProvider aaiRestApiProvider;
    protected final CbamRestApiProvider cbamRestApiProvider;
    protected final DriverProperties driverProperties;

    AbstractManager(AAIRestApiProvider aaiRestApiProvider, CbamRestApiProvider cbamRestApiProvider, DriverProperties driverProperties) {
        this.aaiRestApiProvider = aaiRestApiProvider;
        this.cbamRestApiProvider = cbamRestApiProvider;
        this.driverProperties = driverProperties;
    }

    /**
     * @param key   the key of the relationship
     * @param value the value of the relationship
     * @return the relationship
     */
    protected static RelationshipData buildRelationshipData(String key, String value) {
        RelationshipData data = new RelationshipData();
        data.setRelationshipKey(key);
        data.setRelationshipValue(value);
        return data;
    }

    /**
     * Extract mandatory value from the additional data on LCN resources
     *
     * @param additionalData the additional data
     * @param key            the key of the additional data
     * @return the value of the additional data
     */
    protected static String extractMandatoryValue(Object additionalData, String key) {
        return new Gson().toJsonTree(additionalData).getAsJsonObject().get(key).getAsString();
    }

    /**
     * Create or update the singleton relationship. Singleton means that relationships can only have a
     * single {@link Relationship} with the given {@link Relationship#getRelatedTo} value
     *
     * @param relationships the list of relationships
     * @param relationship  the expected relationship
     */
    protected static void addSingletonRelation(RelationshipList relationships, Relationship relationship) {
        boolean found = false;
        for (Relationship currentRelationShip : relationships.getRelationship()) {
            if (relationship.getRelatedTo().equals(currentRelationShip.getRelatedTo())) {
                found = true;
            }
        }
        if (!found) {
            relationships.getRelationship().add(relationship);
        } else {
            Relationship existingRelationShip = find(relationships.getRelationship(), currentRelationShip -> currentRelationShip.getRelatedTo().equals(relationship.getRelatedTo()));
            existingRelationShip.getRelationshipData().clear();
            existingRelationShip.getRelationshipData().addAll(relationship.getRelationshipData());
        }
    }

    /**
     * Add the given relationship if it is already not part of the relationships
     *
     * @param relationships the relationships
     * @param relationship  the relationship to be added
     */
    protected static void addMissingRelation(RelationshipList relationships, Relationship relationship) {
        for (Relationship currentRelationShip : relationships.getRelationship()) {
            if (currentRelationShip.getRelatedTo().equals(relationship.getRelatedTo())
                    && compositeKeys(currentRelationShip.getRelationshipData()).equals(compositeKeys(relationship.getRelationshipData()))) {
                return;
            }
        }
        relationships.getRelationship().add(relationship);
    }

    private static Set<String> compositeKeys(List<RelationshipData> data) {
        Set<String> keys = new HashSet<>();
        for (RelationshipData relationshipData : data) {
            keys.add(relationshipData.getRelationshipKey() + SEPARATOR + relationshipData.getRelationshipValue());
        }
        return keys;
    }

    /**
     * @return the concrete logger to be used
     */
    protected abstract Logger getLogger();

    /**
     * Creates or returns a REST resource instance
     *
     * @param service     the type of the service
     * @param url         the URL of the resource without the service prefix
     * @param newInstance the empty instance if the resource does not exists
     * @param <T>         the type of the resource
     * @return the created or queried resource
     */
    protected <T> T createOrGet(AAIRestApiProvider.AAIService service, String url, T newInstance) {
        try {
            return (T) aaiRestApiProvider.get(getLogger(), service, url, newInstance.getClass());
        } catch (NoSuchElementException e) {
            getLogger().debug("The resource on " + url + " URL was not found in AAI", e);
            return newInstance;
        }
    }
}
