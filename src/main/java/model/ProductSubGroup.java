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

/**
 *
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

    public void setProductGroup(ProductGroup productGroup) {
        this.productGroup = productGroup;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ProductSubGroup that = (ProductSubGroup) o;

        if (productGroup != that.productGroup) return false;
        return name.equals(that.name);

    }

    @Override
    public int hashCode() {
        int result = productGroup.hashCode();
        result = 31 * result + name.hashCode();
        return result;
    }
}
