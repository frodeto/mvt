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

package no.torvundconsulting.mvt.integration;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Indexes;
import no.torvundconsulting.mvt.model.FoodItem;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * Writes each import to a new Mongo database, and drops older imports once the new one is complete.
 */
public class MongoWriter implements DbWriter {
    private static final DateTimeFormatter DB_NAME_SUFFIX = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final MongoClient mongoClient;
    private final Gson gson = new GsonBuilder().create();
    private final Logger logger = LoggerFactory.getLogger(MongoWriter.class);
    private LocalDateTime timestamp;
    private String dbName;

    /**
     * @param connectionString e.g. {@code mongodb://localhost:27017}
     */
    public MongoWriter(String connectionString) {
        mongoClient = MongoClients.create(connectionString);
    }

    @Override
    public MongoWriter initDb() {
        timestamp = LocalDateTime.now();
        dbName = Db.DB_NAME + timestamp.format(DB_NAME_SUFFIX);
        logger.info("Importing to mongo db '{}'", dbName);
        return this;
    }

    @Override
    public MongoWriter writeItems(List<FoodItem> items) {
        if (dbName == null) {
            throw new IllegalStateException("initDb() must be called before writeItems()");
        }
        MongoDatabase db = mongoClient.getDatabase(dbName);
        if (!items.isEmpty()) {
            var collection = db.getCollection(Db.MVT_MAIN_TABLE_NAME);
            collection.insertMany(items.stream().map(toDocument).toList());
            collection.createIndex(Indexes.ascending("id"));
        }
        // Written last: readers only use databases with an import timestamp, so they never see a partial import
        db.getCollection(Db.IMPORT).insertOne(new Document(Db.TIMESTAMP_KEY, timestamp.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)));
        logger.info("Wrote {} food items to mongo db '{}'", items.size(), dbName);
        dropOldDbs();
        return this;
    }

    @Override
    public void close() {
        mongoClient.close();
    }

    private void dropOldDbs() {
        for (String databaseName : mongoClient.listDatabaseNames().into(new ArrayList<>())) {
            if (Db.isMvtDatabase(databaseName) && !databaseName.equals(dbName)) {
                mongoClient.getDatabase(databaseName).drop();
                logger.info("Dropped old mongo db '{}'", databaseName);
            }
        }
    }

    private final Function<FoodItem, Document> toDocument = foodItem -> Document.parse(gson.toJson(foodItem));
}
