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

package integration;

import model.DataSource;
import model.FoodItem;
import model.Nutrient;
import model.ProductCategory;
import model.ProductGroup;
import model.ProductSubGroup;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
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
 * Rows are either a grouping level, identified by a code like {@code 1}, {@code 1.4} or {@code 1.4.2}, or a food
 * item, identified by an id like {@code 01.001}. Nutrient values marked {@code M} (missing) are left out of the
 * food item's nutrient map.
 */
public class MvtXlsxReader {
    private static final Pattern FOOD_ITEM_ID_PATTERN = Pattern.compile("\\d{2}\\.\\d{3}");
    private static final Pattern GROUP_CODE_PATTERN = Pattern.compile("[1-9]\\d*(\\.[1-9]\\d*)*");
    private static final String HEADER_ID = "MatvareID";
    private static final String DEFAULT_GROUP_NAME = "Default";
    private static final Logger logger = LoggerFactory.getLogger(MvtXlsxReader.class);

    private final InputStream xlsxInput;

    public MvtXlsxReader(InputStream xlsxInput) {
        this.xlsxInput = xlsxInput;
    }

    public List<FoodItem> read() {
        try (XSSFWorkbook workbook = new XSSFWorkbook(xlsxInput)) {
            return read(workbook.getSheetAt(0));
        } catch (IOException e) {
            throw new XlsxReaderException("Could not read xlsx input", e);
        }
    }

    private List<FoodItem> read(XSSFSheet mvtSheet) {
        Iterator<Row> rowIterator = mvtSheet.rowIterator();
        Map<Integer, Nutrient> nutrientColumnMap = findNutrientColumns(rowIterator);

        ProductCategory currentProductCategory = null;
        ProductGroup currentProductGroup = null;
        ProductSubGroup currentProductSubGroup = null;
        List<FoodItem> foodItems = new ArrayList<>();
        int incrementalId = 0;
        while (rowIterator.hasNext()) {
            Row row = rowIterator.next();
            String code = getCellAsString(row, 0);
            String name = getCellAsString(row, 1);

            if (FOOD_ITEM_ID_PATTERN.matcher(code).matches()) {
                if (currentProductGroup == null) {
                    currentProductGroup = new ProductGroup(currentProductCategory, DEFAULT_GROUP_NAME);
                }
                if (currentProductSubGroup == null) {
                    currentProductSubGroup = new ProductSubGroup(currentProductGroup, DEFAULT_GROUP_NAME);
                }
                foodItems.add(new FoodItem(++incrementalId, code, name, currentProductSubGroup,
                        readNutrients(row, nutrientColumnMap), DataSource.MVT));
            } else if (GROUP_CODE_PATTERN.matcher(code).matches()) {
                int level = code.split("\\.").length;
                logger.debug("Row {}: level {} group {} {}", row.getRowNum(), level, code, name);
                if (level == 1) {
                    currentProductCategory = ProductCategory.fromMvtId(Integer.parseInt(code));
                    currentProductGroup = null;
                    currentProductSubGroup = null;
                } else if (level == 2) {
                    currentProductGroup = new ProductGroup(currentProductCategory, name);
                    currentProductSubGroup = null;
                } else {
                    // Level 3 and deeper: the deepest level (e.g. 4.1.1.1 "Fet fisk, rå") is the most specific
                    currentProductSubGroup = new ProductSubGroup(currentProductGroup, name);
                }
            } else if (!code.isEmpty()) {
                logger.debug("Row {}: skipping '{}'", row.getRowNum(), code);
            }
        }
        logger.info("Antall matvarer funnet: {}", foodItems.size());
        return foodItems;
    }

    private Map<Integer, Nutrient> findNutrientColumns(Iterator<Row> rowIterator) {
        while (rowIterator.hasNext()) {
            Row row = rowIterator.next();
            if (HEADER_ID.equals(getCellAsString(row, 0))) {
                Map<Integer, Nutrient> columnMap = new HashMap<>();
                for (Cell cell : row) {
                    Nutrient nutrient = Nutrient.fromMvtName(cell.toString().strip());
                    if (nutrient != null) {
                        columnMap.put(cell.getColumnIndex(), nutrient);
                    }
                }
                return columnMap;
            }
        }
        throw new XlsxReaderException("Could not find header row starting with " + HEADER_ID);
    }

    private static Map<Nutrient, Double> readNutrients(Row row, Map<Integer, Nutrient> nutrientColumnMap) {
        Map<Nutrient, Double> nutrientMap = new EnumMap<>(Nutrient.class);
        nutrientColumnMap.forEach((column, nutrient) -> {
            Cell cell = row.getCell(column);
            if (cell != null && cell.getCellType() == CellType.NUMERIC) {
                nutrientMap.put(nutrient, cell.getNumericCellValue());
            }
        });
        return nutrientMap;
    }

    /**
     * The cell's text. Whole numbers are written without decimals, since some group codes are stored as numbers.
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

    private static class XlsxReaderException extends RuntimeException {
        XlsxReaderException(String message, Throwable cause) {
            super(message, cause);
        }

        XlsxReaderException(String message) {
            super(message);
        }
    }
}
