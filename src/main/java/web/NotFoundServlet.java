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

package web;

import jakarta.servlet.http.HttpServletRequest;

import java.util.Optional;

/**
 * Fallback for unknown paths, so they also get a JSON 404 response.
 */
class NotFoundServlet extends JsonServlet {

    @Override
    protected Optional<?> handleGet(HttpServletRequest req) {
        return Optional.empty();
    }
}
