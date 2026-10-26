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


package no.torvundconsulting.mvt.control;

import no.torvundconsulting.mvt.integration.FoodItemRepository;
import no.torvundconsulting.mvt.integration.InMemoryFoodItemRepository;
import no.torvundconsulting.mvt.integration.MongoReader;
import no.torvundconsulting.mvt.web.MvtServer;
import org.apache.catalina.LifecycleException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;

/**
 * Starts the MVT web server. Settings are read from environment variables, see {@link Config}.
 */
public class Mvt {
    private static final Logger logger = LoggerFactory.getLogger(Mvt.class);

    public static void main(String[] args) throws LifecycleException {
        // Tomcat logs through java.util.logging
        SLF4JBridgeHandler.removeHandlersForRootLogger();
        SLF4JBridgeHandler.install();

        // Search and lookups use the same snapshot, so they stay consistent when a new import replaces the database
        FoodItemRepository foodItems;
        try (MongoReader mongoReader = new MongoReader(Config.mongoUri())) {
            foodItems = new InMemoryFoodItemRepository(mongoReader.findAll());
        }
        MvtServer server = new MvtServer(Config.port(), new MvtJAnalyzer(foodItems.findAll()), foodItems);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                server.close();
            } catch (LifecycleException e) {
                logger.warn("Failed to stop server", e);
            }
        }));
        server.start();
        logger.info("MVT server listening on port {}", server.port());
        server.await();
    }
}
