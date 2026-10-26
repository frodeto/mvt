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

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Logs each request and its response status and duration.
 */
class RequestLoggingFilter extends HttpFilter {
    private static final Logger logger = LoggerFactory.getLogger(RequestLoggingFilter.class);

    @Override
    protected void doFilter(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws IOException, ServletException {
        long start = System.nanoTime();
        String request = req.getQueryString() == null
                ? req.getMethod() + " " + req.getRequestURI()
                : req.getMethod() + " " + req.getRequestURI() + "?" + req.getQueryString();
        logger.info("Received {}", request);
        try {
            chain.doFilter(req, res);
        } finally {
            logger.info("Responded {} to {} in {} ms", res.getStatus(), request, (System.nanoTime() - start) / 1_000_000);
        }
    }
}
