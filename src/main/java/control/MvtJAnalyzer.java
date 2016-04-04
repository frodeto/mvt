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

import integration.FoodItemReference;
import model.FoodItem;
import model.Nutrient;

import java.util.List;
import java.util.stream.Collectors;

class MvtJAnalyzer implements MvtAnalyzer {

    private final List<FoodItem> foodItems;

    MvtJAnalyzer(List<FoodItem> foodItems) {
        this.foodItems = foodItems;

    }

    @Override
    public List<FoodItemReference> getAboveLevel(Nutrient nutrient, Double level) {
        return foodItems.stream().filter(foodItem -> {
            Double thisLevel = foodItem.getNutrientMap().get(nutrient);
            return thisLevel >= level;
        }).map(foodItem -> new FoodItemReference(foodItem.getId(), foodItem.getName(), foodItem.getDataSource()))
                .collect(Collectors.toList());
    }
}
