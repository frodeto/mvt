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
 * "Nutritients" found in mvt data (including water and enrgy)
 */
public enum Nutrient {
    // Non-nutrients
    WATER("Vann", 0, "water", MvtUnit.GRAMS),
    ENERGY("Kilojoule", 0, "kilojoules", MvtUnit.KJOULE),
    CALORIES("Kilokalorier", 0, "kilocalories", MvtUnit.KCAL),

    // Fats
    FAT("Fett", 0, "fat", MvtUnit.GRAMS),
    SATURATED_FAT("Fett", 0, "saturated fat", MvtUnit.GRAMS),
    MONOUNSATURATED_FAT("Enumettet", 0, "monounsaturated fat", MvtUnit.GRAMS),
    POLYUNSATURATED_FAT("Flerumettet", 0, "polyunsaturated fat", MvtUnit.GRAMS),
    TRANS_FAT("Trans", 0, "transfat", MvtUnit.GRAMS),
    OMEGA3("Omega-3", 0, "omega 3", MvtUnit.GRAMS),
    OMEGA6("Omega-6", 0, "omega 6", MvtUnit.GRAMS),
    CHOLESTEROL("Kolesterol", 0, "cholesterol", MvtUnit.MILLIGRAMS),

    // Carbs
    CARBS("Karbohydrat", 0, "carbohydrates", MvtUnit.GRAMS),
    STARCH("Stivelse", 0, "starch", MvtUnit.GRAMS),
    MONO_AND_DI_SACC("Mono+disakk", 0, "mono- and di-saccharides", MvtUnit.GRAMS),

    DIETARY_FIBRE("Kostfiber", 0, "dietary fibre", MvtUnit.GRAMS),

    PROTEIN("Protein", 0, "protein", MvtUnit.GRAMS),

    ALCOHOL("Alkohol", 0, "alcohol", MvtUnit.GRAMS),

    // Minerals and vitamins
    SALT("Salt", 0, "salt", MvtUnit.GRAMS),
    VITAMIN_A("Vitamin A", 0, "vitamin A", MvtUnit.RAE),
    RETINOL("Retinol", 0, "retinol", MvtUnit.MICROGRAMS),
    BETA_KAROTEN("Beta-karoten", 0, "beta-carotene", MvtUnit.MICROGRAMS),
    VITAMIN_D("Vitamin D", 0, "vitamin D", MvtUnit.MICROGRAMS),
    VITAMIN_E("Vitamin E", 0, "vitamin E", MvtUnit.ALFA_TE),
    TIAMIN("Tiamin", 0, "thiamine", MvtUnit.MILLIGRAMS),
    RIBOFLAVIN("Riboflavin", 0, "riboflavin", MvtUnit.MILLIGRAMS),
    NIACIN("Niacin", 0, "niacin", MvtUnit.MILLIGRAMS),
    VITAMIN_B6("Vitamin B6", 0, "vitamin B6", MvtUnit.MILLIGRAMS),
    FOLAT("Folat", 0, "folic acid", MvtUnit.MICROGRAMS),
    VITAMIN_B12("Vitamin B12", 0, "vitamin B12", MvtUnit.ALFA_TE),
    VITAMIN_C("Vitamin C", 0, "vitamin C", MvtUnit.MILLIGRAMS),
    CALCIUM("Kalsium", 0, "calcium", MvtUnit.MILLIGRAMS),
    IRON("Jern", 0, "iron", MvtUnit.MILLIGRAMS),
    SODIUM("Natrium", 0, "sodium", MvtUnit.MILLIGRAMS),
    POTASSIUM("Kalium", 0, "potassium", MvtUnit.MILLIGRAMS),
    MAGNESIUM("Magnesium", 0, "magnesium", MvtUnit.MILLIGRAMS),
    ZINC("Sink", 0, "zinc", MvtUnit.MILLIGRAMS),
    SELENIUM("Selen", 0, "selenium", MvtUnit.MICROGRAMS),
    COPPER("Kopper", 0, "copper", MvtUnit.MILLIGRAMS),
    PHOSPHORUS("Fosfor", 0, "phosphorus", MvtUnit.MILLIGRAMS),
    IODINE("Jod", 0, "iodine", MvtUnit.MICROGRAMS);

    private String mvtName;
    private String name;
    private MvtUnit unit;

    Nutrient(String mvtName, int mvtColumn, String name, MvtUnit unit) {
        this.mvtName = mvtName;
        this.name = name;
        this.unit = unit;
    }

    public static Nutrient fromMvtName(final String mvtName) {
        return Arrays.stream(values())
                .filter(n -> n.mvtName.equals(mvtName))
                .findAny()
                .orElse(null);
                //.orElseThrow(() -> new IllegalArgumentException("Nutrient " + mvtName + " not supported"));
    }

}
