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
import com.mongodb.DBCollection;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import model.FoodItem;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 *
 */
public class MongoWriter implements DbWriter {

    private MongoClient mongoClient;
    private String dbName = null;
    private ObjectMapper jsonMapper = new ObjectMapper();

    private final Logger logger = LoggerFactory.getLogger(MongoWriter.class);

    public MongoWriter() {
        mongoClient = new MongoClient();
        mongoClient.getUsedDatabases().stream().filter(db -> db.collectionExists(Db.IMPORT)).forEach(db -> {
            DBCollection collection = db.getCollection(Db.IMPORT);
            if (collection.count() > 0) {
                dbName = db.getName();
            }
        });
    }

    @Override
    public MongoWriter createDb() {
        LocalDate localDate = LocalDate.now();
        dbName= "mvt" + localDate.format(DateTimeFormatter.BASIC_ISO_DATE);
        MongoDatabase db = mongoClient.getDatabase(dbName);
        long count = db.getCollection(Db.MVT_MAIN_TABLE_NAME).count();
        logger.info("count: " + count);
        if(count > 0) {
            db.getCollection(Db.MVT_MAIN_TABLE_NAME).drop();
        }
        return this;
    }

    @Override
    public void writeItems(List<FoodItem> items) {
        List<Document> jsonItems = items.stream().map(toDocument).collect(Collectors.<Document>toList());
        MongoDatabase db = mongoClient.getDatabase(dbName);
        MongoCollection<Document> collection = db.getCollection(Db.MVT_MAIN_TABLE_NAME);
        collection.insertMany(jsonItems);
    }

    Function<FoodItem, Document> toDocument = foodItem -> {
        try {
            return Document.parse(jsonMapper.writeValueAsString(foodItem));
        } catch (JsonProcessingException e) {
            logger.error("Error mapping json", e);
            throw new RuntimeException(e);
        }
    };

}
