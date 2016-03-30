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

import com.google.common.base.Strings;
import model.*;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Reading mvt data from xlsx (which is currently the way it is distributed).
 */
public class XlsxReader {
    private static final Pattern PRODUCT_PATTERN = Pattern.compile("^\\d+(?!\\.)");
    private static final Pattern PRODUCT_GROUP_PATTERN = Pattern.compile("^\\d+\\.\\d{1,2}(?!\\.)");
    private static final Pattern PRODUCT_SUB_GROUP_PATTERN = Pattern.compile("^\\d+\\.\\d+\\.\\d+(?!\\.)");
    private final Logger logger = LoggerFactory.getLogger(XlsxReader.class);

    private XSSFWorkbook workbook;

    public XlsxReader(InputStream xlsxInput) {
        try {
            workbook = new XSSFWorkbook(xlsxInput);
        } catch (IOException e) {
            throw new XlsxReaderException("Could not open input stream " + xlsxInput.toString(), e);
        }
    }

    public List<FoodItem> read() {
        XSSFSheet mvtSheet = workbook.getSheetAt(0);
        Map<Integer, Nutrient> nutrientColumnMap = null;

        Iterator<Row> rowIterator = mvtSheet.rowIterator();
        while (rowIterator.hasNext()) {
            Row row = rowIterator.next();
            if (row.getCell(0, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK).toString().equals("MatvareID")) {
                nutrientColumnMap = findNutrientColumns(row);
                break;
            }
        }
        if(nutrientColumnMap == null) {
            throw new XlsxReaderException("Failed to populate nutrient column map");
        }

        ProductCategory currentProductCategory = null;
        ProductGroup currentProductGroup = null;
        ProductSubGroup currentProductSubGroup = null;
        List<FoodItem> foodItems = new ArrayList<>();
        int incrementalId = 0;
        while (rowIterator.hasNext()) {
            Row row = rowIterator.next();

            if(isProductCategory(row)) {
                logger.info("Row no " + row.getRowNum() + " - " + row.getCell(0) + " PRODUCT: " + row.getCell(1));
                currentProductCategory = ProductCategory.fromMvtId(getMvtId(row));
                continue;
            }
            if(isProductGroup(row)) {
                logger.info("Row no " + row.getRowNum() + " - " + row.getCell(0) + " GROUP: " + row.getCell(1));
                currentProductGroup = new ProductGroup(currentProductCategory, getCellAsString(row, 1));
                continue;
            }
            if(isProductSubGroup(row)) {
                logger.info("Row no " + row.getRowNum() + " - " + row.getCell(0) + " SUBGROUP: " + row.getCell(1));
                currentProductSubGroup = new ProductSubGroup(currentProductGroup, getCellAsString(row, 1));
                continue;
            }
            if(Strings.isNullOrEmpty(getCellAsString(row, 0))) {
                continue;
            }
            String matvareName = getCellAsString(row, 1);
            Map<Nutrient, Double> nutrientMap = new HashMap<>();
            nutrientColumnMap.forEach((k, v) -> nutrientMap.put(v, getCellAsDouble(row, k)));
            foodItems.add(new FoodItem(++incrementalId, matvareName, currentProductSubGroup, nutrientMap));
        }
        logger.info("Antall matvarer funnet: " + foodItems.size());
        return foodItems;
    }

    private Integer getMvtId(Row row) {
        return Integer.parseInt(getCellAsString(row, 0));
    }

    private boolean isProductCategory(Row row) {
        Matcher matcher = PRODUCT_PATTERN.matcher(getCellAsString(row, 0));
        return matcher.matches();
    }

    private boolean isProductGroup(Row row) {
        Matcher matcher = PRODUCT_GROUP_PATTERN.matcher(getCellAsString(row, 0));
        return matcher.matches();
    }

    private boolean isProductSubGroup(Row row) {
        Matcher matcher = PRODUCT_SUB_GROUP_PATTERN.matcher(getCellAsString(row, 0));
        return matcher.matches();
    }

    private String getCellAsString(Row row, int rowNum) {
        Cell cell = row.getCell(rowNum, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
        cell.setCellType(Cell.CELL_TYPE_STRING);
        return cell.toString();
    }

    private Double getCellAsDouble(Row row, int rowNum) {
        Cell cell = row.getCell(rowNum, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
        cell.setCellType(Cell.CELL_TYPE_NUMERIC);
        return cell.getNumericCellValue();
    }

    private Map<Integer, Nutrient> findNutrientColumns(Row row) {
        Iterator<Cell> cellIterator = row.cellIterator();
        Map<Integer, Nutrient> columnMap = new HashMap<>();
        while (cellIterator.hasNext()) {
            Cell cell = cellIterator.next();
            Nutrient nutrient = Nutrient.fromMvtName(cell.toString());
            if(nutrient != null) {
                columnMap.put(cell.getColumnIndex(), nutrient);
            }
        }
        return columnMap;
    }

    public static class XlsxReaderException extends RuntimeException {
        XlsxReaderException(String message, Throwable cause) {
            super(message, cause);
        }

        XlsxReaderException(String message) {
            super(message);
        }
    }
}
