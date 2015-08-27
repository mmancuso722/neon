/*
 * Copyright 2015 Next Century Corporation
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

package com.ncc.neon.userimport

import com.ncc.neon.userimport.exceptions.BadSheetException
import com.ncc.neon.userimport.exceptions.UnsupportedFiletypeException
import com.ncc.neon.userimport.readers.SheetReader
import com.ncc.neon.userimport.readers.SheetReaderFactory

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component

import com.mongodb.MongoClient
import com.mongodb.DB
import com.mongodb.DBCollection
import com.mongodb.DBCursor
import com.mongodb.DBObject
import com.mongodb.BasicDBObject
import com.mongodb.gridfs.GridFS
import com.mongodb.gridfs.GridFSDBFile

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import com.ncc.neon.userimport.types.ImportUtilities
import com.ncc.neon.userimport.types.ConversionFailureResult
import com.ncc.neon.userimport.types.FieldType
import com.ncc.neon.userimport.types.FieldTypePair


/**
 * Houses the asynchronous methods that do the actual labor of importing data into mongo data stores, as well as a number
 * of helper methods and methods to handle different types of input.
 */
@Component
class MongoImportHelperProcessor implements ImportHelperProcessor {

    // For use by the various loadAndConvert methods. Updating loading/conversion progress is relatively expensive, so only
    // do it every so many records. For now, 137 was chosen because it's a prime number whose multiples look fairly random.
    private static final int UPDATE_FREQUENCY = 137

    private static final Logger LOGGER = LoggerFactory.getLogger(MongoImportHelperProcessor)

    @Autowired
    SheetReaderFactory sheetReaderFactory

    @Async
    @Override
    void processTypeGuesses(String host, String uuid) {
        MongoClient mongo = new MongoClient(host)
        GridFS gridFS = new GridFS(mongo.getDB(ImportUtilities.MONGO_UPLOAD_DB_NAME))
        GridFSDBFile gridFile = gridFS.findOne([identifier: uuid] as BasicDBObject)
        String fileType = gridFile.get("fileType")
        try {
            switch(fileType) {
                case "csv":
                    processTypeGuessesCSV(gridFile)
                    break
                case "xlsx":
                    processTypeGuessesExcel(gridFile)
                    break
                default:
                    throw new UnsupportedFiletypeException("Can't parse files of type ${fileType}!")
            }
        } catch(UnsupportedFiletypeException e){
            LOGGER.error(e.getMessage(), e)
        } finally {
            mongo.close()
        }
    }

    /**
     * Gets type guesses for fields of information stored in CSV format and stores them as metadata in the GridFSDBFile that
     * stores the CSV file.
     * @param file The GridFSDBFile storing the CSV file for which to make field type guesses.
     */
    private void processTypeGuessesCSV(GridFSDBFile file) {
        SheetReader reader = sheetReaderFactory.getSheetReader("csv").initialize(file.getInputStream())
        if(!reader) {
            throw new BadSheetException("No records in this file to read!")
        }
        List fields = reader.getSheetFieldNames()
        Map fieldsAndValues = [:]
        fields.each { field ->
            fieldsAndValues.put(field, [])
        }
        for(int counter = 0; counter < ImportUtilities.NUM_TYPE_CHECKED_RECORDS && reader.hasNext(); counter++) {
            List record = reader.next()
            for(int field = 0; field < fields.size() && field < record.size(); field++) {
                if(record[field]) {
                    fieldsAndValues[(fields[field])] << record[field]
                }
            }
        }
        updateFile(file, [programGuesses: fieldTypePairListToStorageMap(ImportUtilities.getTypeGuesses(fieldsAndValues))])
        reader.close()
    }

