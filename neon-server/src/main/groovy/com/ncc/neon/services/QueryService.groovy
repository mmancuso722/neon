/*
 * Copyright 2013 Next Century Corporation
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
 *
 */

package com.ncc.neon.services

import com.ncc.neon.connect.ConnectionInfo
import com.ncc.neon.connect.ConnectionManager
import com.ncc.neon.connect.DataSources
import com.ncc.neon.metadata.model.ColumnMetadata
import com.ncc.neon.query.*
import com.ncc.neon.query.clauses.WhereClause
import com.ncc.neon.query.executor.QueryExecutor
import com.ncc.neon.query.filter.GlobalFilterState
import com.ncc.neon.query.result.QueryResult
import com.ncc.neon.query.result.ArrayCountPair

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import javax.ws.rs.*
import javax.ws.rs.core.MediaType

/**
 * Service for executing queries against an arbitrary data store.
 */

@Component
@Path("/queryservice")
class QueryService {

    @Autowired
    QueryExecutorFactory queryExecutorFactory

    @Autowired
    MetadataResolver metadataResolver

    @Autowired
    ConnectionManager connectionManager

    @Autowired
    GlobalFilterState filterState

    /**
     * Executes a query against the supplied connection.
     * This takes into account the user's current filters so the results will be limited by these if they exist.
     * @param host The host the database is running on
     * @param databaseType the type of database
     * @param includeFilters If filters should be ignored and all data should be returned. Defaults to false.
     * @param selectionOnly If only data that is currently selected should be returned. Defaults to false.
     * @param ignoredFilterIds If any specific filters should be ignored (only used if includeFilters is false)
     * @param query The query being executed
     * @return The result of the query
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("query/{host}/{databaseType}")
    QueryResult executeQuery(@PathParam("host") String host,
                             @PathParam("databaseType") String databaseType,
                             @DefaultValue("false") @QueryParam("ignoreFilters") boolean ignoreFilters,
                             @DefaultValue("false") @QueryParam("selectionOnly") boolean selectionOnly,
                             @QueryParam("ignoredFilterIds") Set<String> ignoredFilterIds,
                             Query query) {
        return execute(host, databaseType, query, new QueryOptions(ignoreFilters: ignoreFilters,
                selectionOnly: selectionOnly, ignoredFilterIds: (ignoredFilterIds ?: ([] as Set))))
    }

    /**
     * Executes a group of queries against the supplied connection
     * This takes into account the user's current filters so the results will be limited by these if they exist.
     * @param host The host the database is running on
     * @param databaseType the type of database
     * @param includeFilters If filters should be ignored and all data should be returned. Defaults to false.
     * @param selectionOnly If only data that is currently selected should be returned. Defaults to false.
     * @param query A collection of queries. The results of the queries will be appended together.
     * @return The result of the query
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("querygroup/{host}/{databaseType}")
    QueryResult executeQueryGroup(@PathParam("host") String host,
                                  @PathParam("databaseType") String databaseType,
                                  @DefaultValue("false") @QueryParam("ignoreFilters") boolean ignoreFilters,
                                  @DefaultValue("false") @QueryParam("selectionOnly") boolean selectionOnly,
                                  @QueryParam("ignoredFilterIds") Set<String> ignoredFilterIds,
                                  QueryGroup query) {
        return execute(host, databaseType, query, new QueryOptions(ignoreFilters: ignoreFilters, selectionOnly: selectionOnly,
                ignoredFilterIds: (ignoredFilterIds ?: ([] as Set))))
    }

    /**
     * Get all the columns for tabular datasets from the supplied connection.
     * @param host The host the database is running on
     * @param databaseType the type of database
     * @param databaseName The database containing the data
     * @param tableName The table containing the data
     * @return The result of the query
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("fields/{host}/{databaseType}/{databaseName}/{tableName}")
    List<String> getFields(
            @PathParam("host") String host,
            @PathParam("databaseType") String databaseType,
            @PathParam("databaseName") String databaseName,
            @PathParam("tableName") String tableName) {

        QueryExecutor queryExecutor = getExecutor(host, databaseType)
        return queryExecutor.getFieldNames(databaseName, tableName)
    }

    /**
     * Get all the columns for all the tables from the supplied connection.
     * @param host The host the database is running on
     * @param databaseType the type of database
     * @param databaseName The database containing the data
     * @return The result of the query
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("tablesandfields/{host}/{databaseType}/{databaseName}")
    Map<String, List<String>> getTablesAndFields(
            @PathParam("host") String host,
            @PathParam("databaseType") String databaseType,
            @PathParam("databaseName") String databaseName) {

        List<String> tableNames = getTableNames(host, databaseType, databaseName)
        QueryExecutor queryExecutor = getExecutor(host, databaseType)
        Map<String, List<String>> tablesAndFields = [:]
        tableNames.each { tableName ->
            tablesAndFields[tableName] = queryExecutor.getFieldNames(databaseName, tableName)
        }
        return tablesAndFields
    }

    /**
     * Gets metadata associated with the columns of the table
     * @param databaseName The database containing the data
     * @param tableName The table containing the data
     * @return The list of metadata associated with the columns
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("columnmetadata/{databaseName}/{tableName}")
    List<ColumnMetadata> getColumnMetadata(
            @PathParam("databaseName") String databaseName,
            @PathParam("tableName") String tableName
    ) {
        return metadataResolver.retrieve(databaseName, tableName, [] as Set)
    }

    /**
     * Gets a list of all the databases for the database type/host pair.
     * @param host The host the database is running on
     * @param databaseType the type of database
     * @return The list of database names
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("databasenames/{host}/{databaseType}")
    List<String> getDatabaseNames(@PathParam("host") String host, @PathParam("databaseType") String databaseType) {
        QueryExecutor queryExecutor = getExecutor(host, databaseType)
        return queryExecutor.showDatabases()
    }

    /**
     * Gets a list of all the tables for the supplied connection
     * @param host The host the database is running on
     * @param databaseType the type of database
     * @param database The database that contains the tables
     * @return The list of table names
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("tablenames/{host}/{databaseType}/{database}")
    List<String> getTableNames(@PathParam("host") String host, @PathParam("databaseType") String databaseType, @PathParam("database") String database) {
        QueryExecutor queryExecutor = getExecutor(host, databaseType)
        return queryExecutor.showTables(database)
    }

    /**
     * Gets a sorted list of key, count pairs for an array field in the database
     * @param host The host on which the database is running
     * @param databaseType The type of the database
     * @param databaseName The name of the database
     * @param tableName The name of the collection or table
     * @param fieldName The name of the array field to count
     * @param limit The number of pairs to return (default:  50)
     * @param whereClause The where clause to apply to the array counts query, or null to apply no where clause
     */
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Path("arraycounts/{host}/{databaseType}/{databaseName}/{tableName}/{fieldName}")
    public List<ArrayCountPair> getArrayCounts(@PathParam("host") String host, @PathParam("databaseType") String databaseType, @PathParam("databaseName") String databaseName,
      @PathParam("tableName") String tableName, @PathParam("fieldName") String fieldName, @DefaultValue("50") @QueryParam("limit") int limit, WhereClause whereClause) {
        QueryExecutor queryExecutor = getExecutor(host, databaseType)
        return queryExecutor.getArrayCounts(databaseName, tableName, fieldName, limit, filterState, whereClause)
    }

    private QueryResult execute(String host, String databaseType, def query, QueryOptions options) {
        QueryExecutor queryExecutor = getExecutor(host, databaseType)
        return queryExecutor.execute(query, options, filterState)
    }

    private QueryExecutor getExecutor(String host, String databaseType) {
        return queryExecutorFactory.getExecutor(new ConnectionInfo(host: host, dataSource: databaseType as DataSources))
    }
}
