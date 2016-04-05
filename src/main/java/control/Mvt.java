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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import integration.FoodItemReference;
import integration.MongoReader;
import integration.NutrientReference;
import model.FoodItem;
import model.Nutrient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Request;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static spark.Spark.*;

/**
 *
 */
public class Mvt {
    private static final Logger logger = LoggerFactory.getLogger(Mvt.class);
    private static Gson gson = new GsonBuilder().create();
    private final MongoReader mongoReader;
    private final MvtAnalyzer analyzer;

    public static void main(String[] args) {
        Mvt mvt = new Mvt();

        // Define SPARK interface:
        get("/search", (req, res) -> mvt.executeSearch(req), gson::toJson);
        get("/nutrients", (req, res) -> mvt.retrieveNutrients(), gson::toJson);
        get("/:name/:id", (req, res) -> mvt.retrieve(req), gson::toJson);

        // Define SPARK filters:
        before((req, res) -> logger.info("Received {}", req.url()));
        after((req, res) -> logger.info("Response: {}", res.raw()));

    }

    private List<NutrientReference> retrieveNutrients() {
        return Stream.of(Nutrient.values()).map(nutrient -> new NutrientReference(nutrient.ordinal(), nutrient.getMvtName()))
                .collect(Collectors.toList());
    }

    private Mvt() {
        mongoReader = new MongoReader();
        analyzer = new MvtJAnalyzer(mongoReader.retrieveAll());
    }

    private List<FoodItemReference> executeSearch(Request req) {
        Nutrient nutrient = Nutrient.fromMvtName(req.queryParams("Nutrient"));
        double amount = Double.parseDouble(req.queryParams("Amount"));
        return  analyzer.getAboveLevel(nutrient, amount);
    }

    private Object retrieve(Request req) {
        switch (req.params(":name").toLowerCase()) {
            case "fooditem":
                return retrieveFoodItem(req.params(":id"));
            case "nutrient":
                return retrieveNutrient(req.params(":id"));
            default:
                return null;
        }
    }

    private Nutrient retrieveNutrient(String params) {
        logger.info(params);
        return null;
    }

    private FoodItem retrieveFoodItem(String id) {
        return mongoReader.get(Integer.decode(id));
    }

}
