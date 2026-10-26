package no.torvundconsulting.mvt.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ProductCategoryTest {

    @Test
    void findsCategoryFromMvtName() {
        assertEquals(ProductCategory.DAIRY, ProductCategory.fromMvtName("Meieriprodukter"));
        assertEquals(ProductCategory.FATS_AND_OILS, ProductCategory.fromMvtName("Spisefett"));
    }

    @Test
    void mapsToCoarseCategory() {
        assertEquals(Category.MEAT_OR_FISH, ProductCategory.FISH_AND_SHELLFISH.getCategory());
        assertEquals(Category.VEGETABLE, ProductCategory.LEGUMES.getCategory());
    }

    @Test
    void rejectsUnknownMvtName() {
        assertThrows(IllegalArgumentException.class, () -> ProductCategory.fromMvtName("Melk og melkeprodukter"));
    }
}
