package com.ncc.neon.config.field
import com.ncc.neon.query.filter.DataSet
import org.codehaus.jackson.map.ObjectMapper
import org.slf4j.Logger
import org.slf4j.LoggerFactory
/*
 * ************************************************************************
 * Copyright (c), 2013 Next Century Corporation. All Rights Reserved.
 *
 * This software code is the exclusive property of Next Century Corporation and is
 * protected by United States and International laws relating to the protection
 * of intellectual property.  Distribution of this software code by or to an
 * unauthorized party, or removal of any of these notices, is strictly
 * prohibited and punishable by law.
 *
 * UNLESS PROVIDED OTHERWISE IN A LICENSE AGREEMENT GOVERNING THE USE OF THIS
 * SOFTWARE, TO WHICH YOU ARE AN AUTHORIZED PARTY, THIS SOFTWARE CODE HAS BEEN
 * ACQUIRED BY YOU "AS IS" AND WITHOUT WARRANTY OF ANY KIND.  ANY USE BY YOU OF
 * THIS SOFTWARE CODE IS AT YOUR OWN RISK.  ALL WARRANTIES OF ANY KIND, EITHER
 * EXPRESSED OR IMPLIED, INCLUDING, WITHOUT LIMITATION, IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE, ARE HEREBY EXPRESSLY
 * DISCLAIMED.
 *
 * PROPRIETARY AND CONFIDENTIAL TRADE SECRET MATERIAL NOT FOR DISCLOSURE OUTSIDE
 * OF NEXT CENTURY CORPORATION EXCEPT BY PRIOR WRITTEN PERMISSION AND WHEN
 * RECIPIENT IS UNDER OBLIGATION TO MAINTAIN SECRECY.
 *
 * 
 * @author tbrooks
 */

final class GenerateConfigBundleScript {

    private static final Logger LOGGER = LoggerFactory.getLogger(GenerateConfigBundleScript)
    private static final String BUNDLE_CSV_LOCATION = "config/bundle.csv"
    private static final String OUTPUT_LOCATION = "build/config"
    private static final String OUTPUT_FILE_NAME = "config-bundle.json"

    private final FieldConfigurationMapping configurationMapping = new FieldConfigurationMapping()

    @SuppressWarnings("JavaIoPackageAccess")
    void parseBundleSpec(){
        File bundleSpec = new File(BUNDLE_CSV_LOCATION)
        if(!bundleSpec.exists()){
            LOGGER.info("${bundleSpec.absolutePath} does not exist; not generating any config bundle.")
            return
        }
        readEachLine(bundleSpec)
        outputFile()
    }

    private void readEachLine(File bundleSpec) {
        bundleSpec.eachLine { String line ->
            if (shouldIgnoreLine(line)) {
                return
            }
            def dataArray = line.split(",")
            populateConfigurationMapping(dataArray)
        }
    }

    @SuppressWarnings("JavaIoPackageAccess")
    private void outputFile() {
        ObjectMapper mapper = new ObjectMapper()
        String json = mapper.writeValueAsString(configurationMapping)

        new File(OUTPUT_LOCATION).mkdirs()
        File file = new File("$OUTPUT_LOCATION/$OUTPUT_FILE_NAME")
        file.text = json
        LOGGER.info("Outputting config bundle to ${OUTPUT_LOCATION}")
    }


    private void populateConfigurationMapping(String[] dataArray) {
        validateLine(dataArray)

        DataSet dataSet = new DataSet(databaseName: dataArray[0], tableName: dataArray[1])
        WidgetDataSet widgetDataSet = new WidgetDataSet(widgetName: dataArray[2], dataSet: dataSet)

        def columnMap = parseColumnMap(dataArray[3])

        configurationMapping.put(widgetDataSet, new ColumnMapping(mapping: columnMap))
    }

    private def parseColumnMap(String mapString) {
        String mapData = mapString.trim()[1..-2]
        String [] keyValues = mapData.split(";")
        def columnMap = [:]
        keyValues.each { String keyValue ->
            String [] split = keyValue.split(":")
            columnMap.put(split[0],split[1])
        }
        return columnMap
    }

    private void validateLine(String[] dataArray) {
        if (dataArray.length != 4) {
            throw new IllegalArgumentException("The spec file is malformed: ${dataArray}")
        }
        if (!WidgetNames.contains(dataArray[2])) {
            throw new IllegalArgumentException("There is an invalid widget name on this line: ${dataArray}")
        }
    }

    private boolean shouldIgnoreLine(String line) {
        line.trim().length() == 0 || line.startsWith("#")
    }

    public static void main(String [] args){
        GenerateConfigBundleScript generator = new GenerateConfigBundleScript()
        generator.parseBundleSpec()
    }

}
