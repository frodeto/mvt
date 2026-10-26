package integration;

import com.google.gson.Gson;
import model.DataSource;
import model.FoodItem;
import model.Nutrient;
import model.ProductCategory;
import model.ProductGroup;
import model.ProductSubGroup;
import org.bson.Document;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class MongoReaderTest {

    @Test
    void picksNewestImportRegardlessOfOrder() {
        Map<String, LocalDateTime> imports = Map.of(
                "mvt20240101", LocalDateTime.of(2024, 1, 1, 12, 0),
                "mvt20260301", LocalDateTime.of(2026, 3, 1, 12, 0),
                "mvt20250101", LocalDateTime.of(2025, 1, 1, 12, 0));
        assertEquals(Optional.of("mvt20260301"), MongoReader.newestImport(imports));
    }

    @Test
    void foodItemSurvivesMongoDocumentRoundTrip() {
        // Same conversions as MongoWriter (FoodItem -> Document) and MongoReader (Document -> FoodItem)
        Gson gson = new Gson();
        ProductSubGroup subGroup = new ProductSubGroup(new ProductGroup(ProductCategory.EGG, "Egg, rå"), "Default");
        FoodItem egg = new FoodItem(7, "02.001", "Egg, rå", subGroup,
                Map.of(Nutrient.PROTEIN, 12.4, Nutrient.IODINE, 49.0), DataSource.MVT);

        Document document = Document.parse(gson.toJson(egg));
        FoodItem restored = gson.fromJson(document.toJson(), FoodItem.class);

        assertEquals(egg, restored);
        assertEquals("02.001", restored.mvtId());
        assertEquals(egg.nutrientMap(), restored.nutrientMap());
        assertEquals(subGroup, restored.productSubGroup());
        assertEquals(7, document.getInteger("id"));
    }

    @Test
    void findsNothingWithoutImports() {
        assertEquals(Optional.empty(), MongoReader.newestImport(Map.of()));
    }
}
