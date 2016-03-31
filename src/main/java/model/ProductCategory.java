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

import java.util.Arrays;

/**
 * Corresponds to first level of grouping in MVT
 * Structure:
 * {@link Category}
 *  |-  {@link ProductCategory}
 *      |-  {@link ProductGroup}
 *          |-  {@link ProductSubGroup}
 *              |-  {@link FoodItem}
 *
 *  E.g.: [DAIRY] - [Melk og melkeprodukter] - [Ost] - [Ost, ekstra fet] - [Ridderost]
 */
public enum ProductCategory {

    DAIRY(1, "Melk og melkeprodukter", Category.DAIRY),
    EGG(2, "Egg", Category.EGG),
    POULTRY_AND_MEAT(3, "Fjørfe og kjøtt", Category.MEAT_OR_FISH),
    FISH_AND_SHELLFISH(4, "Fisk og skalldyr", Category.MEAT_OR_FISH),
    GRAIN_BREAD_CEREALS_ETC(5, "Korn- og bakevarer, frø og nøtter", Category.VEGETABLE),
    POTATOES_FRUIT_VEGETABLES(6, "Poteter, grønnsaker, frukt og bær", Category.VEGETABLE),
    SUGAR_AND_SWEETS(7, "Sukker og søte produkter", Category.VEGETABLE),
    BUTTER_AND_OILS(8, "Margarin, smør, matolje o.l.", Category.MIXED),
    BEVERAGES(9, "Drikke", Category.OTHER),
    DISHES_AND_PRODUCTS(10, "Diverse retter, produkter og ingredienser", Category.MIXED),
    INFANT_FOODS(11, "Spedbarnsmat", Category.MIXED);


    private Integer mvtId;
    private String mvtName;
    private Category category;

    ProductCategory(Integer mvtId, String mvtName, Category category) {
        this.mvtId = mvtId;
        this.mvtName = mvtName;
        this.category = category;
    }

    public static ProductCategory fromMvtId(final Integer mvtId) {
        return Arrays.stream(values())
                .filter(u -> u.mvtId.equals(mvtId))
                .findAny()
                .orElseThrow(() -> new IllegalArgumentException("Product with id  " + mvtId + " not supported"));
    }

    public Integer getMvtId() {
        return mvtId;
    }

    public String getMvtName() {
        return mvtName;
    }

    public Category getCategory() {
        return category;
    }
}
