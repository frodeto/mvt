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
import no.torvundconsulting.mvt.control.MvtAnalyzer;
import no.torvundconsulting.mvt.model.Nutrient;

import java.util.Optional;

/**
 * {@code GET /search?Nutrient=<mvt name>&Amount=<level>}: food items with at least the given amount of a nutrient.
 */
class SearchServlet extends JsonServlet {
    private final transient MvtAnalyzer analyzer;

    SearchServlet(MvtAnalyzer analyzer) {
        this.analyzer = analyzer;
    }

    @Override
    protected Optional<?> handleGet(HttpServletRequest req) {
        String nutrientName = requiredParameter(req, "Nutrient");
        Nutrient nutrient = Nutrient.fromMvtName(nutrientName);
        if (nutrient == null) {
            throw new BadRequestException("Unknown nutrient: '" + nutrientName + "'");
        }
        return Optional.of(analyzer.getAboveLevel(nutrient, parseAmount(requiredParameter(req, "Amount"))));
    }

    private static double parseAmount(String amount) {
        try {
            double value = Double.parseDouble(amount);
            if (Double.isFinite(value)) {
                return value;
            }
        } catch (NumberFormatException e) {
            // handled below
        }
        throw new BadRequestException("Invalid amount: '" + amount + "'");
    }
}
