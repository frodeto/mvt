
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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import model.FoodItem;
import model.Nutrient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Request;

import java.util.List;

import static spark.Spark.get;

/**
 *
 */
public class Mvt {
    private final Logger logger = LoggerFactory.getLogger(Mvt.class);
    private final MvtAnalyzer analyzer;
    private Gson gson = new GsonBuilder().create();

    public static void main(String[] args) {
        Mvt mvt = new Mvt();
        // SPARK:
        get("/search", (req, res) -> mvt.parseRequest(req));
    }

    private Mvt() {
        analyzer = new MvtJAnalyzer();
    }

    private String parseRequest(Request req) {
        logger.info("Received {}", req.url());
        Nutrient nutrient = Nutrient.fromMvtName(req.queryParams("Nutrient"));
        double amount = Double.parseDouble(req.queryParams("Amount"));
        List<FoodItem> aboveLevel = analyzer.getAboveLevel(nutrient, amount);
        String response = gson.toJson(aboveLevel);
        logger.info("Respnse: {}", response);
        return response;
    }

}
