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

package no.torvundconsulting.mvt.model;

import java.util.Arrays;

/**
 * The food categories in Matvaretabellen, in the order they appear in the spreadsheet.
 * Structure:
 * {@link Category}
 *  |-  {@link ProductCategory}
 *      |-  {@link FoodItem}
 *
 *  E.g.: [DAIRY] - [Meieriprodukter] - [Appenzeller, ost]
 */
public enum ProductCategory {

    DAIRY("Meieriprodukter", Category.DAIRY),
    EGG("Egg", Category.EGG),
    MEAT_AND_POULTRY("Kjøtt og fjørfe", Category.MEAT_OR_FISH),
    FISH_AND_SHELLFISH("Fisk og skalldyr", Category.MEAT_OR_FISH),
    LEGUMES("Belgvekster", Category.VEGETABLE),
    FRUIT_AND_BERRIES("Frukt og bær", Category.VEGETABLE),
    NUTS_AND_SEEDS("Nøtter og frø", Category.VEGETABLE),
    POTATOES("Poteter", Category.VEGETABLE),
    HERBS_AND_SPICES("Urter og krydder", Category.VEGETABLE),
    GRAIN_AND_BAKERY("Korn- og bakevarer", Category.VEGETABLE),
    VEGETABLES("Grønnsaker", Category.VEGETABLE),
    BEVERAGES("Drikke", Category.OTHER),
    MIXED_FOODS_AND_DISHES("Diverse matvarer og retter", Category.MIXED),
    INFANT_FOODS("Spedbarnsmat", Category.MIXED),
    SUGAR_AND_SWEETS("Sukker og søte produkter", Category.VEGETABLE),
    FATS_AND_OILS("Spisefett", Category.MIXED);

    private final String mvtName;
    private final Category category;

    ProductCategory(String mvtName, Category category) {
        this.mvtName = mvtName;
        this.category = category;
    }

    public static ProductCategory fromMvtName(final String mvtName) {
        return Arrays.stream(values())
                .filter(c -> c.mvtName.equals(mvtName))
                .findAny()
                .orElseThrow(() -> new IllegalArgumentException("Product category '" + mvtName + "' not supported"));
    }

    public String getMvtName() {
        return mvtName;
    }

    public Category getCategory() {
        return category;
    }
}
