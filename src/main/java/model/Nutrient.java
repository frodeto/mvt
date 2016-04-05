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
    WATER("Vann", "water", MvtUnit.GRAMS),
    ENERGY("Kilojoule", "kilojoules", MvtUnit.KJOULE),
    CALORIES("Kilokalorier", "kilocalories", MvtUnit.KCAL),

    // Fats
    FAT("Fett", "fat", MvtUnit.GRAMS),
    SATURATED_FAT("Fett", "saturated fat", MvtUnit.GRAMS),
    MONOUNSATURATED_FAT("Enumettet", "monounsaturated fat", MvtUnit.GRAMS),
    POLYUNSATURATED_FAT("Flerumettet", "polyunsaturated fat", MvtUnit.GRAMS),
    TRANS_FAT("Trans", "transfat", MvtUnit.GRAMS),
    OMEGA3("Omega-3", "omega 3", MvtUnit.GRAMS),
    OMEGA6("Omega-6", "omega 6", MvtUnit.GRAMS),
    CHOLESTEROL("Kolesterol", "cholesterol", MvtUnit.MILLIGRAMS),

    // Carbs
    CARBS("Karbohydrat", "carbohydrates", MvtUnit.GRAMS),
    STARCH("Stivelse", "starch", MvtUnit.GRAMS),
    MONO_AND_DI_SACC("Mono+disakk", "mono- and di-saccharides", MvtUnit.GRAMS),

    DIETARY_FIBRE("Kostfiber", "dietary fibre", MvtUnit.GRAMS),

    PROTEIN("Protein", "protein", MvtUnit.GRAMS),

    ALCOHOL("Alkohol", "alcohol", MvtUnit.GRAMS),

    // Minerals and vitamins
    SALT("Salt", "salt", MvtUnit.GRAMS),
    VITAMIN_A("Vitamin A", "vitamin A", MvtUnit.RAE),
    RETINOL("Retinol", "retinol", MvtUnit.MICROGRAMS),
    BETA_KAROTEN("Beta-karoten", "beta-carotene", MvtUnit.MICROGRAMS),
    VITAMIN_D("Vitamin D", "vitamin D", MvtUnit.MICROGRAMS),
    VITAMIN_E("Vitamin E", "vitamin E", MvtUnit.ALFA_TE),
    TIAMIN("Tiamin", "thiamine", MvtUnit.MILLIGRAMS),
    RIBOFLAVIN("Riboflavin", "riboflavin", MvtUnit.MILLIGRAMS),
    NIACIN("Niacin", "niacin", MvtUnit.MILLIGRAMS),
    VITAMIN_B6("Vitamin B6", "vitamin B6", MvtUnit.MILLIGRAMS),
    FOLAT("Folat", "folic acid", MvtUnit.MICROGRAMS),
    VITAMIN_B12("Vitamin B12", "vitamin B12", MvtUnit.ALFA_TE),
    VITAMIN_C("Vitamin C", "vitamin C", MvtUnit.MILLIGRAMS),
    CALCIUM("Kalsium", "calcium", MvtUnit.MILLIGRAMS),
    IRON("Jern", "iron", MvtUnit.MILLIGRAMS),
    SODIUM("Natrium", "sodium", MvtUnit.MILLIGRAMS),
    POTASSIUM("Kalium", "potassium", MvtUnit.MILLIGRAMS),
    MAGNESIUM("Magnesium", "magnesium", MvtUnit.MILLIGRAMS),
    ZINC("Sink", "zinc", MvtUnit.MILLIGRAMS),
    SELENIUM("Selen", "selenium", MvtUnit.MICROGRAMS),
    COPPER("Kopper", "copper", MvtUnit.MILLIGRAMS),
    PHOSPHORUS("Fosfor", "phosphorus", MvtUnit.MILLIGRAMS),
    IODINE("Jod", "iodine", MvtUnit.MICROGRAMS);

    private String mvtName;
    private String name;
    private MvtUnit unit;

    Nutrient(String mvtName, String name, MvtUnit unit) {
        this.mvtName = mvtName;
        this.name = name;
        this.unit = unit;
    }

    public static Nutrient fromMvtName(final String mvtName) {
        return Arrays.stream(values())
                .filter(n -> n.mvtName.equalsIgnoreCase(mvtName))
                .findAny()
                .orElse(null);
                //.orElseThrow(() -> new IllegalArgumentException("Nutrient " + mvtName + " not supported"));
    }

    public String getMvtName() {
        return mvtName;
    }

    public String getName() {
        return name;
    }

    public MvtUnit getUnit() {
        return unit;
    }
}
