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

package com.ncc.neon.query.mongo
import com.mongodb.BasicDBObject
import com.mongodb.DB
import com.mongodb.DBObject
import com.mongodb.MongoClient
import com.ncc.neon.connect.ConnectionManager
import com.ncc.neon.query.executor.AbstractQueryExecutor
import com.ncc.neon.query.Query
import com.ncc.neon.query.QueryOptions
import com.ncc.neon.query.result.QueryResult
import com.ncc.neon.query.result.ArrayCountPair
import com.ncc.neon.query.filter.FilterState
import com.ncc.neon.query.filter.SelectionState
import com.ncc.neon.query.filter.DataSet
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

/**
 * Executes queries against a mongo data store
 */
@Component
class MongoQueryExecutor extends AbstractQueryExecutor {

    private static final Logger LOGGER = LoggerFactory.getLogger(MongoQueryExecutor)

    @Autowired
    private FilterState filterState

    @Autowired
    private SelectionState selectionState

    @Autowired
    private ConnectionManager connectionManager

    @Override
    QueryResult doExecute(Query query, QueryOptions options) {
        AbstractMongoQueryWorker worker = createMongoQueryWorker(query)
        MongoConversionStrategy mongoConversionStrategy = new MongoConversionStrategy(filterState: filterState, selectionState: selectionState)
        MongoQuery mongoQuery = mongoConversionStrategy.convertQuery(query, options)
        return worker.executeQuery(mongoQuery)
    }

    @Override
    List<String> showDatabases() {
        LOGGER.debug("Executing getDatabaseNames")
        mongo.databaseNames
    }

    @Override
    List<String> showTables(String dbName) {
        DB database = mongo.getDB(dbName)
        LOGGER.debug("Executing getCollectionNames on database {}", dbName)
        database.getCollectionNames().collect { it }
    }

    @Override
    List<String> getFieldNames(String databaseName, String tableName) {
        def db = mongo.getDB(databaseName)
        def collection = db.getCollection(tableName)
        def result = collection.findOne()
        return (result?.keySet() as List) ?: []
    }

    private AbstractMongoQueryWorker createMongoQueryWorker(Query query) {
        if (query.isDistinct) {
            LOGGER.trace("Using distinct mongo query worker")
            return new DistinctMongoQueryWorker(mongo)
        } else if (query.aggregates || query.groupByClauses) {
            if (query.groupByClauses.size() == 0 && query.aggregates.size() == 1) {
                def aggregate = query.aggregates[0]
                if (aggregate.operation == 'count' && aggregate.field == '*') {
                    LOGGER.trace("Using simple count query worker")
                    return new SimpleMongoCountWorker(mongo)
                }
            }
            LOGGER.trace("Using aggregate mongo query worker")
            return new AggregateMongoQueryWorker(mongo)
        }
        LOGGER.trace("Using simple mongo query worker")
        return new SimpleMongoQueryWorker(mongo)
    }

    private MongoClient getMongo(){
        connectionManager.connection.mongo
    }

    DBObject mergeNeonFilters(DBObject query, String databaseName, String collectionName ) {
        DataSet dataSet = new DataSet(databaseName: databaseName, tableName: collectionName)
        List neonFiltersAndSelection = []

        neonFiltersAndSelection.addAll(MongoConversionStrategy.createWhereClausesForFilters(dataSet, filterState))

        // the demo only shows selected data - right now selection is basically a temporary filter so only show selected
        neonFiltersAndSelection.addAll(MongoConversionStrategy.createWhereClausesForFilters(dataSet, selectionState))

        // TODO: Do we need to flatten  - the lists added to this should be empty, but it looks like this list contains 2 empty list objects if not flattened first
        neonFiltersAndSelection = neonFiltersAndSelection.flatten()

        if (neonFiltersAndSelection) {
            DBObject matchNeonFilters = MongoConversionStrategy.buildMongoWhereClause((List) neonFiltersAndSelection)
            return new BasicDBObject('$and', [matchNeonFilters, query])
        }
        // no neon filters/selection, just return the original query
        return query
    }

    List<ArrayCountPair> getArrayCounts(String databaseName, String tableName, String field, int limit) {
        ArrayCountQueryWorker worker = new ArrayCountQueryWorker(mongo)

        //work out query weirdness ==== do the whole merge thing

        return worker.executeQuery(query)
    }
}
