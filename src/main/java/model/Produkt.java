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
 */
public enum Produkt {

    DAIRY(1, "Melk og melkeprodukter", Kategori.DAIRY),
    EGG(2, "Egg", Kategori.EGG),
    POULTRY_AND_MEAT(3, "Fjørfe og kjøtt", Kategori.MEAT_OR_FISH),
    FISH_AND_SHELLFISH(4, "Fisk og skalldyr", Kategori.MEAT_OR_FISH),
    GRAIN_BREAD_CEREALS_ETC(5, "Korn- og bakevarer, frø og nøtter", Kategori.VEGETABLE),
    POTATOES_FRUIT_VEGETABLES(6, "Poteter, grønnsaker, frukt og bær", Kategori.VEGETABLE),
    SUGAR_AND_SWEETS(7, "Sukker og søte produkter\n", Kategori.VEGETABLE);


    private Integer mvtId;
    private String mvtName;
    private Kategori category;

    Produkt(Integer mvtId, String mvtName, Kategori category) {
        this.mvtId = mvtId;
        this.mvtName = mvtName;
        this.category = category;
    }

    public static Produkt fromMvtId(final Integer mvtId) {
        return Arrays.stream(values())
                .filter(u -> u.mvtId.equals(mvtId))
                .findAny()
                .orElseThrow(() -> new IllegalArgumentException("Product with id  " + mvtId + " not supported"));
    }
}
