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

import model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.List;
import java.util.Map;

/**
 *
 */
public class MySqlWriter implements DbWriter {
    private static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
    private Connection connection;

    private final Logger logger = LoggerFactory.getLogger(MySqlWriter.class);

    public MySqlWriter() {
        try {
            Class.forName(JDBC_DRIVER);
            connection = DriverManager.getConnection(Db.JDBC_CONN_URL + Db.DB_NAME + "?user=root&password=admin");
            logger.info("Connected to {} in {} as root", Db.DB_NAME, Db.JDBC_CONN_URL);
        } catch (ClassNotFoundException | SQLException e) {
            throw new MySqlWriterException("Error connecting to database", e);
        }
    }

    @Override
    public void close() {
        try {
            if(connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            throw new MySqlWriterException("Failed to close connection", e);
        }
    }

    @Override
    public DbWriter initDb() {
        if(!mvtDbExists()) {
            throw new MySqlWriterException("Could not find mvt database");
        }

        try {
            createCategories();
            createUnits();
            createNutrients();
            createProductCategories();
            createItemTables();
        } catch (SQLException e) {
            throw new MySqlWriterException("Failed to initialize database",e);
        }
        return this;
    }

    @Override
    public DbWriter writeItems(List<FoodItem> items) {
        logger.info("Inserting {} food items", items.size());
        try {
            connection.setAutoCommit(false);
        } catch (SQLException e) {
            throw new MySqlWriterException("Failed to disable autocommit");
        }

        try {
            PreparedStatement foodItemStatement = connection.prepareStatement("INSERT INTO FoodItem (FId, Name, PCId, SubGroup) VALUES(?,?,?,?)");
            PreparedStatement nutrientStatement = connection.prepareStatement("INSERT INTO FoodNutrients (Amount, FId, NutrientId) VALUES(?,?,?)");
            items.forEach(foodItem -> {
                try {
                    foodItemStatement.setInt(1, foodItem.getId());
                    foodItemStatement.setString(2, foodItem.getName());
                    foodItemStatement.setInt(3, foodItem.getProductSubGroup().getProductGroup().getProductCategory().ordinal());
                    foodItemStatement.setString(4, foodItem.getProductSubGroup().getName());
                    foodItemStatement.addBatch();
                    addNutrients(nutrientStatement, foodItem.getId(), foodItem.getNutrientMap());
                } catch (SQLException e) {
                    throw new MySqlWriterException("Failed to prepare sql statement", e);
                }
            });
            logger.info("Executing food items batch");
            foodItemStatement.executeBatch();
            connection.commit();
            logger.info("Executing nutrient batch");
            nutrientStatement.executeBatch();
            connection.commit();
            connection.setAutoCommit(true);
        } catch (SQLException e) {
            throw new MySqlWriterException("Failed to insert items ", e);
        }
        return this;
    }

    private void addNutrients(PreparedStatement nutrientStatement, final int foodItemId, Map<Nutrient, Double> nutrientMap) {
        nutrientMap.forEach((nutrient, amount) -> {
            try {
                nutrientStatement.setDouble(1, amount);
                nutrientStatement.setInt(2, foodItemId);
                nutrientStatement.setInt(3, nutrient.ordinal());
                nutrientStatement.addBatch();
            } catch (SQLException e) {
                throw new MySqlWriterException("Failed to prepare sql statement", e);
            }
        });
    }

    private void createCategories() throws SQLException {
        String createTableStatement = "CREATE TABLE Category(" +
                "CategoryId INT NOT NULL" + ',' +
                "Name VARCHAR(80) NOT NULL" + ',' +
                "PRIMARY KEY ( CategoryId ));";
        createTable(createTableStatement);
        PreparedStatement statement = connection.prepareStatement("INSERT INTO Category VALUES (?,?)");
        for (Category category : Category.values()) {
            statement.setInt(1, category.ordinal());
            statement.setString(2, category.name());
            statement.executeUpdate();
        }
        statement.close();
        logger.info("Created Categories.");
    }

    private void createUnits() throws SQLException {
        String createTableStatement = "CREATE TABLE Unit(" +
                "UnitId INT NOT NULL" + ',' +
                "Name VARCHAR(80) NOT NULL" + ',' +
                "Symbol VARCHAR(20) NOT NULL" + ',' +
                "PRIMARY KEY ( UnitId ));";
        createTable(createTableStatement);
        PreparedStatement statement = connection.prepareStatement("INSERT INTO Unit VALUES (?,?,?)");
        for (MvtUnit mvtUnit : MvtUnit.values()) {
            statement.setInt(1, mvtUnit.ordinal());
            statement.setString(2, mvtUnit.name());
            statement.setString(3, mvtUnit.getSymbol());
            statement.executeUpdate();
        }
        statement.close();
        logger.info("Created Units.");
    }

    private void createNutrients() throws SQLException {
        String createTableStatement = "CREATE TABLE Nutrient(" +
                "NutrientId INT NOT NULL" + ',' +
                "Name VARCHAR(80) NOT NULL" + ',' +
                "MvtName VARCHAR(80) NOT NULL" + ',' +
                "UnitId INT NOT NULL" + ',' +
                "PRIMARY KEY ( NutrientId )" + ',' +
                "FOREIGN KEY (UnitId) REFERENCES Unit (UnitId));";
        createTable(createTableStatement);
        PreparedStatement statement = connection.prepareStatement("INSERT INTO Nutrient VALUES (?,?,?,?)");
        for (Nutrient nutrient : Nutrient.values()) {
            statement.setInt(1, nutrient.ordinal());
            statement.setString(2, nutrient.getName());
            statement.setString(3, nutrient.getMvtName());
            statement.setInt(4, nutrient.getUnit().ordinal());
            statement.executeUpdate();
        }
        statement.close();
        logger.info("Created Nutrients.");
    }

    private void createProductCategories() throws SQLException {
        String createTableStatement = "CREATE TABLE ProductCategory(" +
                "PCId INT NOT NULL" + ',' +
                "Name VARCHAR(80) NOT NULL" + ',' +
                "MvtName VARCHAR(80) NOT NULL" + ',' +
                "CategoryId INT NOT NULL" + ',' +
                "PRIMARY KEY ( PCId )" + ',' +
                "FOREIGN KEY (CategoryId) REFERENCES Category (CategoryId));";
        createTable(createTableStatement);
        PreparedStatement statement = connection.prepareStatement("INSERT INTO ProductCategory VALUES (?,?,?,?)");
        for (ProductCategory productCategory : ProductCategory.values()) {
            statement.setInt(1, productCategory.ordinal());
            statement.setString(2, productCategory.name());
            statement.setString(3, productCategory.getMvtName());
            statement.setInt(4, productCategory.getCategory().ordinal());
            statement.executeUpdate();
        }
        statement.close();
        logger.info("Created ProductCategories.");
    }

    private void createItemTables() {
        String foodItem = "CREATE TABLE FoodItem(" +
                "FId INT NOT NULL" + ',' +
                "Name VARCHAR(80) NOT NULL" + ',' +
                "SubGroup VARCHAR(80) NOT NULL" + ',' +
                "PCId INT NOT NULL" + ',' +
                "PRIMARY KEY ( FId )" + ',' +
                "FOREIGN KEY (PCId) REFERENCES ProductCategory (PCId));";
        createTable(foodItem);

        String foodNutrients = "CREATE TABLE FoodNutrients(" +
                "FNId INT NOT NULL AUTO_INCREMENT" + ',' +
                "FId INT NOT NULL" + ',' +
                "NutrientId INT NOT NULL" + ',' +
                "Amount DOUBLE NOT NULL" + ',' +
                "PRIMARY KEY ( FNId )" + ',' +
                "FOREIGN KEY (FId) REFERENCES FoodItem (FId)" + ',' +
                "FOREIGN KEY (NutrientId) REFERENCES Nutrient (NutrientId));";
        createTable(foodNutrients);
    }

    private void createTable(String createTableStatement) {
        PreparedStatement statement;
        try {
            statement = connection.prepareStatement(createTableStatement);
            statement.executeUpdate();
            statement.close();
        } catch (SQLException e) {
            throw new MySqlWriterException("Failed to create table using statement " + createTableStatement, e);
        }
        logger.info(createTableStatement.split("\\(")[0]);
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

    private static class MySqlWriterException extends RuntimeException {
        MySqlWriterException(String message, Throwable cause) {
            super(message, cause);
        }
        MySqlWriterException(String message) {
            super(message);
        }
    }
}
