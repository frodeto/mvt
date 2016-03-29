/*
 * Copyright 2016 Frode Torvund
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

package integration;

import model.FoodItem;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 *
 */
public class MySqlWriter implements DbWriter {
    private static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
    private Connection connection;

    public MySqlWriter() {
        try {
            Class.forName(JDBC_DRIVER);
            connection = DriverManager.getConnection("jdbc:mysql://localhost/" + Db.DB_NAME + "?user=root&password=admin");
        } catch (ClassNotFoundException | SQLException e) {
            throw new MySqlWriterException("Error connecting to database", e);
        }
    }

    @Override
    public DbWriter initDb() {
        if(!mvtDbExists()) {
            throw new MySqlWriterException("Could not find mvt database");
        }

        // TODO create tables
        return this;
    }

    private boolean mvtDbExists() {
        try {
            ResultSet resultSet = connection.getMetaData().getCatalogs();
            while (resultSet.next()) {
                if(Db.DB_NAME.equals(resultSet.getString(1))) {
                    resultSet.close();
                    return true;
                }
            }
            resultSet.close();
            return true;
        } catch (SQLException e) {
            throw new MySqlWriterException("Error reading metadata from mysql", e);
        }
    }

    @Override
    public void writeItems(List<FoodItem> items) {

    }

    private static class MySqlWriterException extends RuntimeException {
        MySqlWriterException(String message, Throwable cause) {
            super(message, cause);
        }

        MySqlWriterException(String message) {
            super(message);
        }
    }
}