    /**
     * Gets type guesses for fields of information stored in XLSX format and stores them as metadata in the GridFSDBFile that
     * stores the XLSX file.
     * @param file The GridFSDBFile storing the XLSX file for which to make field type guesses.
     */
    private void processTypeGuessesExcel(GridFSDBFile file) {
        SheetReader reader
        try {
            reader = sheetReaderFactory.getSheetReader("xlsx").initialize(file.getInputStream())
            if(!reader) {
                throw new BadSheetException("No records in this file to read!")
            }
            List fields = reader.getSheetFieldNames()
            Map fieldsAndValues = [:]
            fields.each { field ->
                fieldsAndValues.put(field, [])
            }
            for(int count = 0; count < ImportUtilities.NUM_TYPE_CHECKED_RECORDS && reader.hasNext(); count++) {
                List record = reader.next()
                for(int field = 0; field < fields.size(); field++) {
                    if(record[field]) {
                        fieldsAndValues[(fields[field])] << record[field]
                    }
                }
            }
            updateFile(file, [programGuesses: fieldTypePairListToStorageMap(ImportUtilities.getTypeGuesses(fieldsAndValues))])
        }
        finally {
            reader?.close()
        }
    }

    @Async
    @Override
    void processLoadAndConvert(String host, String uuid, String dateFormat, List<FieldTypePair> fieldTypePairs) {
        MongoClient mongo = new MongoClient(host)
        GridFSDBFile gridFile = new GridFS(mongo.getDB(ImportUtilities.MONGO_UPLOAD_DB_NAME)).findOne([identifier: uuid] as BasicDBObject)
        String fileType = gridFile.get("fileType")
        updateFile(gridFile, [complete: false, numCompleted: 0])
        try {
            if(alreadyLoaded(mongo, gridFile)) {
                processConvert(mongo, gridFile, dateFormat, fieldTypePairs)
                return
            }
            switch(fileType) {
                case "csv":
                    processLoadAndConvertCSV(mongo, gridFile, dateFormat, fieldTypePairs)
                    break
                case "xlsx":
                    processLoadAndConvertExcel(mongo, gridFile, dateFormat, fieldTypePairs)
                    break
                default:
                    mongo.close()
                    throw new UnsupportedFiletypeException("Can't parse files of type ${fileType}!")
            }
        } catch(UnsupportedFiletypeException e){
            LOGGER.error(e.getMessage(), e)
        } finally {
            mongo.close()
        }
    }

    /**
     * Reads a raw CSV file from the given GridFSDFile line by line, grabbing records, converting their fields into the types
     * given in fieldTypePairs, and then storing them in a database on the given mongo instance. The database's name is
     * generated using the userName and prettyName metadata in the file. This method assumes that there is only one record
     * per line of the file, and that the file contains nothing but records and a list of field names on the first line.
     * If any fields fail to convert for at least one record, these fields and the type they were attempting to be converted to are
     * stored as metadata in the given file.
     * @param mongo The mongo instance on which the database in which to convert records is located.
     * @param file The GridFSDBFile containing metadata that allows for finding of the database in which to convert records.
     * @param dateFormat The date format string to be used when attempting to convert any record to a Date.
     * @param fieldTypePairs A list of field names and what types they should be converted to.
     */
    private void processLoadAndConvertCSV(MongoClient mongo, GridFSDBFile file, String dateFormat, List<FieldTypePair> fieldTypePairs) {
        int numCompleted = 0
        SheetReader reader = sheetReaderFactory.getSheetReader("csv").initialize(file.getInputStream())
        if(!reader) {
            throw new BadSheetException("No records in this file to read!")
        }
        Map fieldsToTypes = fieldTypePairListToMap(fieldTypePairs)
        Set<FieldTypePair> failedFields = [] as Set
        DBCollection collection = mongo.getDB(getDatabaseName(file.get("userName"), file.get("prettyName"))).getCollection(ImportUtilities.MONGO_USERDATA_COLL_NAME)
        List fieldNamesInOrder = reader.getSheetFieldNames()
        reader.each { record ->
            addRecord(collection, fieldNamesInOrder, record, fieldsToTypes, dateFormat).each { ftPair ->
                failedFields << ftPair
                fieldsToTypes.put(ftPair.name, FieldType.STRING) // If a field failed to convert, set its type to string to prevent exceptions being thrown for that field on future records.
            }
            if(numCompleted++ % UPDATE_FREQUENCY == 0) {
                updateFile(file, [numCompleted: numCompleted])
            }
        }
        updateFile(file, [numCompleted: numCompleted, failedFields: fieldTypePairListToStorageMap(failedFields as List)])
        addMetadata(mongo, file)
        reader.close()
    }

