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

package no.torvundconsulting.mvt.web;

import jakarta.servlet.http.HttpServletRequest;
import no.torvundconsulting.mvt.model.Nutrient;

import java.util.Optional;

/**
 * {@code GET /nutrient/<id>}: details for one nutrient, using the id from {@code /nutrients}.
 */
class NutrientServlet extends JsonServlet {

    @Override
    protected Optional<?> handleGet(HttpServletRequest req) {
        int id = pathId(req);
        Nutrient[] nutrients = Nutrient.values();
        if (id < 0 || id >= nutrients.length) {
            return Optional.empty();
        }
        return Optional.of(NutrientDetails.of(nutrients[id]));
    }

    private record NutrientDetails(int id, String mvtName, String name, String unit) {
        static NutrientDetails of(Nutrient nutrient) {
            return new NutrientDetails(nutrient.ordinal(), nutrient.getMvtName(), nutrient.getName(),
                    nutrient.getUnit().getSymbol());
        }
    }
}
