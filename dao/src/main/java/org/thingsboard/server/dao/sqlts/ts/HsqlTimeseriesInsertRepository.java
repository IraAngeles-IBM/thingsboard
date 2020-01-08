/**
 * Copyright © 2016-2020 The Thingsboard Authors
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
package org.thingsboard.server.dao.sqlts.ts;

import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.thingsboard.server.dao.model.sqlts.ts.TsKvEntity;
import org.thingsboard.server.dao.sqlts.AbstractTimeseriesInsertRepository;
import org.thingsboard.server.dao.util.HsqlDao;
import org.thingsboard.server.dao.util.SqlTsDao;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;

@SqlTsDao
@HsqlDao
@Repository
@Transactional
public class HsqlTimeseriesInsertRepository extends AbstractTimeseriesInsertRepository<TsKvEntity> {

    private static final String TS_KV_CONSTRAINT = "(ts_kv.entity_type=A.entity_type AND ts_kv.entity_id=A.entity_id AND ts_kv.key=A.key AND ts_kv.ts=A.ts)";

    private static final String INSERT_OR_UPDATE_BOOL_STATEMENT = getInsertOrUpdateStringHsql(TS_KV_TABLE, TS_KV_CONSTRAINT, BOOL_V, HSQL_ON_BOOL_VALUE_UPDATE_SET_NULLS);
    private static final String INSERT_OR_UPDATE_STR_STATEMENT = getInsertOrUpdateStringHsql(TS_KV_TABLE, TS_KV_CONSTRAINT, STR_V, HSQL_ON_STR_VALUE_UPDATE_SET_NULLS);
    private static final String INSERT_OR_UPDATE_LONG_STATEMENT = getInsertOrUpdateStringHsql(TS_KV_TABLE, TS_KV_CONSTRAINT, LONG_V, HSQL_ON_LONG_VALUE_UPDATE_SET_NULLS);
    private static final String INSERT_OR_UPDATE_DBL_STATEMENT = getInsertOrUpdateStringHsql(TS_KV_TABLE, TS_KV_CONSTRAINT, DBL_V, HSQL_ON_DBL_VALUE_UPDATE_SET_NULLS);

    private static final String INSERT_OR_UPDATE =
            "MERGE INTO ts_kv USING(VALUES ?, ?, ?, ?, ?, ?, ?, ?) " +
                    "T (entity_type, entity_id, key, ts, bool_v, str_v, long_v, dbl_v) " +
                    "ON (ts_kv.entity_type=T.entity_type " +
                    "AND ts_kv.entity_id=T.entity_id " +
                    "AND ts_kv.key=T.key " +
                    "AND ts_kv.ts=T.ts) " +
                    "WHEN MATCHED THEN UPDATE SET ts_kv.bool_v = T.bool_v, ts_kv.str_v = T.str_v, ts_kv.long_v = T.long_v, ts_kv.dbl_v = T.dbl_v " +
                    "WHEN NOT MATCHED THEN INSERT (entity_type, entity_id, key, ts, bool_v, str_v, long_v, dbl_v) " +
                    "VALUES (T.entity_type, T.entity_id, T.key, T.ts, T.bool_v, T.str_v, T.long_v, T.dbl_v);";

    @Override
    public void saveOrUpdate(TsKvEntity entity) {
        processSaveOrUpdate(entity, INSERT_OR_UPDATE_BOOL_STATEMENT, INSERT_OR_UPDATE_STR_STATEMENT, INSERT_OR_UPDATE_LONG_STATEMENT, INSERT_OR_UPDATE_DBL_STATEMENT);
    }

    @Override
    public void saveOrUpdate(List<TsKvEntity> entities) {
        jdbcTemplate.batchUpdate(INSERT_OR_UPDATE, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                ps.setString(1, entities.get(i).getEntityType().name());
                ps.setString(2, entities.get(i).getEntityId());
                ps.setString(3, entities.get(i).getKey());
                ps.setLong(4, entities.get(i).getTs());

                if (entities.get(i).getBooleanValue() != null) {
                    ps.setBoolean(5, entities.get(i).getBooleanValue());
                } else {
                    ps.setNull(5, Types.BOOLEAN);
                }

                ps.setString(6, entities.get(i).getStrValue());

                if (entities.get(i).getLongValue() != null) {
                    ps.setLong(7, entities.get(i).getLongValue());
                } else {
                    ps.setNull(7, Types.BIGINT);
                }

                if (entities.get(i).getDoubleValue() != null) {
                    ps.setDouble(8, entities.get(i).getDoubleValue());
                } else {
                    ps.setNull(8, Types.DOUBLE);
                }
            }

            @Override
            public int getBatchSize() {
                return entities.size();
            }
        });
    }

    @Override
    protected void saveOrUpdateBoolean(TsKvEntity entity, String query) {
        entityManager.createNativeQuery(query)
                .setParameter("entity_type", entity.getEntityType().name())
                .setParameter("entity_id", entity.getEntityId())
                .setParameter("key", entity.getKey())
                .setParameter("ts", entity.getTs())
                .setParameter("bool_v", entity.getBooleanValue())
                .executeUpdate();
    }

    @Override
    protected void saveOrUpdateString(TsKvEntity entity, String query) {
        entityManager.createNativeQuery(query)
                .setParameter("entity_type", entity.getEntityType().name())
                .setParameter("entity_id", entity.getEntityId())
                .setParameter("key", entity.getKey())
                .setParameter("ts", entity.getTs())
                .setParameter("str_v", entity.getStrValue())
                .executeUpdate();
    }

    @Override
    protected void saveOrUpdateLong(TsKvEntity entity, String query) {
        entityManager.createNativeQuery(query)
                .setParameter("entity_type", entity.getEntityType().name())
                .setParameter("entity_id", entity.getEntityId())
                .setParameter("key", entity.getKey())
                .setParameter("ts", entity.getTs())
                .setParameter("long_v", entity.getLongValue())
                .executeUpdate();
    }

    @Override
    protected void saveOrUpdateDouble(TsKvEntity entity, String query) {
        entityManager.createNativeQuery(query)
                .setParameter("entity_type", entity.getEntityType().name())
                .setParameter("entity_id", entity.getEntityId())
                .setParameter("key", entity.getKey())
                .setParameter("ts", entity.getTs())
                .setParameter("dbl_v", entity.getDoubleValue())
                .executeUpdate();
    }
}