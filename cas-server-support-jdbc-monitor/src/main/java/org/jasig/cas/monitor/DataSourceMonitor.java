/*
 * Licensed to Apereo under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Apereo licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License.  You may obtain a
 * copy of the License at the following location:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.jasig.cas.monitor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import javax.sql.DataSource;
import javax.validation.constraints.NotNull;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Monitors a data source that describes a single connection or connection pool to a database.
 *
 * @author Marvin S. Addison
 * @since 3.5.1
 */
@Component("dataSourceMonitor")
public class DataSourceMonitor extends AbstractPoolMonitor {

    @NotNull
    private JdbcTemplate jdbcTemplate;

    @NotNull
    @Value("${datasource.monitor.validation.query:SELECT 1}")
    private String validationQuery;

    /**
     * Creates a new instance that monitors the given data source.
     *
     * @param dataSource Data source to monitor.
     */
    @Autowired

    public DataSourceMonitor(@Qualifier("monitorDataSource") @Nullable final DataSource dataSource) {
        if (dataSource != null) {
            this.jdbcTemplate = new JdbcTemplate(dataSource);
        } else {
            logger.debug("No data source is defined to monitor");
        }
    }


    /**
     * Sets the validation query used to monitor the data source. The validation query should return
     * at least one result; otherwise results are ignored.
     *
     * @param query Validation query that should be as efficient as possible.
     */
    public void setValidationQuery(final String query) {
        this.validationQuery = query;
    }


    @Override
    protected StatusCode checkPool() throws Exception {
        return this.jdbcTemplate.query(this.validationQuery, new ResultSetExtractor<StatusCode>() {
            @Override
            public StatusCode extractData(final ResultSet rs) throws SQLException {
                if (rs.next()) {
                    return StatusCode.OK;
                }
                return StatusCode.WARN;
            }
        });
    }


    @Override
    protected int getIdleCount() {
        return PoolStatus.UNKNOWN_COUNT;
    }


    @Override
    protected int getActiveCount() {
        return PoolStatus.UNKNOWN_COUNT;
    }
}