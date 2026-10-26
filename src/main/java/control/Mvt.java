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


package control;

import integration.MongoReader;
import org.apache.catalina.LifecycleException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;
import web.MvtServer;

/**
 * Starts the MVT web server. The port is read from the PORT environment variable (default 4567).
 */
public class Mvt {
    private static final Logger logger = LoggerFactory.getLogger(Mvt.class);
    private static final int DEFAULT_PORT = 4567;

    public static void main(String[] args) throws LifecycleException {
        // Tomcat logs through java.util.logging
        SLF4JBridgeHandler.removeHandlersForRootLogger();
        SLF4JBridgeHandler.install();

        int port = Integer.parseInt(System.getenv().getOrDefault("PORT", String.valueOf(DEFAULT_PORT)));
        MongoReader mongoReader = new MongoReader();
        MvtServer server = new MvtServer(port, new MvtJAnalyzer(mongoReader.findAll()), mongoReader);
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
