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

package org.onap.vfc.nfvo.vnfm.svnfm.vnfmadapter.service.entity;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.onap.vfc.nfvo.vnfm.svnfm.vnfmadapter.service.entity.Vnfm;

public class VnfmTest {
    Vnfm vnfm;

    @Before
    public void setUp(){
        vnfm = new Vnfm();
        vnfm.setId("123");
        vnfm.setId("v1");
        vnfm.setVnfdId("1234");
        vnfm.setVnfPackageId("abcd");
    }

    @Test
    public void hashCodeTest(){
        Vnfm tempVnfm = new Vnfm();
        tempVnfm.setId("123");
        tempVnfm.setId("v1");
        tempVnfm.setVnfdId("1234");
        tempVnfm.setVnfPackageId("abcd");
        assertTrue(vnfm.hashCode() == tempVnfm.hashCode());
    }

    @Test
    public void equalsTest1(){
        Vnfm tempVnfm = new Vnfm();
        tempVnfm.setId("123");
        tempVnfm.setId("v1");
        tempVnfm.setVnfdId("1234");
        tempVnfm.setVnfPackageId("abcd");
        assertTrue(vnfm.equals(tempVnfm));
    }

    @Test
    public void testProps(){
        Vnfm tempVnfm = new Vnfm();
        tempVnfm.getId();
        tempVnfm.setId("123");
        tempVnfm.getVersion();
        tempVnfm.setVersion("1234");
        tempVnfm.getVnfdId();
        tempVnfm.setVnfdId("123");
        tempVnfm.getVnfPackageId();
        tempVnfm.setVnfPackageId("123");
        tempVnfm.toString();
        assertTrue(true);
    }


    @Test
    public void equalsTest2(){
        assertTrue(vnfm.equals(vnfm));
    }

    @Test
    public void equalsTest3(){

        assertTrue(!vnfm.equals(null));
    }
    @Test
    public void equalsTest4(){

        assertTrue(!vnfm.equals(new Object()));
    }
    @Test
    public void equalsTest5(){

        assertTrue(!vnfm.equals(new Object()));
    }

}
