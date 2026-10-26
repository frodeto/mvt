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

package no.torvundconsulting.mvt.integration;

import no.torvundconsulting.mvt.model.FoodItem;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Food items held in memory, e.g. a snapshot loaded from the database at startup.
 */
public class InMemoryFoodItemRepository implements FoodItemRepository {
    private final List<FoodItem> foodItems;
    private final Map<Integer, FoodItem> foodItemsById;

    public InMemoryFoodItemRepository(List<FoodItem> foodItems) {
        this.foodItems = List.copyOf(foodItems);
        this.foodItemsById = this.foodItems.stream().collect(Collectors.toUnmodifiableMap(FoodItem::id, Function.identity()));
    }

    @Override
    public List<FoodItem> findAll() {
        return foodItems;
    }

    @Override
    public Optional<FoodItem> findById(int id) {
        return Optional.ofNullable(foodItemsById.get(id));
    }
}
