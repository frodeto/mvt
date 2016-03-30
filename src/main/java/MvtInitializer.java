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

import integration.DbWriter;
import integration.MongoWriter;
import integration.MySqlWriter;
import integration.XlsxReader;
import model.FoodItem;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 *
 */
public class MvtInitializer {
    public static void main(String[] args) {
        Path path = FileSystems.getDefault().getPath("target/classes", "Matvaretabellen+2015-truncated.xlsx");
        InputStream xlsxInputStream = null;
        if (Files.exists(path) && Files.isReadable(path)) {
            try {
                xlsxInputStream = Files.newInputStream(path);
            } catch (IOException e) {
                System.err.println("Could not read " + path.toString());
                e.printStackTrace();
                System.exit(1);
            }
        } else {
            System.err.println("Could not find resource " + path.toString());
            System.exit(1);
        }
        List<FoodItem> foodItems = new XlsxReader(xlsxInputStream).read();
        //new MongoWriter().initDb().writeItems(foodItems).close();
        new MySqlWriter().initDb().writeItems(foodItems).close();
    }
}
