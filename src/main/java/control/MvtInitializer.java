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

import integration.DbWriter;
import integration.MongoWriter;
import integration.MvtXlsxReader;
import model.FoodItem;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * Reading from sources, populating data storages
 */
public class MvtInitializer {
    private static final String MVT_RESOURCE = "/Matvaretabellen+2015.xlsx";

    public static void main(String[] args) throws IOException {
        List<FoodItem> foodItems;
        try (InputStream xlsxInputStream = MvtInitializer.class.getResourceAsStream(MVT_RESOURCE)) {
            if (xlsxInputStream == null) {
                throw new IllegalStateException("Could not find resource " + MVT_RESOURCE + " on the classpath");
            }
            foodItems = new MvtXlsxReader(xlsxInputStream).read();
        }
        try (DbWriter writer = new MongoWriter()) {
            writer.initDb().writeItems(foodItems);
        }
    }
}
