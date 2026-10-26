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

import model.Category;
import model.DataSource;
import model.FoodItem;
import model.MvtUnit;
import model.Nutrient;
import model.ProductCategory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Map;

/**
 * Writes food items to a relational schema in the MySQL database {@value Db#DB_NAME}. Existing tables are replaced.
 */
public class MySqlWriter implements DbWriter {
    /**
     * Tables in dependency order: a table only references tables before it.
     */
    private static final List<String> TABLES =
            List.of("Source", "Category", "Unit", "Nutrient", "ProductCategory", "FoodItem", "FoodNutrients");

    private final Connection connection;
    private final Logger logger = LoggerFactory.getLogger(MySqlWriter.class);

    public MySqlWriter() {
        try {
            connection = DriverManager.getConnection(Db.JDBC_CONN_URL + Db.DB_NAME + "?user=root&password=admin");
            logger.info("Connected to {} in {} as root", Db.DB_NAME, Db.JDBC_CONN_URL);
        } catch (SQLException e) {
            throw new MySqlWriterException("Error connecting to database", e);
        }
    }

    @Override
    public void close() {
        try {
            if (!connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            throw new MySqlWriterException("Failed to close connection", e);
        }
    }

    @Override
    public DbWriter initDb() {
        if (!mvtDbExists()) {
            throw new MySqlWriterException("Could not find mvt database");
        }

        try {
            dropTables();
            createSources();
            createCategories();
            createUnits();
            createNutrients();
            createProductCategories();
            createItemTables();
        } catch (SQLException e) {
            throw new MySqlWriterException("Failed to initialize database", e);
        }
        return this;
    }

    @Override
    public DbWriter writeItems(List<FoodItem> items) {
        logger.info("Inserting {} food items", items.size());
        try {
            connection.setAutoCommit(false);
            try (PreparedStatement foodItemStatement = connection.prepareStatement(
                         "INSERT INTO FoodItem (FId, MvtId, Name, PCId, SubGroup, SourceId) VALUES(?,?,?,?,?,?)");
                 PreparedStatement nutrientStatement = connection.prepareStatement(
                         "INSERT INTO FoodNutrients (Amount, FId, NutrientId) VALUES(?,?,?)")) {
                for (FoodItem foodItem : items) {
                    foodItemStatement.setInt(1, foodItem.getId());
                    foodItemStatement.setString(2, foodItem.getMvtId());
                    foodItemStatement.setString(3, foodItem.getName());
                    foodItemStatement.setInt(4, foodItem.getProductSubGroup().getProductGroup().getProductCategory().ordinal());
                    foodItemStatement.setString(5, foodItem.getProductSubGroup().getName());
                    foodItemStatement.setInt(6, foodItem.getDataSource().ordinal());
                    foodItemStatement.addBatch();
                    addNutrients(nutrientStatement, foodItem.getId(), foodItem.getNutrientMap());
                }
                logger.info("Executing food items batch");
                foodItemStatement.executeBatch();
                logger.info("Executing nutrient batch");
                nutrientStatement.executeBatch();
                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new MySqlWriterException("Failed to insert items", e);
        }
        return this;
    }

    private static void addNutrients(PreparedStatement nutrientStatement, int foodItemId, Map<Nutrient, Double> nutrientMap)
            throws SQLException {
        for (Map.Entry<Nutrient, Double> entry : nutrientMap.entrySet()) {
            nutrientStatement.setDouble(1, entry.getValue());
            nutrientStatement.setInt(2, foodItemId);
            nutrientStatement.setInt(3, entry.getKey().ordinal());
            nutrientStatement.addBatch();
        }
    }

    private void dropTables() throws SQLException {
        try (Statement statement = connection.createStatement()) {
            for (String table : TABLES.reversed()) {
                statement.executeUpdate("DROP TABLE IF EXISTS " + table);
            }
        }
    }

    private void createSources() throws SQLException {
        createTable("CREATE TABLE Source(" +
                "SourceId INT NOT NULL," +
                "Name VARCHAR(80) NOT NULL," +
                "PRIMARY KEY (SourceId))");
        try (PreparedStatement statement = connection.prepareStatement("INSERT INTO Source VALUES (?,?)")) {
            for (DataSource dataSource : DataSource.values()) {
                statement.setInt(1, dataSource.ordinal());
                statement.setString(2, dataSource.name());
                statement.executeUpdate();
            }
        }
        logger.info("Created DataSources.");
    }

    private void createCategories() throws SQLException {
        createTable("CREATE TABLE Category(" +
                "CategoryId INT NOT NULL," +
                "Name VARCHAR(80) NOT NULL," +
                "PRIMARY KEY (CategoryId))");
        try (PreparedStatement statement = connection.prepareStatement("INSERT INTO Category VALUES (?,?)")) {
            for (Category category : Category.values()) {
                statement.setInt(1, category.ordinal());
                statement.setString(2, category.name());
                statement.executeUpdate();
            }
        }
        logger.info("Created Categories.");
    }

    private void createUnits() throws SQLException {
        createTable("CREATE TABLE Unit(" +
                "UnitId INT NOT NULL," +
                "Name VARCHAR(80) NOT NULL," +
                "Symbol VARCHAR(20) NOT NULL," +
                "PRIMARY KEY (UnitId))");
        try (PreparedStatement statement = connection.prepareStatement("INSERT INTO Unit VALUES (?,?,?)")) {
            for (MvtUnit mvtUnit : MvtUnit.values()) {
                statement.setInt(1, mvtUnit.ordinal());
                statement.setString(2, mvtUnit.name());
                statement.setString(3, mvtUnit.getSymbol());
                statement.executeUpdate();
            }
        }
        logger.info("Created Units.");
    }

    private void createNutrients() throws SQLException {
        createTable("CREATE TABLE Nutrient(" +
                "NutrientId INT NOT NULL," +
                "Name VARCHAR(80) NOT NULL," +
                "MvtName VARCHAR(80) NOT NULL," +
                "UnitId INT NOT NULL," +
                "PRIMARY KEY (NutrientId)," +
                "FOREIGN KEY (UnitId) REFERENCES Unit (UnitId))");
        try (PreparedStatement statement = connection.prepareStatement("INSERT INTO Nutrient VALUES (?,?,?,?)")) {
            for (Nutrient nutrient : Nutrient.values()) {
                statement.setInt(1, nutrient.ordinal());
                statement.setString(2, nutrient.getName());
                statement.setString(3, nutrient.getMvtName());
                statement.setInt(4, nutrient.getUnit().ordinal());
                statement.executeUpdate();
            }
        }
        logger.info("Created Nutrients.");
    }

    private void createProductCategories() throws SQLException {
        createTable("CREATE TABLE ProductCategory(" +
                "PCId INT NOT NULL," +
                "Name VARCHAR(80) NOT NULL," +
                "MvtName VARCHAR(80) NOT NULL," +
                "CategoryId INT NOT NULL," +
                "PRIMARY KEY (PCId)," +
                "FOREIGN KEY (CategoryId) REFERENCES Category (CategoryId))");
        try (PreparedStatement statement = connection.prepareStatement("INSERT INTO ProductCategory VALUES (?,?,?,?)")) {
            for (ProductCategory productCategory : ProductCategory.values()) {
                statement.setInt(1, productCategory.ordinal());
                statement.setString(2, productCategory.name());
                statement.setString(3, productCategory.getMvtName());
                statement.setInt(4, productCategory.getCategory().ordinal());
                statement.executeUpdate();
            }
        }
        logger.info("Created ProductCategories.");
    }

    private void createItemTables() throws SQLException {
        createTable("CREATE TABLE FoodItem(" +
                "FId INT NOT NULL," +
                "MvtId VARCHAR(20) NOT NULL," +
                "Name VARCHAR(255) NOT NULL," +
                "SubGroup VARCHAR(255) NOT NULL," +
                "PCId INT NOT NULL," +
                "SourceId INT NOT NULL," +
                "PRIMARY KEY (FId)," +
                "FOREIGN KEY (SourceId) REFERENCES Source (SourceId)," +
                "FOREIGN KEY (PCId) REFERENCES ProductCategory (PCId))");

        createTable("CREATE TABLE FoodNutrients(" +
                "FNId INT NOT NULL AUTO_INCREMENT," +
                "FId INT NOT NULL," +
                "NutrientId INT NOT NULL," +
                "Amount DOUBLE NOT NULL," +
                "PRIMARY KEY (FNId)," +
                "FOREIGN KEY (FId) REFERENCES FoodItem (FId)," +
                "FOREIGN KEY (NutrientId) REFERENCES Nutrient (NutrientId))");
    }

    private void createTable(String createTableStatement) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate(createTableStatement);
        }
        logger.info(createTableStatement.split("\\(")[0]);
    }

    private boolean mvtDbExists() {
        try (ResultSet resultSet = connection.getMetaData().getCatalogs()) {
            while (resultSet.next()) {
                if (Db.DB_NAME.equals(resultSet.getString(1))) {
                    return true;
                }
            }
            return false;
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
