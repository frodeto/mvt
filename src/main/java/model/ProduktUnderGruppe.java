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
public class ProduktUnderGruppe {
    private ProduktGruppe produktGruppe;
    private String name;

    public ProduktUnderGruppe(ProduktGruppe produktGruppe, String name) {
        this.produktGruppe = produktGruppe;
        this.name = name;
    }

    public ProduktGruppe getProduktGruppe() {
        return produktGruppe;
    }

    public void setProduktGruppe(ProduktGruppe produktGruppe) {
        this.produktGruppe = produktGruppe;
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

        ProduktUnderGruppe that = (ProduktUnderGruppe) o;

        if (produktGruppe != that.produktGruppe) return false;
        return name.equals(that.name);

    }

    @Override
    public int hashCode() {
        int result = produktGruppe.hashCode();
        result = 31 * result + name.hashCode();
        return result;
    }
}
