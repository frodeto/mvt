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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import model.FoodItem;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import static com.mongodb.client.model.Filters.eq;


/**
 * Reads food items from the most recent complete Mongo import.
 */
public class MongoReader implements FoodItemRepository, AutoCloseable {
    private final MongoClient mongoClient;
    private final MongoCollection<Document> mvtCollection;
    private final Gson gson = new GsonBuilder().create();
    private final Logger logger = LoggerFactory.getLogger(MongoReader.class);

    public MongoReader() {
        mongoClient = MongoClients.create();
        MongoDatabase mvtDb = findCurrentDb();
        logger.info("Reading from database {}", mvtDb.getName());
        mvtCollection = mvtDb.getCollection(Db.MVT_MAIN_TABLE_NAME);
    }

    @Override
    public List<FoodItem> findAll() {
        logger.info("Reading {} objects from database", mvtCollection.countDocuments());
        return mvtCollection.find().map(toFoodItem::apply).into(new ArrayList<>());
    }

    @Override
    public Optional<FoodItem> findById(int id) {
        return Optional.ofNullable(mvtCollection.find(eq("id", id)).first()).map(toFoodItem);
    }

    @Override
    public void close() {
        mongoClient.close();
    }

    private final Function<Document, FoodItem> toFoodItem = document -> gson.fromJson(document.toJson(), FoodItem.class);

    private MongoDatabase findCurrentDb() {
        Map<String, LocalDateTime> importTimestamps = new HashMap<>();
        for (String databaseName : mongoClient.listDatabaseNames()) {
            if (Db.isMvtDatabase(databaseName)) {
                Db.importTimestamp(mongoClient, databaseName)
                        .ifPresent(timestamp -> importTimestamps.put(databaseName, timestamp));
            }
        }
        return newestImport(importTimestamps)
                .map(mongoClient::getDatabase)
                .orElseThrow(() -> new IllegalStateException("No mvt database found. Run MvtInitializer to import data."));
    }

    static Optional<String> newestImport(Map<String, LocalDateTime> importTimestamps) {
        return importTimestamps.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey);
    }
}
