/*
 * Copyright 2026 Frode Torvund
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

/**
 * Settings read from environment variables, with defaults for a local development setup.
 */
final class Config {
    private Config() {
    }

    static int port() {
        String port = env("PORT", "4567");
        try {
            return Integer.parseInt(port);
        } catch (NumberFormatException e) {
            throw new IllegalStateException("PORT must be a number, was '" + port + "'", e);
        }
    }

    static String mongoUri() {
        return env("MONGODB_URI", "mongodb://localhost:27017");
    }

    static String mysqlUrl() {
        return env("MYSQL_URL", "jdbc:mysql://localhost/mvt");
    }

    static String mysqlUser() {
        return env("MYSQL_USER", "root");
    }

    static String mysqlPassword() {
        String password = System.getenv("MYSQL_PASSWORD");
        if (password == null) {
            throw new IllegalStateException("Set MYSQL_PASSWORD to import to MySQL");
        }
        return password;
    }

    private static String env(String name, String defaultValue) {
        String value = System.getenv(name);
        return value == null || value.isBlank() ? defaultValue : value;
    }
}
