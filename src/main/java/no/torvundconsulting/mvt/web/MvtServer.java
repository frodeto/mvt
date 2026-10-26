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

import jakarta.servlet.Filter;
import jakarta.servlet.http.HttpServlet;
import no.torvundconsulting.mvt.control.MvtAnalyzer;
import no.torvundconsulting.mvt.integration.FoodItemRepository;
import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.startup.Tomcat;
import org.apache.tomcat.util.descriptor.web.FilterDef;
import org.apache.tomcat.util.descriptor.web.FilterMap;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;

/**
 * Embedded Tomcat serving the MVT JSON API.
 */
public final class MvtServer implements AutoCloseable {
    private final Tomcat tomcat = new Tomcat();

    /**
     * @param port the port to listen on, or 0 for any free port
     */
    public MvtServer(int port, MvtAnalyzer analyzer, FoodItemRepository foodItems) {
        tomcat.setBaseDir(createBaseDir());
        tomcat.setPort(port);
        tomcat.getConnector().setProperty("useVirtualThreads", "true");

        StandardContext context = (StandardContext) tomcat.addContext("", null);
        // Leak detection is for redeployed webapps; it does not apply to this single embedded context
        context.setClearReferencesThreadLocals(false);
        context.setClearReferencesRmiTargets(false);
        addServlet(context, "search", new SearchServlet(analyzer), "/search");
        addServlet(context, "nutrients", new NutrientsServlet(), "/nutrients");
        addServlet(context, "nutrient", new NutrientServlet(), "/nutrient/*");
        addServlet(context, "fooditem", new FoodItemServlet(foodItems), "/fooditem/*");
        addServlet(context, "notFound", new NotFoundServlet(), "/");
        addFilter(context, "requestLogging", new RequestLoggingFilter(), "/*");
    }

    public void start() throws LifecycleException {
        tomcat.start();
    }

    /**
     * Blocks until the server is stopped.
     */
    public void await() {
        tomcat.getServer().await();
    }

    /**
     * The port the server listens on. Only valid after {@link #start()}.
     */
    public int port() {
        return tomcat.getConnector().getLocalPort();
    }

    @Override
    public void close() throws LifecycleException {
        tomcat.stop();
        tomcat.destroy();
    }

    private static void addServlet(Context context, String name, HttpServlet servlet, String mapping) {
        Tomcat.addServlet(context, name, servlet);
        context.addServletMapping(mapping, name);
    }

    private static void addFilter(Context context, String name, Filter filter, String urlPattern) {
        FilterDef filterDef = new FilterDef();
        filterDef.setFilterName(name);
        filterDef.setFilter(filter);
        context.addFilterDef(filterDef);

        FilterMap filterMap = new FilterMap();
        filterMap.setFilterName(name);
        filterMap.addURLPattern(urlPattern);
        context.addFilterMap(filterMap);
    }

    private static String createBaseDir() {
        try {
            return Files.createTempDirectory("mvt-tomcat").toString();
        } catch (IOException e) {
            throw new UncheckedIOException("Could not create Tomcat base directory", e);
        }
    }
}
