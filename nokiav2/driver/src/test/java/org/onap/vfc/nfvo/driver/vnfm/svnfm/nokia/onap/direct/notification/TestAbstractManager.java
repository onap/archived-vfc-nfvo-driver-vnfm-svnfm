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
import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.onap.aai.model.GenericVnf;
import org.onap.aai.model.Relationship;
import org.onap.aai.model.RelationshipData;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.onap.direct.AAIRestApiProvider;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.vnfm.CbamRestApiProvider;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.vnfm.TestBase;
import org.slf4j.Logger;

import static io.reactivex.Observable.error;
import static junit.framework.TestCase.assertEquals;

public class TestAbstractManager extends TestBase {
    @Mock
    private AAIRestApiProvider aaiRestApiProvider;
    private DummyManager dummyManager;

    @Before
    public void init() {
        dummyManager = new DummyManager(aaiRestApiProvider, cbamRestApiProvider);
    }

    /**
     * if the REST resource does not exists the provided instance is used
     */
    @Test
    public void testIfResourceDoesNotExists() throws Exception {
        GenericVnf newInstance = new GenericVnf();
        //when
        GenericVnf actualInstance = dummyManager.createOrGet(error(new RuntimeException()), newInstance);
        //verify
        assertEquals(newInstance, actualInstance);
    }

    /**
     * if the REST resource exists it is not recreated
     */
    @Test
    public void testIfResourceExists() throws Exception {
        GenericVnf newInstance = new GenericVnf();
        GenericVnf existingInstance = new GenericVnf();
        //when
        GenericVnf actualInstance = dummyManager.createOrGet(buildObservable(existingInstance), newInstance);
        //verify
        assertEquals(existingInstance, actualInstance);
    }

    /**
     * Test relationship data builder
     */
    @Test
    public void testBuildRelationshipData() {
        RelationshipData relationshipData = AbstractManager.buildRelationshipData("key", "value");
        assertEquals("key", relationshipData.getRelationshipKey());
        assertEquals("value", relationshipData.getRelationshipValue());
    }

    /**
     * test mandatory value extraction
     */
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
        List<Relationship> relationships = new ArrayList<>();
        Relationship relation = new Relationship();
        relation.setRelatedTo("unknownRelation");
        relation.setRelationshipData(new ArrayList<>());
        relationships.add(relation);
        Relationship sameRelation = new Relationship();
        sameRelation.setRelatedTo("relatedTo");
        relationships.add(sameRelation);
        RelationshipData data = new RelationshipData();
        data.setRelationshipValue("v");
        data.setRelationshipKey("k");
        sameRelation.setRelationshipData(new ArrayList<>());
        sameRelation.getRelationshipData().add(data);

        Relationship newRelation = new Relationship();
        newRelation.setRelatedTo("relatedTo");
        RelationshipData data2 = new RelationshipData();
        data2.setRelationshipValue("v2");
        data2.setRelationshipKey("k2");
        newRelation.setRelationshipData(new ArrayList<>());
        newRelation.getRelationshipData().add(data2);

        //when
        AbstractManager.addSingletonRelation(relationships, newRelation);
        //verify

