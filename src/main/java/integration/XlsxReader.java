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

import model.Nutrient;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Reading mvt data from xlsx (which is currently the way it is distributed).
 */
public class XlsxReader {
    public static final Pattern PRODUCT_PATTERN = Pattern.compile("^\\d+(?!\\.)");
    public static final Pattern PRODUCT_GROUP_PATTERN = Pattern.compile("^\\d+\\.\\d{1,2}(?!\\.)");
    public static final Pattern PRODUCT_SUB_GROUP_PATTERN = Pattern.compile("^\\d+\\.\\d+\\.\\d+(?!\\.)");
    final Logger logger = LoggerFactory.getLogger(XlsxReader.class);

    private XSSFSheet mvtSheet;

    public XlsxReader(InputStream xlsxInput) {
        XSSFWorkbook workbook;
        try {
            workbook = new XSSFWorkbook(xlsxInput);
        } catch (IOException e) {
            throw new XlsxReaderException("Could not open input stream " + xlsxInput.toString(), e);
        }
        mvtSheet = workbook.getSheetAt(0);
        Map<Integer, Nutrient> columnMap;

        Iterator<Row> rowIterator = mvtSheet.rowIterator();
        while (rowIterator.hasNext()) {
            Row row = rowIterator.next();
            StringBuilder line = new StringBuilder();
            if (row.getCell(0, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK).toString().equals("MatvareID")) {
                columnMap = findNutrientColumns(row);
                break;
            }
        }
        while (rowIterator.hasNext()) {
            Row row = rowIterator.next();
            //System.out.println("Row no " + row.getRowNum() + " - " + row.getCell(0));
            if(isProduct(row)) {
                System.out.println("Row no " + row.getRowNum() + " - " + row.getCell(0) + " PRODUCT: " + row.getCell(1));
            }
            else if(isProductGroup(row)) {
                System.out.println("Row no " + row.getRowNum() + " - " + row.getCell(0) + " GROUP: " + row.getCell(1));
            }
            else if(isProductSubGroup(row)) {
                System.out.println("Row no " + row.getRowNum() + " - " + row.getCell(0) + " SUBGROUP: " + row.getCell(1));
            }
            /*
            Iterator<Cell> cellIterator = row.cellIterator();
            while (cellIterator.hasNext()) {
                line.append(cellIterator.next().toString()).append(" / ");
            }
            System.out.println(line.toString());
            */
        }
    }

    private boolean isProduct(Row row) {
        Cell cell = row.getCell(0, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
        cell.setCellType(Cell.CELL_TYPE_STRING);
        Matcher matcher = PRODUCT_PATTERN.matcher(cell.toString());
        return matcher.matches();
    }

    private boolean isProductGroup(Row row) {
        Cell cell = row.getCell(0, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
        cell.setCellType(Cell.CELL_TYPE_STRING);
        Matcher matcher = PRODUCT_GROUP_PATTERN.matcher(cell.toString());
        return matcher.matches();
    }

    private boolean isProductSubGroup(Row row) {
        Cell cell = row.getCell(0, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
        cell.setCellType(Cell.CELL_TYPE_STRING);
        Matcher matcher = PRODUCT_SUB_GROUP_PATTERN.matcher(cell.toString());
        return matcher.matches();
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
        public XlsxReaderException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
