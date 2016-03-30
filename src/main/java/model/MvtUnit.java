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
 * Units used in mvt data
 */
public enum MvtUnit {
    KCAL("kcal"),
    KJOULE("kJ"),
    GRAMS("g"),
    MILLIGRAMS("mg"),
    MICROGRAMS("µg"),
    RAE("RAE"),
    ALFA_TE("alfa-TE");

    private String symbol;

    MvtUnit(String symbol) {
        this.symbol = symbol;
    }

    public static MvtUnit fromSymbol(final String symbol) {
        return Arrays.stream(values())
                .filter(u -> u.symbol.equals(symbol))
                .findAny()
                .orElseThrow(() -> new IllegalArgumentException("Unit " + symbol + " not supported"));
    }

    public String getSymbol() {
        return symbol;
    }
}
