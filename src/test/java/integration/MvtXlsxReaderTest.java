package integration;

import model.FoodItem;
import model.Nutrient;
import model.ProductCategory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MvtXlsxReaderTest {
    private static List<FoodItem> foodItems;
    private static List<FoodItem> allFoodItems;

    @BeforeAll
    static void readSheets() throws IOException {
        foodItems = read("/Matvaretabellen+2015-truncated.xlsx");
        allFoodItems = read("/Matvaretabellen+2015.xlsx");
    }

    @Test
    void readsAllFoodItems() {
        assertEquals(235, foodItems.size());
        assertEquals(1543, allFoodItems.size());
    }

    @Test
    void skipsFooterRows() {
        assertTrue(allFoodItems.stream().noneMatch(item -> item.getName().isEmpty()));
        assertTrue(allFoodItems.stream().allMatch(item -> item.getMvtId().matches("\\d{2}\\.\\d{3}")));
    }

    @Test
    void keepsSourceIdAndAssignsSequentialIds() {
        FoodItem milk = foodItems.getFirst();
        assertEquals(1, milk.getId());
        assertEquals("01.001", milk.getMvtId());
        assertEquals("Helmelk, 3,5 % fett", milk.getName());
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
    void assignsCategoriesStoredAsNumbers() {
        // Category rows 1, 3, 8 and 11 are numeric cells (1.0, 3.0, ...) in the sheet
        assertEquals(ProductCategory.BUTTER_AND_OILS, categoryOf(allFoodItems, "Smør"));
        assertEquals(ProductCategory.INFANT_FOODS, categoryOf(allFoodItems, "Morsmelk"));
        assertTrue(allFoodItems.stream().allMatch(item ->
                item.getProductSubGroup().getProductGroup().getProductCategory() != null));
    }

    @Test
    void usesDeepestGroupLevelAsSubGroup() {
        // 4.1.1 Fet fisk > 4.1.1.1 Fet fisk, rå > 04.001 Blåkveite
        assertEquals("Fet fisk, rå", byName(allFoodItems, "Blåkveite, rå/kaldrøkt").getProductSubGroup().getName());
    }

    @Test
    void readsAddedSugar() {
        assertEquals(0.0, byName("Helmelk, 3,5 % fett").getNutrientMap().get(Nutrient.ADDED_SUGAR));
    }

    @Test
    void assignsSubGroupBelongingToCurrentGroup() {
        assertEquals("Fjørfe, rå", byName("And, kjøtt med skinn, rå").getProductSubGroup().getName());
        assertEquals(ProductCategory.EGG,
                byName("Egg, rå").getProductSubGroup().getProductGroup().getProductCategory());
    }

    private static List<FoodItem> read(String resource) throws IOException {
        try (InputStream in = MvtXlsxReaderTest.class.getResourceAsStream(resource)) {
            return new MvtXlsxReader(in).read();
        }
    }

    private static ProductCategory categoryOf(String name) {
        return categoryOf(foodItems, name);
    }

    private static ProductCategory categoryOf(List<FoodItem> items, String name) {
        return byName(items, name).getProductSubGroup().getProductGroup().getProductCategory();
    }

    private static FoodItem byName(String name) {
        return byName(foodItems, name);
    }

    private static FoodItem byName(List<FoodItem> items, String name) {
        return items.stream().filter(f -> f.getName().equals(name)).findFirst()
                .orElseThrow(() -> new AssertionError("No food item named " + name));
    }
}
