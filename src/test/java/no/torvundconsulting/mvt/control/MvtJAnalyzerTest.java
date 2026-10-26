package no.torvundconsulting.mvt.control;

import no.torvundconsulting.mvt.integration.FoodItemReference;
import no.torvundconsulting.mvt.model.DataSource;
import no.torvundconsulting.mvt.model.FoodItem;
import no.torvundconsulting.mvt.model.Nutrient;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class MvtJAnalyzerTest {
    private final FoodItem milk = new FoodItem(1, "01.001", "Milk", null, Map.of(Nutrient.PROTEIN, 3.4, Nutrient.FAT, 3.5), DataSource.MVT);
    private final FoodItem egg = new FoodItem(2, "02.001", "Egg", null, Map.of(Nutrient.PROTEIN, 12.4), DataSource.MVT);
    private final MvtJAnalyzer analyzer = new MvtJAnalyzer(List.of(milk, egg));

    @Test
    void findsItemsAtOrAboveLevel() {
        assertEquals(List.of(new FoodItemReference(2, "Egg", DataSource.MVT)),
                analyzer.getAboveLevel(Nutrient.PROTEIN, 12.4));
        assertEquals(2, analyzer.getAboveLevel(Nutrient.PROTEIN, 0.0).size());
    }

    @Test
    void findsNothingAboveHighestLevel() {
        assertTrue(analyzer.getAboveLevel(Nutrient.PROTEIN, 100.0).isEmpty());
    }

    @Test
    void skipsItemsWithoutNutrient() {
        assertEquals(List.of(new FoodItemReference(1, "Milk", DataSource.MVT)),
                analyzer.getAboveLevel(Nutrient.FAT, 1.0));
    }
}
