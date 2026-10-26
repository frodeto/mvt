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
 * "Nutrients" found in mvt data (including water and energy).
 * <p>
 * Each nutrient has a short name used in the API (the column names of Matvaretabellen 2015, e.g. "Jern"), and the
 * name of its column in the current spreadsheet, which is followed by the unit, e.g. "Jern (Fe) (mg)".
 */
public enum Nutrient {
    // Non-nutrients
    WATER("Vann", "Vann", "water", MvtUnit.GRAMS),
    ENERGY("Kilojoule", "Kilojoule", "kilojoules", MvtUnit.KJOULE),
    CALORIES("Kilokalorier", "Kilokalorier", "kilocalories", MvtUnit.KCAL),

    // Fats
    FAT("Fett", "Fett", "fat", MvtUnit.GRAMS),
    SATURATED_FAT("Mettet", "Mettede fettsyrer", "saturated fat", MvtUnit.GRAMS),
    MONOUNSATURATED_FAT("Enumettet", "Enumettede fettsyrer", "monounsaturated fat", MvtUnit.GRAMS),
    POLYUNSATURATED_FAT("Flerumettet", "Flerumettede fettsyrer", "polyunsaturated fat", MvtUnit.GRAMS),
    TRANS_FAT("Trans", "Transfettsyrer", "transfat", MvtUnit.GRAMS),
    OMEGA3("Omega-3", "Omega-3", "omega 3", MvtUnit.GRAMS),
    OMEGA6("Omega-6", "Omega-6", "omega 6", MvtUnit.GRAMS),
    CHOLESTEROL("Kolesterol", "Kolesterol", "cholesterol", MvtUnit.MILLIGRAMS),

    // Carbs
    CARBS("Karbohydrat", "Karbohydrat", "carbohydrates", MvtUnit.GRAMS),
    STARCH("Stivelse", "Stivelse", "starch", MvtUnit.GRAMS),
    MONO_AND_DI_SACC("Mono+disakk", "Sukkerarter", "mono- and di-saccharides", MvtUnit.GRAMS),
    ADDED_SUGAR("Sukker, tilsatt", "Sukker, tilsatt", "added sugar", MvtUnit.GRAMS),

    DIETARY_FIBRE("Kostfiber", "Kostfiber", "dietary fibre", MvtUnit.GRAMS),

    PROTEIN("Protein", "Protein", "protein", MvtUnit.GRAMS),

    ALCOHOL("Alkohol", "Alkohol", "alcohol", MvtUnit.GRAMS),

    // Minerals and vitamins
    SALT("Salt", "Salt (NaCl)", "salt", MvtUnit.GRAMS),
    VITAMIN_A("Vitamin A", "Vitamin A (RAE)", "vitamin A", MvtUnit.RAE),
    RETINOL("Retinol", "Retinol", "retinol", MvtUnit.MICROGRAMS),
    BETA_KAROTEN("Beta-karoten", "Betakaroten", "beta-carotene", MvtUnit.MICROGRAMS),
    VITAMIN_D("Vitamin D", "Vitamin D", "vitamin D", MvtUnit.MICROGRAMS),
    VITAMIN_E("Vitamin E", "Vitamin E", "vitamin E", MvtUnit.ALFA_TE),
    TIAMIN("Tiamin", "Vitamin B1 (tiamin)", "thiamine", MvtUnit.MILLIGRAMS),
    RIBOFLAVIN("Riboflavin", "Vitamin B2 (riboflavin)", "riboflavin", MvtUnit.MILLIGRAMS),
    NIACIN("Niacin", "Vitamin B3 (niacin)", "niacin", MvtUnit.MILLIGRAMS),
    VITAMIN_B6("Vitamin B6", "Vitamin B6 (pyridoksin)", "vitamin B6", MvtUnit.MILLIGRAMS),
    FOLAT("Folat", "Vitamin B9 (folat)", "folic acid", MvtUnit.MICROGRAMS),
    VITAMIN_B12("Vitamin B12", "Vitamin B12 (kobalamin)", "vitamin B12", MvtUnit.MICROGRAMS),
    VITAMIN_C("Vitamin C", "Vitamin C (askorbinsyre)", "vitamin C", MvtUnit.MILLIGRAMS),
    CALCIUM("Kalsium", "Kalsium (Ca)", "calcium", MvtUnit.MILLIGRAMS),
    IRON("Jern", "Jern (Fe)", "iron", MvtUnit.MILLIGRAMS),
    SODIUM("Natrium", "Natrium (Na)", "sodium", MvtUnit.MILLIGRAMS),
    POTASSIUM("Kalium", "Kalium (K)", "potassium", MvtUnit.MILLIGRAMS),
    MAGNESIUM("Magnesium", "Magnesium (Mg)", "magnesium", MvtUnit.MILLIGRAMS),
    ZINC("Sink", "Sink (Zn)", "zinc", MvtUnit.MILLIGRAMS),
    SELENIUM("Selen", "Selen (Se)", "selenium", MvtUnit.MICROGRAMS),
    COPPER("Kopper", "Kobber (Cu)", "copper", MvtUnit.MILLIGRAMS),
    PHOSPHORUS("Fosfor", "Fosfor (P)", "phosphorus", MvtUnit.MILLIGRAMS),
    IODINE("Jod", "Jod (I)", "iodine", MvtUnit.MICROGRAMS);

    private final String mvtName;
    private final String sheetName;
    private final String name;
    private final MvtUnit unit;

    Nutrient(String mvtName, String sheetName, String name, MvtUnit unit) {
        this.mvtName = mvtName;
        this.sheetName = sheetName;
        this.name = name;
        this.unit = unit;
    }

    public static Nutrient fromMvtName(final String mvtName) {
        return Arrays.stream(values())
                .filter(n -> n.mvtName.equalsIgnoreCase(mvtName))
                .findAny()
                .orElse(null);
    }

    public String getMvtName() {
        return mvtName;
    }

    /**
     * The nutrient's column header in the spreadsheet, e.g. "Jern (Fe) (mg)".
     */
    public String getColumnName() {
        return sheetName + " (" + unit.getSymbol() + ")";
    }

    public String getName() {
        return name;
    }

    public MvtUnit getUnit() {
        return unit;
    }
}
