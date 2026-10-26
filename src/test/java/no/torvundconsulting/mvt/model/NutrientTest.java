package no.torvundconsulting.mvt.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class NutrientTest {

    @ParameterizedTest
    @CsvSource(delimiter = ';', value = {"Vann;WATER", "Kilojoule;ENERGY", "Fett;FAT", "Mettet;SATURATED_FAT",
            "Sukker, tilsatt;ADDED_SUGAR", "Protein;PROTEIN", "Vitamin B12;VITAMIN_B12", "Jod;IODINE"})
    void mapsSpreadsheetHeaderToNutrient(String header, Nutrient expected) {
        assertEquals(expected, Nutrient.fromMvtName(header));
    }

    @ParameterizedTest
    @CsvSource(delimiter = ';', value = {"WATER;Vann (g)", "SATURATED_FAT;Mettede fettsyrer (g)", "IRON;Jern (Fe) (mg)",
            "VITAMIN_A;Vitamin A (RAE) (RAE)", "VITAMIN_E;Vitamin E (mg-ATE)", "COPPER;Kobber (Cu) (mg)"})
    void hasSpreadsheetColumnName(Nutrient nutrient, String columnName) {
        assertEquals(columnName, nutrient.getColumnName());
    }

    @Test
    void hasUniqueColumnNames() {
        assertEquals(Nutrient.values().length, Arrays.stream(Nutrient.values()).map(Nutrient::getColumnName).distinct().count());
    }

    @Test
    void mapsCaseInsensitively() {
        assertEquals(Nutrient.WATER, Nutrient.fromMvtName("VANN"));
    }

    @Test
    void returnsNullForUnknownHeader() {
        assertNull(Nutrient.fromMvtName("Spiselig del"));
        assertNull(Nutrient.fromMvtName(null));
    }

    @Test
    void mapsSaturatedFat() {
        assertEquals(Nutrient.SATURATED_FAT, Nutrient.fromMvtName("Mettet"));
    }

    @Test
    void hasUniqueMvtNames() {
        assertEquals(Nutrient.values().length, Arrays.stream(Nutrient.values()).map(Nutrient::getMvtName).distinct().count());
    }

    @Test
    void vitaminB12IsMeasuredInMicrograms() {
        assertEquals(MvtUnit.MICROGRAMS, Nutrient.VITAMIN_B12.getUnit());
    }
}