    /**
     * Reads a raw XLSX file from the given GridFSDFile into a temporary file on disk, and then reads through it line by line,
     * grabbing records, converting their fields into the types given in fieldTypePairs, and then storing them in a database
     * on the given mongo instance. The database's name is generated using the userName and prettyName metadata in the file.
     * This method assumes that there is only one record per line of the file, and that the file contains nothing but records
     * and a list of field names on the first line.
     * If any fields fail to convert for at least one record, these fields and the type they were attempting to be converted to are
     * stored as metadata in the given file.
     * @param mongo The mongo instance on which the database in which to convert records is located.
     * @param file The GridFSDBFile containing metadata that allows for finding of the database in which to convert records.
     * @param dateFormat The date format string to be used when attempting to convert any record to a Date.
     * @param fieldTypePairs A list of field names and what types they should be converted to.
     */
    private void processLoadAndConvertExcel(MongoClient mongo, GridFSDBFile file, String dateFormat, List<FieldTypePair> fieldTypePairs) {
        SheetReader reader
        try {
            reader = sheetReaderFactory.getSheetReader("xlsx").initialize(file.getInputStream())
            if(!reader) {
                throw new BadSheetException("No records in this file to read!")
            }
            int numCompleted = 0
            Map fieldsToTypes = fieldTypePairListToMap(fieldTypePairs)
            Set<FieldTypePair> failedFields = [] as Set
            DBCollection collection = mongo.getDB(getDatabaseName(file.get("userName"), file.get("prettyName"))).getCollection(ImportUtilities.MONGO_USERDATA_COLL_NAME)
            List fieldNamesInOrder = reader.getSheetFieldNames()
            reader.each { record ->
                addRecord(collection, fieldNamesInOrder, record, fieldsToTypes, dateFormat).each { failedFTPair ->
                    failedFields << failedFTPair
                    fieldsToTypes.put(failedFTPair.name, FieldType.STRING) // If a field failed to convert, set its type to string to avoid exceptions on future records.
                }
                if(numCompleted++ % UPDATE_FREQUENCY == 0) {
                    updateFile(file, [numCompleted: numCompleted])
                }
            }
            updateFile(file, [numCompleted: numCompleted, failedFields: fieldTypePairListToStorageMap(failedFields as List)])
            addMetadata(mongo, file)
        }
        finally {
            reader?.close()
        }
    }

