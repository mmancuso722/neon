/*
 * Copyright 2016 Next Century Corporation
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

package com.ncc.neon.connect

import java.sql.*

/**
 * Holds a connection to derby
 */

@SuppressWarnings("ClassForName")
class DerbyConnectionClient implements ConnectionClient{

    private Connection connection
    private final String embeddedDriver = "org.apache.derby.jdbc.EmbeddedDriver"
    private final String clientDriver = "org.apache.derby.jdbc.ClientDriver"

    public DerbyConnectionClient(ConnectionInfo info){
        if(info.host) {
            String[] connectionUrl = info.host.split(':', 2)
            String host = connectionUrl[0]
            int port = connectionUrl.length == 2 ? Integer.parseInt(connectionUrl[1]) : 1527

            Class.forName(clientDriver).newInstance()
            connection = DriverManager.getConnection("jdbc:derby://" + host + ":" + port + "/" + info.databaseName + ";create=true")
        } else {
            Class.forName(embeddedDriver).newInstance()
            connection = DriverManager.getConnection("jdbc:derby:" + info.databaseName + ";create=true")
        }
    }

    Connection getDerby(){
        return connection
    }

    /**
     * Close the connection to derby.
     */
    @Override
    void close(){
        connection?.close()
        connection = null
    }

}