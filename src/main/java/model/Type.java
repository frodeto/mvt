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
public enum Type {

    PORK("Svinekjøtt", Category.MEAT_OR_FISH),
    BEEF("Storfe", Category.MEAT_OR_FISH),
    CHICKEN("Kylling", Category.MEAT_OR_FISH),
    OTHER_MEAT("Annet kjøtt", Category.MEAT_OR_FISH),
    MIXED("Blandet", Category.MIXED),
    OTHER("Annet", Category.OTHER);

    private String navn;

    private Category category;

    public String getNavn() {
        return navn;
    }

    public Category getCategory() {
        return category;
    }

    Type(String navn, Category category) {
        this.navn = navn;
        this.category = category;
    }
}
