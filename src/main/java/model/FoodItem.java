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

import java.util.Map;
import java.util.Objects;

/**
 * Structure:
 * {@link Category}
 *  |-  {@link ProductCategory}
 *      |-  {@link ProductGroup}
 *          |-  {@link ProductSubGroup}
 *              |-  {@link FoodItem}
 *
 *  E.g.: [DAIRY] - [Melk og melkeprodukter] - [Ost] - [Ost, ekstra fet] - [Ridderost]
 */
public class FoodItem {
    private int id;
    private String name;
    private ProductSubGroup productSubGroup;
    private Map<Nutrient, Double> nutrientMap;
    private DataSource dataSource;

    public FoodItem() {
    }

    public FoodItem(int id, String name, ProductSubGroup productSubGroup, Map<Nutrient, Double> nutrientMap, DataSource dataSource) {
        this.id = id;
        this.name = name;
        this.productSubGroup = productSubGroup;
        this.nutrientMap = nutrientMap;
        this.dataSource = dataSource;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public ProductSubGroup getProductSubGroup() {
        return productSubGroup;
    }

    public Map<Nutrient, Double> getNutrientMap() {
        return nutrientMap;
    }

    public DataSource getDataSource() {
        return dataSource;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FoodItem foodItem = (FoodItem) o;
        return id == foodItem.id &&
                Objects.equals(name, foodItem.name) &&
                dataSource == foodItem.dataSource;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, dataSource);
    }
}
