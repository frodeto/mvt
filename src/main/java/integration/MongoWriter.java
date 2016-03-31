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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import model.FoodItem;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 *
 */
public class MongoWriter implements DbWriter {

    private static final String TIMESTAMP_KEY = "timestamp";
    private MongoClient mongoClient;
    private String dbName = null;
    private ObjectMapper jsonMapper = new ObjectMapper();

    private final Logger logger = LoggerFactory.getLogger(MongoWriter.class);

    public MongoWriter() {
        mongoClient = new MongoClient();
    }

    @Override
    public MongoWriter initDb() {
        dropOldDb();
        LocalDateTime timestamp = LocalDateTime.now();
        dbName = Db.DB_NAME + timestamp.format(DateTimeFormatter.BASIC_ISO_DATE);
        MongoDatabase db = mongoClient.getDatabase(dbName);
        logger.info("Initialized mongo db '{}'", dbName);
        timeStamp(timestamp, db);
        return this;
    }

    @Override
    public DbWriter writeItems(List<FoodItem> items) {
        List<Document> jsonItems = items.stream().map(toDocument).collect(Collectors.toList());
        MongoDatabase db = mongoClient.getDatabase(dbName);
        MongoCollection<Document> collection = db.getCollection(Db.MVT_MAIN_TABLE_NAME);
        collection.insertMany(jsonItems);
        return this;
    }

    private void dropOldDb() {
        for (String databaseName : mongoClient.listDatabaseNames()) {
            if(databaseName.contains(Db.DB_NAME)) {
                String timestamp = (String) mongoClient.getDatabase(databaseName)
                        .getCollection(Db.IMPORT).find().first().get(TIMESTAMP_KEY);
                mongoClient.dropDatabase(databaseName);
                logger.info("Dropped mongo db '{}' with timestamp '{}'", databaseName, timestamp);
            }
        }
    }

    private void timeStamp(LocalDateTime timestamp, MongoDatabase db) {
        MongoCollection<Document> collection = db.getCollection(Db.IMPORT);
        collection.insertOne(new Document(TIMESTAMP_KEY, timestamp.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)));
    }

    private Function<FoodItem, Document> toDocument = foodItem -> {
        try {
            return Document.parse(jsonMapper.writeValueAsString(foodItem));
        } catch (JsonProcessingException e) {
            logger.error("Error mapping json", e);
            throw new RuntimeException(e);
        }
    };

}
