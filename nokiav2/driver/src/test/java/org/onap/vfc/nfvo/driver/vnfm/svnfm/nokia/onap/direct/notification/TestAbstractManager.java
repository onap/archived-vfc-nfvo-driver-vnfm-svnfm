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

import com.google.gson.JsonObject;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.onap.aai.domain.yang.v11.*;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.onap.direct.AAIRestApiProvider;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.vnfm.CbamRestApiProvider;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.vnfm.DriverProperties;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.vnfm.TestBase;
import org.slf4j.Logger;

import java.util.NoSuchElementException;

import static junit.framework.TestCase.assertEquals;
import static org.mockito.Mockito.when;

public class TestAbstractManager extends TestBase {
    private ObjectFactory OBJECT_FACTORY = new ObjectFactory();
    @Mock
    private AAIRestApiProvider aaiRestApiProvider;
    private DummyManager dummyManager;

    @Before
    public void init() {
        dummyManager = new DummyManager(aaiRestApiProvider, cbamRestApiProvider, driverProperties);
    }

    /**
     * if the REST resource does not exists the provided instance is used
     */
    @Test
    public void testIfResourceDoesNotExists() throws Exception {
        GenericVnf newInstance = OBJECT_FACTORY.createGenericVnf();
        when(aaiRestApiProvider.get(logger, AAIRestApiProvider.AAIService.CLOUD, "url", GenericVnf.class)).thenThrow(new NoSuchElementException());
        //when
        GenericVnf actualInstance = dummyManager.createOrGet(AAIRestApiProvider.AAIService.CLOUD, "url", newInstance);
        //verify
        assertEquals(newInstance, actualInstance);
    }

    /**
     * if the REST resource exists it is not recreated
     */
    @Test
    public void testIfResourceExists() throws Exception {
        GenericVnf newInstance = OBJECT_FACTORY.createGenericVnf();
        GenericVnf existingInstance = OBJECT_FACTORY.createGenericVnf();
        existingInstance.setVnfId("id");
        when(aaiRestApiProvider.get(logger, AAIRestApiProvider.AAIService.CLOUD, "url", GenericVnf.class)).thenReturn(existingInstance);
        //when
        GenericVnf actualInstance = dummyManager.createOrGet(AAIRestApiProvider.AAIService.CLOUD, "url", newInstance);
        //verify
        assertEquals(existingInstance, actualInstance);
    }

    @Test
    public void testBuildRelationshipData() {
        RelationshipData relationshipData = AbstractManager.buildRelationshipData("key", "value");
        assertEquals("key", relationshipData.getRelationshipKey());
        assertEquals("value", relationshipData.getRelationshipValue());
    }

    @Test
    public void testExtractMandatoryValue() {
        JsonObject object = new JsonObject();
        object.addProperty("key", "value");
        assertEquals("value", AbstractManager.extractMandatoryValue(object, "key"));
    }

    /**
     * the same relation is replaced
     */
    @Test
    public void testAddSingletonRelationForExisting() {
        RelationshipList relations = OBJECT_FACTORY.createRelationshipList();
        Relationship relation = OBJECT_FACTORY.createRelationship();
        relation.setRelatedTo("unknownRelation");
        relations.getRelationship().add(relation);
        Relationship sameRelation = OBJECT_FACTORY.createRelationship();
        sameRelation.setRelatedTo("relatedTo");
        relations.getRelationship().add(sameRelation);
        RelationshipData data = OBJECT_FACTORY.createRelationshipData();
        data.setRelationshipValue("v");
        data.setRelationshipKey("k");
        sameRelation.getRelationshipData().add(data);

        Relationship newRelation = OBJECT_FACTORY.createRelationship();
        newRelation.setRelatedTo("relatedTo");
        RelationshipData data2 = OBJECT_FACTORY.createRelationshipData();
        data2.setRelationshipValue("v2");
        data2.setRelationshipKey("k2");
        newRelation.getRelationshipData().add(data2);

        //when
        AbstractManager.addSingletonRelation(relations, newRelation);
        //verify

        assertEquals(2, relations.getRelationship().size());
        assertEquals(1, relations.getRelationship().get(1).getRelationshipData().size());
        assertEquals("k2", relations.getRelationship().get(1).getRelationshipData().get(0).getRelationshipKey());
        assertEquals("v2", relations.getRelationship().get(1).getRelationshipData().get(0).getRelationshipValue());
    }

