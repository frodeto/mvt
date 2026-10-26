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

import com.mongodb.client.MongoClient;
import org.bson.Document;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Names and helpers shared by the database readers and writers.
 */
class Db {
    static final String DB_NAME = "mvt";
    static final String MVT_MAIN_TABLE_NAME = "FOODITEM";
    static final String IMPORT = "IMPORT";
    static final String TIMESTAMP_KEY = "timestamp";

    /**
     * Mongo databases are named mvt + import date (yyyyMMdd), or date and time (yyyyMMddHHmmss).
     */
    private static final Pattern MONGO_DB_NAME_PATTERN = Pattern.compile(DB_NAME + "\\d{8}(\\d{6})?");

    private Db() {
    }

    static boolean isMvtDatabase(String databaseName) {
        return MONGO_DB_NAME_PATTERN.matcher(databaseName).matches();
    }

    /**
     * The time a Mongo import completed, or empty if the database has no (complete) import.
     */
    static Optional<LocalDateTime> importTimestamp(MongoClient mongoClient, String databaseName) {
        Document importDocument = mongoClient.getDatabase(databaseName).getCollection(IMPORT).find().first();
        return Optional.ofNullable(importDocument)
                .map(document -> document.getString(TIMESTAMP_KEY))
                .map(timestamp -> LocalDateTime.parse(timestamp, DateTimeFormatter.ISO_LOCAL_DATE_TIME));
    }
}
