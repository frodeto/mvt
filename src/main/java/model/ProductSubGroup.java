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
public class ProductSubGroup {
    private ProductGroup productGroup;
    private String name;

    public ProductSubGroup(ProductGroup productGroup, String name) {
        this.productGroup = productGroup;
        this.name = name;
    }

    public ProductGroup getProductGroup() {
        return productGroup;
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ProductSubGroup)) return false;
        ProductSubGroup that = (ProductSubGroup) o;
        return Objects.equals(getProductGroup(), that.getProductGroup()) &&
                Objects.equals(getName(), that.getName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getProductGroup(), getName());
    }
}