    /**
     * Converts the fields of records in a database without adding any new ones to it. Used when the user messes up with their type
     * guesses on the first try to prevent multiple-loading of records into a database. Records progress as metadata in the given
     * GridFSDBFile as it goes along.
     * If any fields fail to convert for at least one record, these fields and the type they were attempting to be converted to are
     * stored as metadata in the given file.
     * @param mongo The mongo instance on which the database in which to convert records is located.
     * @param file The GridFSDBFile containing metadata that allows for finding of the database in which to convert records.
     * @param dateFormat The date format string to be used when attempting to convert any record to a Date.
     * @param fieldTypePairs A list of field names and what types they should be converted to.
     */
    private void processConvert(MongoClient mongo, GridFSDBFile file, String dateFormat, List<FieldTypePair> fieldTypePairs) {
        int numCompleted = 0
        updateFile(file, [numCompleted: numCompleted, complete: false])
        List<FieldTypePair> pairs = fieldTypePairs
        DBCollection collection = mongo.getDB(getDatabaseName(file.get("userName"), file.get("prettyName"))).getCollection(ImportUtilities.MONGO_USERDATA_COLL_NAME)
        DBCursor cursor = collection.find()
        Set<FieldTypePair> failedFields = [] as Set
        cursor.each { record ->
            pairs.each { ftPair ->
                String stringValue = record.get(ftPair.name) as String
                if(!stringValue || (stringValue.length() == 4 && stringValue =~ /(?i)none|null(?-i)/) &&
                    (ftPair.type == FieldType.INTEGER || ftPair.type == FieldType.LONG || ftPair.type == FieldType.DOUBLE || ftPair.type == FieldType.FLOAT)) {
                    return // Don't add anything if the field is null, an empty string, or a "non-existent" value for numeric types.
                }
                Object objectValue = ImportUtilities.convertValueToType(stringValue, ftPair.type, dateFormat)
                if(objectValue instanceof ConversionFailureResult) {
                    failedFields.add(ftPair)
                } else {
                    record.put(ftPair.name, objectValue)
                }
            }
            failedFields.each { ftPair ->
                pairs.remove(ftPair) // If a field failed to convert, stop trying to convert it on future records.
            }
            if(numCompleted++ % UPDATE_FREQUENCY == 0) {
                updateFile(file, [numCompleted: numCompleted])
            }
        }
        updateFile(file, [complete: true, numCompleted: numCompleted, failedFields: fieldTypePairListToStorageMap(failedFields as List)])
    }

    /**
     * Helper method that adds a single record to a mongo collection, given a list of field names and string representations of
     * values, a map of which fields should be converted to which types, and a date format string to use when attempting
     * to convert field values to dates. The field name and value lists must be in order with respect to each other. That is, the
     * field name at index 0 of one list must match up with the value at index 0 of the other.
     * Returns a list of any fields that failed to convert, as well as what type they were attempting to be converted to.
     * @param collection The DBCollection into which to insert the record.
     * @param namesInOrder A list containing the names of fields the record could have (could because it's possible for some of those
     * fields to be null). This list must be in order with respect to the values list for this method to work properly.
     * @param valuesInOrder A list containing the values of fields this record has (any number of these values can be null). This list
     * must be in order with respect to the field names list in order for this method to work properly.
     * @param namesToTypes A map of field names to which type those fields should be converted to.
     * @param dateFormat A date formatting string to be used when attempting to convert a field into a Date. This can be null.
     * @return A list of FieldTypePairs containing the names of any fields which failed to convert, as well as the types they were
     * attempting to be converted to.
     */
    private List<FieldTypePair> addRecord(DBCollection collection, List namesInOrder, List valuesInOrder, Map namesToTypes, String dateFormat) {
        List<FieldTypePair> failedFields = []
        DBObject record = new BasicDBObject()
        for(int field = 0; field < namesInOrder.size() && field < valuesInOrder.size(); field++) {
            String stringValue = valuesInOrder[field]
            FieldType type = namesToTypes.get(namesInOrder[field])
            if(!stringValue || (stringValue.length() == 4 && stringValue =~ /(?i)none|null(?-i)/) &&
                (ftPair.type == FieldType.INTEGER || ftPair.type == FieldType.LONG || ftPair.type == FieldType.DOUBLE || ftPair.type == FieldType.FLOAT)) {
                continue // Don't add anything if the field is null, an empty string, or a "non-existent" value for numeric types.
            }
            Object objectValue = ImportUtilities.convertValueToType(stringValue, type, dateFormat)
            if(objectValue instanceof ConversionFailureResult) {
                record.put(namesInOrder[field], stringValue)
                failedFields.add([name: namesInOrder[field], type: type] as FieldTypePair)
            }
            else {
                record.put(namesInOrder[field], objectValue)
            }
        }
        collection.insert(record)
        return failedFields
    }

