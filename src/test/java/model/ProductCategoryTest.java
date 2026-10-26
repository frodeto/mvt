package model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ProductCategoryTest {

    @Test
    void findsCategoryFromMvtId() {
        assertEquals(ProductCategory.DAIRY, ProductCategory.fromMvtId(1));
        assertEquals(ProductCategory.INFANT_FOODS, ProductCategory.fromMvtId(11));
    }

    @Test
    void rejectsUnknownMvtId() {
        assertThrows(IllegalArgumentException.class, () -> ProductCategory.fromMvtId(12));
    }
}
