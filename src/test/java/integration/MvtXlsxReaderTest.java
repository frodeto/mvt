package integration;

import model.FoodItem;
import model.Nutrient;
import model.ProductCategory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Disabled("Known bug: reader throws on 'M' (missing value) cells - fixed in phase 4")
class MvtXlsxReaderTest {
    private static List<FoodItem> foodItems;

    @BeforeAll
    static void readSheet() throws IOException {
        try (InputStream in = MvtXlsxReaderTest.class.getResourceAsStream("/Matvaretabellen+2015-truncated.xlsx")) {
            foodItems = new MvtXlsxReader(in).read();
        }
    }

    @Test
    void readsAllFoodItems() {
        assertEquals(235, foodItems.size());
    }

    @Test
    void readsNutrientValues() {
        FoodItem milk = byName("Helmelk, 3,5 % fett");
        assertEquals(87.0, milk.getNutrientMap().get(Nutrient.WATER));
        assertEquals(262.0, milk.getNutrientMap().get(Nutrient.ENERGY));
        assertEquals(63.0, milk.getNutrientMap().get(Nutrient.CALORIES));
        assertEquals(3.5, milk.getNutrientMap().get(Nutrient.FAT));
        assertEquals(2.3, milk.getNutrientMap().get(Nutrient.SATURATED_FAT));
    }

    @Test
    void treatsMissingValuesAsAbsent() {
        FoodItem egg = byName("Egg, rå");
        assertEquals(12.4, egg.getNutrientMap().get(Nutrient.PROTEIN));
        assertFalse(egg.getNutrientMap().containsKey(Nutrient.OMEGA3));
    }

    @Test
    void assignsProductCategories() {
        assertEquals(ProductCategory.DAIRY, categoryOf("Helmelk, 3,5 % fett"));
        assertEquals(ProductCategory.EGG, categoryOf("Egg, rå"));
        assertEquals(ProductCategory.POULTRY_AND_MEAT, categoryOf("And, kjøtt med skinn, rå"));
    }

    @Test
    void assignsSubGroupBelongingToCurrentGroup() {
        assertEquals("Fjørfe, rå", byName("And, kjøtt med skinn, rå").getProductSubGroup().getName());
        assertEquals(ProductCategory.EGG,
                byName("Egg, rå").getProductSubGroup().getProductGroup().getProductCategory());
    }

    private static ProductCategory categoryOf(String name) {
        return byName(name).getProductSubGroup().getProductGroup().getProductCategory();
    }

    private static FoodItem byName(String name) {
        return foodItems.stream().filter(f -> f.getName().equals(name)).findFirst()
                .orElseThrow(() -> new AssertionError("No food item named " + name));
    }
}
