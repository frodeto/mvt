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

package no.torvundconsulting.mvt.integration;

import no.torvundconsulting.mvt.model.DataSource;
import no.torvundconsulting.mvt.model.FoodItem;
import no.torvundconsulting.mvt.model.Nutrient;
import no.torvundconsulting.mvt.model.ProductCategory;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Reading mvt data from xlsx (which is currently the way it is distributed).
 * <p>
 * Reads the sheet {@value #SHEET_NAME} of Matvaretabellen 2026. The first row holds the column headers, e.g.
 * "Jern (Fe) (mg)". The other rows are either a category, with only a name like "Meieriprodukter" in the first
 * column, or a food item, identified by an id like {@code 01.344}. All values are stored as text, and an empty value
 * means the amount is unknown; such nutrients are left out of the food item's nutrient map.
 */
public class MvtXlsxReader {
    static final String SHEET_NAME = "Matvarer (alle næringsstoffer)";
    private static final String ID_HEADER = "Matvare ID";
    private static final String NAME_HEADER = "Matvare";
    private static final Pattern FOOD_ITEM_ID_PATTERN = Pattern.compile("\\d{2}\\.\\d{3}");
    private static final Logger logger = LoggerFactory.getLogger(MvtXlsxReader.class);

    private final InputStream xlsxInput;

    public MvtXlsxReader(InputStream xlsxInput) {
        this.xlsxInput = xlsxInput;
    }

    public List<FoodItem> read() {
        try (XSSFWorkbook workbook = new XSSFWorkbook(xlsxInput)) {
            Sheet sheet = workbook.getSheet(SHEET_NAME);
            if (sheet == null) {
                throw new XlsxReaderException("Could not find sheet '" + SHEET_NAME + "'");
            }
            return read(sheet);
        } catch (IOException e) {
            throw new XlsxReaderException("Could not read xlsx input", e);
        }
    }

    private List<FoodItem> read(Sheet sheet) {
        Iterator<Row> rowIterator = sheet.rowIterator();
        if (!rowIterator.hasNext()) {
            throw new XlsxReaderException("Sheet '" + SHEET_NAME + "' is empty");
        }
        Map<Nutrient, Integer> nutrientColumns = findNutrientColumns(rowIterator.next());

        ProductCategory currentProductCategory = null;
        List<FoodItem> foodItems = new ArrayList<>();
        int incrementalId = 0;
        while (rowIterator.hasNext()) {
            Row row = rowIterator.next();
            String code = getCellAsString(row, 0);

            if (FOOD_ITEM_ID_PATTERN.matcher(code).matches()) {
                if (currentProductCategory == null) {
                    throw new XlsxReaderException("Food item " + code + " in row " + (row.getRowNum() + 1)
                            + " comes before any category");
                }
                foodItems.add(new FoodItem(++incrementalId, code, getCellAsString(row, 1), currentProductCategory,
                        readNutrients(row, nutrientColumns), DataSource.MVT));
            } else if (!code.isEmpty()) {
                logger.debug("Row {}: category {}", row.getRowNum() + 1, code);
                try {
                    currentProductCategory = ProductCategory.fromMvtName(code);
                } catch (IllegalArgumentException e) {
                    throw new XlsxReaderException("Unknown category '" + code + "' in row " + (row.getRowNum() + 1), e);
                }
            }
        }
        logger.info("Antall matvarer funnet: {}", foodItems.size());
        return foodItems;
    }

    private static Map<Nutrient, Integer> findNutrientColumns(Row headerRow) {
        if (!ID_HEADER.equals(getCellAsString(headerRow, 0)) || !NAME_HEADER.equals(getCellAsString(headerRow, 1))) {
            throw new XlsxReaderException("Expected the first row to start with '" + ID_HEADER + "' and '"
                    + NAME_HEADER + "'");
        }
        Map<String, Integer> columnsByHeader = new HashMap<>();
        for (Cell cell : headerRow) {
            columnsByHeader.put(cell.toString().strip(), cell.getColumnIndex());
        }

        Map<Nutrient, Integer> nutrientColumns = new EnumMap<>(Nutrient.class);
        List<String> missing = new ArrayList<>();
        for (Nutrient nutrient : Nutrient.values()) {
            Integer column = columnsByHeader.get(nutrient.getColumnName());
            if (column == null) {
                missing.add(nutrient.getColumnName());
            } else {
                nutrientColumns.put(nutrient, column);
            }
        }
        if (!missing.isEmpty()) {
            throw new XlsxReaderException("Missing nutrient columns: " + missing);
        }
        return nutrientColumns;
    }

    private static Map<Nutrient, Double> readNutrients(Row row, Map<Nutrient, Integer> nutrientColumns) {
        Map<Nutrient, Double> nutrientMap = new EnumMap<>(Nutrient.class);
        nutrientColumns.forEach((nutrient, column) -> {
            String value = getCellAsString(row, column);
            if (!value.isEmpty()) {
                try {
                    nutrientMap.put(nutrient, Double.parseDouble(value));
                } catch (NumberFormatException e) {
                    throw new XlsxReaderException("Invalid value '" + value + "' for " + nutrient.getColumnName()
                            + " in row " + (row.getRowNum() + 1), e);
                }
            }
        });
        return nutrientMap;
    }

    /**
     * The cell's text, without surrounding whitespace. Numbers are written without a decimal part when they are whole.
     */
    private static String getCellAsString(Row row, int column) {
        Cell cell = row.getCell(column);
        if (cell == null) {
            return "";
        }
        if (cell.getCellType() == CellType.NUMERIC) {
            double value = cell.getNumericCellValue();
            return value == Math.rint(value) ? String.valueOf((long) value) : String.valueOf(value);
        }
        return cell.toString().strip();
    }

    static class XlsxReaderException extends RuntimeException {
        XlsxReaderException(String message, Throwable cause) {
            super(message, cause);
        }

        XlsxReaderException(String message) {
            super(message);
        }
    }
}
