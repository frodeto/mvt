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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Optional;

/**
 * Base class for read-only endpoints that respond with JSON.
 * An empty result gives 404 Not Found, and a {@link BadRequestException} gives 400 Bad Request.
 */
abstract class JsonServlet extends HttpServlet {
    private static final Gson GSON = new GsonBuilder().disableHtmlEscaping().create();

    @Override
    protected final void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            Optional<?> body = handleGet(req);
            if (body.isPresent()) {
                write(resp, HttpServletResponse.SC_OK, body.get());
            } else {
                write(resp, HttpServletResponse.SC_NOT_FOUND, new ErrorResponse("Not found: " + req.getRequestURI()));
            }
        } catch (BadRequestException e) {
            write(resp, HttpServletResponse.SC_BAD_REQUEST, new ErrorResponse(e.getMessage()));
        }
    }

    protected abstract Optional<?> handleGet(HttpServletRequest req);

    /**
     * Parses the numeric id in a path like {@code /fooditem/42}.
     */
    protected static int pathId(HttpServletRequest req) {
        String pathInfo = req.getPathInfo();
        String id = pathInfo == null ? "" : pathInfo.substring(1);
        try {
            return Integer.parseInt(id);
        } catch (NumberFormatException e) {
            throw new BadRequestException("Invalid id: '" + id + "'");
        }
    }

    protected static String requiredParameter(HttpServletRequest req, String name) {
        String value = req.getParameter(name);
        if (value == null || value.isBlank()) {
            throw new BadRequestException("Missing query parameter '" + name + "'");
        }
        return value.strip();
    }

    private static void write(HttpServletResponse resp, int status, Object body) throws IOException {
        resp.setStatus(status);
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        GSON.toJson(body, resp.getWriter());
    }

    private record ErrorResponse(String error) {
    }
}
