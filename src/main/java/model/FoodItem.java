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

package model;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

/**
 * Structure:
 * {@link Category}
 *  |-  {@link ProductCategory}
 *      |-  {@link FoodItem}
 *
 *  E.g.: [DAIRY] - [Meieriprodukter] - [Appenzeller, ost]
 *
 * @param id          sequential id assigned on import
 * @param mvtId       the id used in the source data, e.g. "01.344" in MVT
 * @param nutrientMap amount of each nutrient per 100 g; nutrients without a known value are absent
 */
public record FoodItem(int id, String mvtId, String name, ProductCategory productCategory,
                       Map<Nutrient, Double> nutrientMap, DataSource dataSource) {

    public FoodItem {
        nutrientMap = nutrientMap == null || nutrientMap.isEmpty()
                ? Map.of()
                : Collections.unmodifiableMap(new EnumMap<>(nutrientMap));
    }
}