    /**
     * Helper method that adds information about user-given data into a meta-database. This allows the ability to track which databases
     * are user-added, as well as provide some information about them at a later time if needed.
     * @param mongo The mongo instance in which to store the meta-database.
     * @param file The GridFSDBFile whose metadata contains the information that will be more permanently stored in the meta-database.
     */
    private void addMetadata(MongoClient mongo, GridFSDBFile file) {
        DB metaDatabase = mongo.getDB(ImportUtilities.MONGO_META_DB_NAME)
        DBCollection metaCollection = metaDatabase.getCollection(ImportUtilities.MONGO_META_COLL_NAME)
        DBObject metaRecord = new BasicDBObject()
        metaRecord.append("userName", file.get("userName"))
        metaRecord.append("prettyName", file.get("prettyName"))
        metaRecord.append("databaseName", getDatabaseName(file.get("userName"), file.get("prettyName")))
        metaCollection.insert(metaRecord)
        updateFile(file, [complete: true])
    }

    /**
     * Helper method that converts a list of FieldTypePairs to a map from name to type, to more easily access the type property.
     * @param ftPairs A list of FieldTypePairs to convert to a map.
     * @return A map whose keys are the names of the input FieldTypePairs and whose values are their corresponding types.
     */
    private Map fieldTypePairListToMap(List ftPairs) {
        Map fieldTypePairMap = [:]
        ftPairs.each { ftPair ->
            fieldTypePairMap.put(ftPair.name, ftPair.type)
        }
        return fieldTypePairMap
    }

    /**
     * Helper method that converts a list of FieldTypePairs to a map from name to type, and converts type from a FieldType to a string.
     * Used because mongo doesn't know how to deserialize FieldTypePairs for storage but does know how to deserialize maps.
     * @param ftPairs A list of FieldTypePairs to convert to a map.
     * @return A map whose keys are the names of the input FieldTypePairs and whose values are their corresponding types.
     */
    private Map fieldTypePairListToStorageMap(List ftPairs) {
        Map fieldTypePairMap = [:]
        ftPairs.each { ftPair ->
            fieldTypePairMap.put(ftPair.name, ftPair.type as String)
        }
        return fieldTypePairMap
    }

    /**
     * Helper method that determines whether there is a already a database in mongo for the information in the given file. If there is
     * already a database whose name is the same as the name that would be generated by the given file's username and "pretty" name, and if that
     * database has a collection in it with the name given to user-imported collections, and if that collection has at least one record in it,
     * returns true. Else, returns false.
     * @param nomgo The mongo instance in which to search.
     * @param file The GridFSDBFile whose metadata contains the username and "pretty" name associated with the database to look for.
     * @return True if the conditions above are met, or false otherwise.
     */
    private boolean alreadyLoaded(MongoClient mongo, GridFSDBFile file) {
        DBCollection collection = mongo.getDB(getDatabaseName(file.get("userName"), file.get("prettyName"))).getCollection(ImportUtilities.MONGO_USERDATA_COLL_NAME)
        return (collection && collection.count() > 0)
    }

    /**
     * Helper method that adds all of the key/value pairs in the given map to the given GridFSDBFile.
     * Made because mongodb's native putAll method doesn't appear to work.
     * @param file The GridFSDBFile to which to add values.
     * @param fieldsToUpdate A map containing key/value pairs to be added to the file.
     */
    private void updateFile(GridFSDBFile file, Map fieldsToUpdate) {
        fieldsToUpdate.each { key, value ->
            file.put(key as String, value)
        }
        file.save()
    }

    /**
     * Helper method that gets the "ugly", more unique database name created from the given username and "pretty", human-readable
     * database name. As part of this, removes any issue characters that would cause problems as part of a mongo database name.
     * @param userName The username associated with the ugly database name to be created.
     * @param prettyName The "pretty", human-readable database name associated with the ugly database name to be created.
     * @return The "ugly", more unique database name generatd from the given username and "pretty" database name.
     */
    private String getDatabaseName(String userName, String prettyName) {
        String safeUserName = userName.replaceAll(/^\$|[ \t\n\.]/, "_")
        String safePrettyName = prettyName.replaceAll(/[ \t\n\.]/, "_")
        return ImportUtilities.makeUglyName(safeUserName, safePrettyName)
    }
}