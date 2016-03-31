/*
 * Copyright 2016 Frode Torvund
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

package model;

import java.util.Objects;

/**
 * Structure:
 * {@link Category}
 *  |-  {@link ProductCategory}
 *      |-  {@link ProductGroup}
 *          |-  {@link ProductSubGroup}
 *              |-  {@link FoodItem}
 *
 *  E.g.: [DAIRY] - [Melk og melkeprodukter] - [Ost] - [Ost, ekstra fet] - [Ridderost]
 */
public class ProductGroup {
    private ProductCategory productCategory;
    private String name;

    public ProductGroup(ProductCategory productCategory, String name) {
        this.productCategory = productCategory;
        this.name = name;
    }

    public ProductCategory getProductCategory() {
        return productCategory;
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ProductGroup)) return false;
        ProductGroup that = (ProductGroup) o;
        return getProductCategory() == that.getProductCategory() &&
                Objects.equals(getName(), that.getName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getProductCategory(), getName());
    }
}
