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
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import model.FoodItem;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.mongodb.client.model.Filters.eq;


/**
 *
 */
public class MongoReader {
    private static final String TIMESTAMP_KEY = "timestamp";
    private MongoClient mongoClient;
    private MongoDatabase mvtDb;
    private MongoCollection<Document> mvtCollection;
    private Gson gson = new GsonBuilder().create();
    private final Logger logger = LoggerFactory.getLogger(MongoReader.class);

    public MongoReader() {
        mongoClient = new MongoClient();
        findCurrentDb();
    }

    public List<FoodItem> retrieveAll() {
        logger.info("Reading {} objects from database", mvtCollection.count());
        return mvtCollection.find().into(new ArrayList<>()).stream().map(toFoodItem).collect(Collectors.toList());
    }

    public FoodItem get(Integer id) {
        return mvtCollection.find(eq("id", id)).into(new ArrayList<>()).stream().map(toFoodItem).findAny().orElse(null);
    }

    private Function<Document, FoodItem> toFoodItem = document -> gson.fromJson(document.toJson(), FoodItem.class);

    private void findCurrentDb() {
        LocalDateTime lastTimestamp = LocalDateTime.MIN;
        for (String databaseName : mongoClient.listDatabaseNames()) {
            if (databaseName.contains(Db.DB_NAME)) {
                LocalDateTime fromTimestamp = getTimeStamp(databaseName);
                if(fromTimestamp.isAfter(lastTimestamp)) {
                    mvtDb = mongoClient.getDatabase(databaseName);
                }
                lastTimestamp = fromTimestamp;
            }
        }
        logger.info("Reading from database {}...", mvtDb.getName());
        mvtCollection = mvtDb.getCollection(Db.MVT_MAIN_TABLE_NAME);
        logger.info("collection loaded.");
    }

    private LocalDateTime getTimeStamp(String databaseName) {
        String timestamp = (String) mongoClient.getDatabase(databaseName).getCollection(Db.IMPORT).find().first().get(TIMESTAMP_KEY);
        return LocalDateTime.from(DateTimeFormatter.ISO_LOCAL_DATE_TIME.parse(timestamp));
    }
}
