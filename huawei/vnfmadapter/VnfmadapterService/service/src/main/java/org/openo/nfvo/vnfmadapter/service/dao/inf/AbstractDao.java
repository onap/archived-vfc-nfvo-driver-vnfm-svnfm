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

package org.openo.nfvo.vnfmadapter.service.dao.inf;

import org.apache.ibatis.session.SqlSession;

/**
 * database abstract class to get the MapperManager.
 */
public class AbstractDao {

    private SqlSession session;

    protected AbstractDao() {
        //Constructor
    }

    public SqlSession getSession() {
        return session;
    }

    public void setSession(SqlSession session) {
        this.session = session;
    }

    /**
     * get Mybatis Mapper.
     *
     * @param type : The class of the instance
     * @param <T> : The type of the instance
     * @return Mapper : The instance
     */
    public <T> T getMapperManager(Class<T> type) {
        return (T)getSession().getMapper(type);
    }
}
