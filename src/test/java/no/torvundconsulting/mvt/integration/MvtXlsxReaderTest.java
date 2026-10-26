package no.torvundconsulting.mvt.integration;

import no.torvundconsulting.mvt.model.FoodItem;
import no.torvundconsulting.mvt.model.Nutrient;
import no.torvundconsulting.mvt.model.ProductCategory;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class MvtXlsxReaderTest {
    private static List<FoodItem> foodItems;

    @BeforeAll
    static void readSheet() throws IOException {
        try (InputStream in = MvtXlsxReaderTest.class.getResourceAsStream("/Matvaretabellen+2026.xlsx")) {
            foodItems = new MvtXlsxReader(in).read();
        }
    }

    @Test
    void readsAllFoodItems() {
        assertEquals(2121, foodItems.size());
        assertTrue(foodItems.stream().allMatch(item -> item.mvtId().matches("\\d{2}\\.\\d{3}")));
    }

    @Test
    void keepsSourceIdAndAssignsSequentialIds() {
        FoodItem first = foodItems.getFirst();
        assertEquals(1, first.id());
        assertEquals("01.344", first.mvtId());
        assertEquals("Appenzeller, ost", first.name());
    }

    @Test
    void readsNutrientValuesStoredAsText() {
        FoodItem appenzeller = byName("Appenzeller, ost");
        assertEquals(1586.0, appenzeller.nutrientMap().get(Nutrient.ENERGY));
        assertEquals(383.0, appenzeller.nutrientMap().get(Nutrient.CALORIES));
        assertEquals(44.0, appenzeller.nutrientMap().get(Nutrient.WATER));
        assertEquals(31.7, appenzeller.nutrientMap().get(Nutrient.FAT));
        assertEquals(19.0, appenzeller.nutrientMap().get(Nutrient.SATURATED_FAT));
        assertEquals(0.6, appenzeller.nutrientMap().get(Nutrient.TRANS_FAT));
        assertEquals(24.3, appenzeller.nutrientMap().get(Nutrient.PROTEIN));
        assertEquals(0.3, appenzeller.nutrientMap().get(Nutrient.IRON));
        assertEquals(1.0, appenzeller.nutrientMap().get(Nutrient.VITAMIN_E));
    }

    @Test
    void treatsEmptyValuesAsUnknown() {
        FoodItem burrata = byName("Burrata, ost");
        assertEquals(25.7, burrata.nutrientMap().get(Nutrient.FAT));
        assertFalse(burrata.nutrientMap().containsKey(Nutrient.TRANS_FAT));
    }

    @Test
    void assignsCategoryFromPrecedingCategoryRow() {
        assertEquals(ProductCategory.DAIRY, byName("Appenzeller, ost").productCategory());
        assertEquals(ProductCategory.EGG, byName("Egg, kokt").productCategory());
        assertEquals(ProductCategory.SUGAR_AND_SWEETS, byName("Agavesirup").productCategory());
        assertEquals(ProductCategory.FATS_AND_OILS, byName("Aioli").productCategory());
        // The id prefix does not decide the category: 01.xxx is not always dairy
        assertEquals(ProductCategory.MIXED_FOODS_AND_DISHES, byName("Kaffehvitt").productCategory());
    }

    @Test
    void usesAllCategories() {
        assertEquals(EnumSet.allOf(ProductCategory.class),
                foodItems.stream().map(FoodItem::productCategory).collect(Collectors.toCollection(() -> EnumSet.noneOf(ProductCategory.class))));
    }

    @Test
    void stripsSurroundingWhitespace() {
        // The sheet has the id "01.332 " and names with trailing spaces
        assertTrue(foodItems.stream().anyMatch(item -> item.mvtId().equals("01.332")));
        assertEquals(ProductCategory.FISH_AND_SHELLFISH,
                byName("Akkar, tiarmet blekksprut, innbakt, forfritert, fryst").productCategory());
        assertTrue(foodItems.stream().allMatch(item -> item.name().equals(item.name().strip())));
    }

    @Test
    void rejectsWorkbookWithoutSheet() {
        var e = assertThrows(MvtXlsxReader.XlsxReaderException.class,
                () -> read(workbook("Matvarer", header(), List.of())));
        assertTrue(e.getMessage().contains(MvtXlsxReader.SHEET_NAME), e.getMessage());
    }

    @Test
    void rejectsMissingNutrientColumn() {
        List<String> header = new ArrayList<>(header());
        header.remove(Nutrient.IRON.getColumnName());
        var e = assertThrows(MvtXlsxReader.XlsxReaderException.class,
                () -> read(workbook(MvtXlsxReader.SHEET_NAME, header, List.of())));
        assertTrue(e.getMessage().contains("Jern (Fe) (mg)"), e.getMessage());
    }

    @Test
    void rejectsUnknownCategory() {
        var e = assertThrows(MvtXlsxReader.XlsxReaderException.class,
                () -> read(workbook(MvtXlsxReader.SHEET_NAME, header(), List.of(List.of("Romkost")))));
        assertTrue(e.getMessage().contains("Romkost"), e.getMessage());
    }

    @Test
    void rejectsInvalidValue() {
        List<List<String>> rows = List.of(List.of("Egg"), List.of("02.001", "Egg, rå", "1,5"));
        var e = assertThrows(MvtXlsxReader.XlsxReaderException.class,
                () -> read(workbook(MvtXlsxReader.SHEET_NAME, header(), rows)));
        assertTrue(e.getMessage().contains("'1,5'"), e.getMessage());
    }

    @Test
    void rejectsFoodItemBeforeCategory() {
        assertThrows(MvtXlsxReader.XlsxReaderException.class,
                () -> read(workbook(MvtXlsxReader.SHEET_NAME, header(), List.of(List.of("02.001", "Egg, rå")))));
    }

    private static FoodItem byName(String name) {
        return foodItems.stream().filter(f -> f.name().equals(name)).findFirst()
                .orElseThrow(() -> new AssertionError("No food item named " + name));
    }

    private static List<FoodItem> read(InputStream in) {
        return new MvtXlsxReader(in).read();
    }

    private static List<String> header() {
        List<String> header = new ArrayList<>(List.of("Matvare ID", "Matvare"));
        Arrays.stream(Nutrient.values()).map(Nutrient::getColumnName).forEach(header::add);
        return header;
    }

    private static InputStream workbook(String sheetName, List<String> header, List<List<String>> rows) throws IOException {
        try (XSSFWorkbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet(sheetName);
            List<List<String>> allRows = new ArrayList<>();
            allRows.add(header);
            allRows.addAll(rows);
            for (int r = 0; r < allRows.size(); r++) {
                Row row = sheet.createRow(r);
                for (int c = 0; c < allRows.get(r).size(); c++) {
                    row.createCell(c).setCellValue(allRows.get(r).get(c));
                }
            }
            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        }
    }
}
