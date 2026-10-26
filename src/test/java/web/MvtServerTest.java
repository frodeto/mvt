package web;

import control.MvtJAnalyzer;
import integration.FoodItemRepository;
import model.DataSource;
import model.FoodItem;
import model.Nutrient;
import model.ProductCategory;
import model.ProductGroup;
import model.ProductSubGroup;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class MvtServerTest {
    private static final ProductSubGroup EGG_GROUP =
            new ProductSubGroup(new ProductGroup(ProductCategory.EGG, "Egg, rå"), "Default");
    private static final List<FoodItem> FOOD_ITEMS = List.of(
            new FoodItem(1, "01.002", "Lettmelk", EGG_GROUP, Map.of(Nutrient.PROTEIN, 3.4, Nutrient.WATER, 90.0), DataSource.MVT),
            new FoodItem(2, "02.001", "Egg, rå", EGG_GROUP, Map.of(Nutrient.PROTEIN, 12.4, Nutrient.WATER, 75.0), DataSource.MVT));

    private static MvtServer server;
    private static final HttpClient client = HttpClient.newHttpClient();

    @BeforeAll
    static void startServer() throws Exception {
        server = new MvtServer(0, new MvtJAnalyzer(FOOD_ITEMS), new InMemoryRepository());
        server.start();
    }

    @AfterAll
    static void stopServer() throws Exception {
        server.close();
    }

    @Test
    void listsNutrients() throws Exception {
        HttpResponse<String> response = get("/nutrients");
        assertEquals(200, response.statusCode());
        assertEquals("application/json;charset=UTF-8", response.headers().firstValue("Content-Type").orElseThrow());
        assertTrue(response.body().startsWith("[{\"id\":0,\"name\":\"Vann\"}"), response.body());
    }

    @Test
    void searchesAboveLevel() throws Exception {
        HttpResponse<String> response = get("/search?Nutrient=Protein&Amount=10");
        assertEquals(200, response.statusCode());
        assertEquals("[{\"id\":2,\"name\":\"Egg, rå\",\"dataSource\":\"MVT\"}]", response.body());
    }

    @Test
    void rejectsSearchWithMissingAmount() throws Exception {
        HttpResponse<String> response = get("/search?Nutrient=Protein");
        assertEquals(400, response.statusCode());
        assertEquals("{\"error\":\"Missing query parameter 'Amount'\"}", response.body());
    }

    @Test
    void rejectsSearchWithInvalidAmount() throws Exception {
        assertEquals(400, get("/search?Nutrient=Protein&Amount=lots").statusCode());
        assertEquals(400, get("/search?Nutrient=Protein&Amount=NaN").statusCode());
    }

    @Test
    void rejectsSearchForUnknownNutrient() throws Exception {
        assertEquals(400, get("/search?Nutrient=Kryptonitt&Amount=1").statusCode());
    }

    @Test
    void retrievesFoodItem() throws Exception {
        HttpResponse<String> response = get("/fooditem/2");
        assertEquals(200, response.statusCode());
        assertTrue(response.body().contains("\"name\":\"Egg, rå\""), response.body());
        assertTrue(response.body().contains("\"PROTEIN\":12.4"), response.body());
    }

    @Test
    void returnsNotFoundForUnknownFoodItem() throws Exception {
        assertEquals(404, get("/fooditem/999").statusCode());
    }

    @Test
    void rejectsInvalidFoodItemId() throws Exception {
        assertEquals(400, get("/fooditem/abc").statusCode());
        assertEquals(400, get("/fooditem/").statusCode());
    }

    @Test
    void retrievesNutrient() throws Exception {
        HttpResponse<String> response = get("/nutrient/" + Nutrient.IRON.ordinal());
        assertEquals(200, response.statusCode());
        assertEquals("{\"id\":" + Nutrient.IRON.ordinal() + ",\"mvtName\":\"Jern\",\"name\":\"iron\",\"unit\":\"mg\"}",
                response.body());
    }

    @Test
    void returnsNotFoundForUnknownNutrient() throws Exception {
        assertEquals(404, get("/nutrient/" + Nutrient.values().length).statusCode());
        assertEquals(404, get("/nutrient/-1").statusCode());
    }

    @Test
    void returnsJsonNotFoundForUnknownPath() throws Exception {
        HttpResponse<String> response = get("/no/such/path");
        assertEquals(404, response.statusCode());
        assertEquals("{\"error\":\"Not found: /no/such/path\"}", response.body());
    }

    private static HttpResponse<String> get(String path) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder(URI.create("http://localhost:" + server.port() + path)).build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private static class InMemoryRepository implements FoodItemRepository {
        @Override
        public List<FoodItem> findAll() {
            return FOOD_ITEMS;
        }

        @Override
        public Optional<FoodItem> findById(int id) {
            return FOOD_ITEMS.stream().filter(item -> item.id() == id).findFirst();
        }
    }
}