    /**
     * the missing relation is created
     */
    @Test
    public void testAddSingletonRelation() {
        RelationshipList relations = OBJECT_FACTORY.createRelationshipList();
        Relationship relation = OBJECT_FACTORY.createRelationship();
        relation.setRelatedTo("unknownRelation");
        relations.getRelationship().add(relation);

        Relationship newRelation = OBJECT_FACTORY.createRelationship();
        newRelation.setRelatedTo("relatedTo");
        RelationshipData data2 = OBJECT_FACTORY.createRelationshipData();
        data2.setRelationshipValue("v2");
        data2.setRelationshipKey("k2");
        newRelation.getRelationshipData().add(data2);

        //when
        AbstractManager.addSingletonRelation(relations, newRelation);
        //verify
        assertEquals(2, relations.getRelationship().size());
        assertEquals(1, relations.getRelationship().get(1).getRelationshipData().size());
        assertEquals("k2", relations.getRelationship().get(1).getRelationshipData().get(0).getRelationshipKey());
        assertEquals("v2", relations.getRelationship().get(1).getRelationshipData().get(0).getRelationshipValue());
    }

    /**
     * the same relation is replaced
     */
    @Test
    public void testAddMissingRelationForExisting() {
        RelationshipList relations = OBJECT_FACTORY.createRelationshipList();
        Relationship relation = OBJECT_FACTORY.createRelationship();
        relation.setRelatedTo("unknownRelation");
        relations.getRelationship().add(relation);
        Relationship sameRelation = OBJECT_FACTORY.createRelationship();
        sameRelation.setRelatedTo("relatedTo");
        relations.getRelationship().add(sameRelation);
        RelationshipData data = OBJECT_FACTORY.createRelationshipData();
        data.setRelationshipValue("v");
        data.setRelationshipKey("k");
        sameRelation.getRelationshipData().add(data);

        Relationship newRelation = OBJECT_FACTORY.createRelationship();
        newRelation.setRelatedTo("relatedTo");
        RelationshipData data2 = OBJECT_FACTORY.createRelationshipData();
        data2.setRelationshipValue("v2");
        data2.setRelationshipKey("k2");
        newRelation.getRelationshipData().add(data2);

        //when
        AbstractManager.addMissingRelation(relations, newRelation);
        //verify

        assertEquals(3, relations.getRelationship().size());
        assertEquals(1, relations.getRelationship().get(1).getRelationshipData().size());
        assertEquals("k", relations.getRelationship().get(1).getRelationshipData().get(0).getRelationshipKey());
        assertEquals("v", relations.getRelationship().get(1).getRelationshipData().get(0).getRelationshipValue());
        assertEquals("k2", relations.getRelationship().get(2).getRelationshipData().get(0).getRelationshipKey());
        assertEquals("v2", relations.getRelationship().get(2).getRelationshipData().get(0).getRelationshipValue());
    }

    /**
     * adding the same relation is not duplicated
     */
    @Test
    public void testAddMissingRelation() {
        RelationshipList relations = OBJECT_FACTORY.createRelationshipList();
        Relationship relation = OBJECT_FACTORY.createRelationship();
        relation.setRelatedTo("unknownRelation");
        relations.getRelationship().add(relation);

        Relationship sameRelation = OBJECT_FACTORY.createRelationship();
        sameRelation.setRelatedTo("relatedTo");
        relations.getRelationship().add(sameRelation);
        RelationshipData data = OBJECT_FACTORY.createRelationshipData();
        data.setRelationshipValue("v");
        data.setRelationshipKey("k");
        sameRelation.getRelationshipData().add(data);

        Relationship newRelation = OBJECT_FACTORY.createRelationship();
        newRelation.setRelatedTo("relatedTo");
        RelationshipData data2 = OBJECT_FACTORY.createRelationshipData();
        data2.setRelationshipValue("v");
        data2.setRelationshipKey("k");
        newRelation.getRelationshipData().add(data2);

        //when
        AbstractManager.addMissingRelation(relations, newRelation);
        //verify
        assertEquals(2, relations.getRelationship().size());
        assertEquals(1, relations.getRelationship().get(1).getRelationshipData().size());
        assertEquals("k", relations.getRelationship().get(1).getRelationshipData().get(0).getRelationshipKey());
        assertEquals("v", relations.getRelationship().get(1).getRelationshipData().get(0).getRelationshipValue());
    }

    class DummyManager extends AbstractManager {

        DummyManager(AAIRestApiProvider aaiRestApiProvider, CbamRestApiProvider cbamRestApiProvider, DriverProperties driverProperties) {
            super(aaiRestApiProvider, cbamRestApiProvider, driverProperties);
        }

        @Override
        protected Logger getLogger() {
            return logger;
        }
    }


}
