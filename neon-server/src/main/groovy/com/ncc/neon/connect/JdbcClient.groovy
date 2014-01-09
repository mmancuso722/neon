package com.ncc.neon.connect

import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.sql.*

/*
 *
 *  ************************************************************************
 *  Copyright (c), 2013 Next Century Corporation. All Rights Reserved.
 *
 *  This software code is the exclusive property of Next Century Corporation and is
 *  protected by United States and International laws relating to the protection
 *  of intellectual property. Distribution of this software code by or to an
 *  unauthorized party, or removal of any of these notices, is strictly
 *  prohibited and punishable by law.
 *
 *  UNLESS PROVIDED OTHERWISE IN A LICENSE AGREEMENT GOVERNING THE USE OF THIS
 *  SOFTWARE, TO WHICH YOU ARE AN AUTHORIZED PARTY, THIS SOFTWARE CODE HAS BEEN
 *  ACQUIRED BY YOU "AS IS" AND WITHOUT WARRANTY OF ANY KIND. ANY USE BY YOU OF
 *  THIS SOFTWARE CODE IS AT YOUR OWN RISK. ALL WARRANTIES OF ANY KIND, EITHER
 *  EXPRESSED OR IMPLIED, INCLUDING, WITHOUT LIMITATION, IMPLIED WARRANTIES OF
 *  MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE, ARE HEREBY EXPRESSLY
 *  DISCLAIMED.
 *
 *  PROPRIETARY AND CONFIDENTIAL TRADE SECRET MATERIAL NOT FOR DISCLOSURE OUTSIDE
 *  OF NEXT CENTURY CORPORATION EXCEPT BY PRIOR WRITTEN PERMISSION AND WHEN
 *  RECIPIENT IS UNDER OBLIGATION TO MAINTAIN SECRECY.
 * /
 */

/**
 * Wrapper for JDBC API
 */
//We have to suppress this warning in order to load the the JDBC driver via DriverManager. See NEON-459
@SuppressWarnings("SynchronizedMethod")
class JdbcClient implements ConnectionClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(JdbcClient)

    /** used to match the limit clause */
    private static final def LIMIT_REGEX = /(?i)(LIMIT\s+)(\d+)/

    /** matches fields that are escaped because they start with a special character */
    private static final def ESCAPED_FIELD_REGEX = /`(.*)`/

    private Connection connection

    JdbcClient(Connection connection) {
        this.connection = connection
    }

    /**
     * Each jdbcClient instance is created per session,
     * This method is synchronized because a user cannot perform multiple simultaneous queries
     * without hive blowing up.
     * @param offset An optional number of rows to skip in the result set. This is provided as a parameter to
     * execute query since not all JDBC drivers (namely hive) support doing this as part of the query
     */
    synchronized List executeQuery(String query, int offset = 0) {
        Statement statement
        ResultSet resultSet
        try {
            statement = connection.createStatement()
            resultSet = statement.executeQuery(addOffsetToLimitQuery(query, offset))
            return createMappedValuesFromResultSet(resultSet, offset)
        }
        finally {
            resultSet?.close()
            statement?.close()
        }

        return []
    }

    /**
     * When an offset is specified and a limit is used, we need to return the offset plus the limit because
     * we're doing the offset in memory. This modifies the query string to do that.
     * @param query
     * @param offset
     * @return
     */
    private static String addOffsetToLimitQuery(String query, int offset) {
        if (offset > 0) {
            // replace the limit X clause with limit X+offset so we can still return the correct number
            // of records
            def matcher = (query =~ LIMIT_REGEX)
            if (matcher) {
                int limit = matcher[0][2].toInteger()
                String modified = matcher.replaceAll("${matcher[0][1]}${limit + offset}")
                return modified
            }
        }
        return query

    }

    private List createMappedValuesFromResultSet(ResultSet resultSet, int offset) {
        List resultList = []
        ResultSetMetaData metadata = resultSet.metaData
        int columnCount = metadata.columnCount
        if (moveCursor(resultSet, offset)) {
            while (resultSet.next()) {
                def result = [:]
                for (ii in 1..columnCount) {
                    // NEON-895/SHARK-211 specifies that escape characters are not properly removed.
                    String columnName = metadata.getColumnName(ii).replaceAll(ESCAPED_FIELD_REGEX, '$1')
                    result[columnName] = getValue(metadata, resultSet, ii)
                }
                resultList.add(result)
            }
        }
        return resultList
    }

    /**
     * Moves the cursor in the result set by the specified offset
     * @param resultSet The result set whose cursor is being advanced - this may be modified
     * @param offset The number of records to offset it by
     * @return true if there are still potentially more records available after the cursor is moved, false if not. This
     * value may differ from resultSet.next() if an offset larger than the number of records is provided - the
     * cursor may not actually be advanced to save time. When this method returns true, use next() to determine if there
     * are more records to iterate over. When this method returns false, assume there are no more records.
     */
    @SuppressWarnings("CatchException")
    @SuppressWarnings("MethodSize") // there are only a couple of lines of code in here, but a large comment block explaining why we implemented this how we did
    private static boolean moveCursor(ResultSet resultSet, int offset) {
        if (offset > 0) {
            try {
                // if the call to "absolute" succeeds, there are still results left
                // if it does not succeed, it means we went past the end of the result set (or before the beginning
                // which wouldn't make sense in this case). When this happens, there are no more results to iterate
                // over, but rather than actually iterating through the result set return false indicating that so
                // the result set is ignored (and "last" is not supported all jdbc drivers, hive for example)
                return resultSet.absolute(offset)
            }
            // happens if this is not supported, but implementations may throw different types of exceptions so
            // catch all of them
            catch (Exception e) {
                LOGGER.debug("ResultSet absolute call failed with exception {}. Using manual offset.", e)
                advanceResultSet(resultSet, offset)

                // always return true from here even if the result set is actually at the end. next() will return
                // false in the cases where there are no results left and will not take any real time to do so
                return true
            }
        }
        // no offset
        return true
    }

    private static void advanceResultSet(ResultSet resultSet, int offset) {
        int count = 0
        while (count < offset && resultSet.next()) {
            count++
        }
    }



    private def getValue(ResultSetMetaData metadata, ResultSet resultSet, int index) {
        def val = resultSet.getObject(index)
        // timestamps are time-zone less, but we assume UTC
        if (metadata.getColumnType(index) == Types.TIMESTAMP) {
            // use joda time because not all jdbc drivers (e.g. hive) support timezones - they return in local time
            val = new DateTime(val.time).withZoneRetainFields(DateTimeZone.UTC).toDate()
        }
        return val
    }

    /**
     * Each jdbcClient instance is created per session,
     * This method is synchronized because a user cannot perform multiple simultaneous queries
     * without hive blowing up.
     */
    synchronized void execute(String query) {
        Statement statement
        try {
            statement = connection.createStatement()
            statement.execute(query)
        }
        finally {
            statement?.close()
        }
    }

    List<String> getColumnNames(String dataStoreName, String databaseName) {
        String query = "select * from ${dataStoreName}.${databaseName} limit 1"

        List list = executeQuery(query)
        if (!list) {
            return []
        }
        list[0].keySet().asList()
    }

    @Override
    void close() {
        connection?.close()
        connection = null
    }

}