        assertEquals(2, relationships.size());
        assertEquals(1, relationships.get(1).getRelationshipData().size());
        assertEquals("k2", relationships.get(1).getRelationshipData().get(0).getRelationshipKey());
        assertEquals("v2", relationships.get(1).getRelationshipData().get(0).getRelationshipValue());
    }

    /**
     * the missing relation is created
     */
    @Test
    public void testAddSingletonRelation() {
        Relationship relation = new Relationship();
        relation.setRelatedTo("unknownRelation");
        List<Relationship> relationships = new ArrayList<>();

        relationships.add(relation);

        Relationship newRelation = new Relationship();
        newRelation.setRelatedTo("relatedTo");
        RelationshipData data2 = new RelationshipData();
        ;
        data2.setRelationshipValue("v2");
        data2.setRelationshipKey("k2");
        newRelation.setRelationshipData(new ArrayList<>());
        newRelation.getRelationshipData().add(data2);

        //when
        AbstractManager.addSingletonRelation(relationships, newRelation);
        //verify
        assertEquals(2, relationships.size());
        assertEquals(1, relationships.get(1).getRelationshipData().size());
        assertEquals("k2", relationships.get(1).getRelationshipData().get(0).getRelationshipKey());
        assertEquals("v2", relationships.get(1).getRelationshipData().get(0).getRelationshipValue());
    }

    /**
     * the same relation is replaced
     */
    @Test
    public void testAddMissingRelationForExisting() {
        List<Relationship> relationships = new ArrayList<>();
        Relationship relation = new Relationship();
        relation.setRelatedTo("unknownRelation");
        relationships.add(relation);
        Relationship sameRelation = new Relationship();
        sameRelation.setRelatedTo("relatedTo");
        relationships.add(sameRelation);
        RelationshipData data = new RelationshipData();
        ;
        data.setRelationshipValue("v");
        data.setRelationshipKey("k");
        sameRelation.setRelationshipData(new ArrayList<>());
        sameRelation.getRelationshipData().add(data);

        Relationship newRelation = new Relationship();
        newRelation.setRelatedTo("relatedTo");
        RelationshipData data2 = new RelationshipData();
        ;
        data2.setRelationshipValue("v2");
        data2.setRelationshipKey("k2");
        newRelation.setRelationshipData(new ArrayList<>());
        newRelation.getRelationshipData().add(data2);

        //when
        AbstractManager.addMissingRelation(relationships, newRelation);
        //verify

        assertEquals(3, relationships.size());
        assertEquals(1, relationships.get(1).getRelationshipData().size());
        assertEquals("k", relationships.get(1).getRelationshipData().get(0).getRelationshipKey());
        assertEquals("v", relationships.get(1).getRelationshipData().get(0).getRelationshipValue());
        assertEquals("k2", relationships.get(2).getRelationshipData().get(0).getRelationshipKey());
        assertEquals("v2", relationships.get(2).getRelationshipData().get(0).getRelationshipValue());
    }

    /**
     * adding the same relation is not duplicated
     */
    @Test
    public void testAddMissingRelation() {
        Relationship relation = new Relationship();
        relation.setRelatedTo("unknownRelation");
        List<Relationship> relationships = new ArrayList<>();
        relationships.add(relation);

        Relationship sameRelation = new Relationship();
        sameRelation.setRelatedTo("relatedTo");
        relationships.add(sameRelation);
        RelationshipData data = new RelationshipData();
        ;
        data.setRelationshipValue("v");
        data.setRelationshipKey("k");
        sameRelation.setRelationshipData(new ArrayList<>());
        sameRelation.getRelationshipData().add(data);

        Relationship newRelation = new Relationship();
        newRelation.setRelatedTo("relatedTo");
        RelationshipData data2 = new RelationshipData();
        ;
        data2.setRelationshipValue("v");
        data2.setRelationshipKey("k");
        newRelation.setRelationshipData(new ArrayList<>());
        newRelation.getRelationshipData().add(data2);

        //when
        AbstractManager.addMissingRelation(relationships, newRelation);
        //verify
        assertEquals(2, relationships.size());
        assertEquals(1, relationships.get(1).getRelationshipData().size());
        assertEquals("k", relationships.get(1).getRelationshipData().get(0).getRelationshipKey());
        assertEquals("v", relationships.get(1).getRelationshipData().get(0).getRelationshipValue());
    }

    class DummyManager extends AbstractManager {

        DummyManager(AAIRestApiProvider aaiRestApiProvider, CbamRestApiProvider cbamRestApiProvider) {
            super(aaiRestApiProvider, cbamRestApiProvider);
        }

        @Override
        protected Logger getLogger() {
            return logger;
        }
    }


}
