package com.gateway.templateservice.parser;

import org.apache.poi.ss.usermodel.*;

import java.io.*;
import java.util.*;

public class ExcelParser {
    public static List<Map<String, String>> parse(InputStream is) throws IOException {
        List<Map<String, String>> out = new ArrayList<>();
        
        try {
            System.out.println("📊 ExcelParser: Starting to parse Excel file (.xls/.xlsx)");
            
            // WorkbookFactory.create() automatically detects .xls vs .xlsx format
            Workbook wb = WorkbookFactory.create(is);
            System.out.println("📊 ExcelParser: Workbook created successfully - Type: " + wb.getClass().getSimpleName());
            
            Sheet sheet = wb.getSheetAt(0);
            System.out.println("📊 ExcelParser: Got first sheet: " + sheet.getSheetName());
            
            Iterator<Row> iter = sheet.iterator();
            if (!iter.hasNext()) {
                System.out.println("⚠️ ExcelParser: No rows found in sheet");
                return out;
            }
            
            Row headerRow = iter.next();
            List<String> headers = new ArrayList<>();
            for (int i = 0; i < headerRow.getLastCellNum(); i++) {
                Cell c = headerRow.getCell(i);
                String header = "";
                if (c != null) {
                    if (c.getCellType() == CellType.STRING) {
                        header = c.getStringCellValue();
                    } else {
                        header = c.toString();
                    }
                }
                headers.add(header);
            }
            System.out.println("📊 ExcelParser: Headers found: " + headers);
            
            while (iter.hasNext()) {
                Row r = iter.next();
                Map<String, String> row = new LinkedHashMap<>();
                for (int i = 0; i < headers.size(); i++) {
                    Cell c = r.getCell(i);
                    String cellValue = "";
                    if (c != null) {
                        switch (c.getCellType()) {
                            case NUMERIC:
                                // Check if it's a whole number
                                double numValue = c.getNumericCellValue();
                                if (numValue == Math.floor(numValue)) {
                                    cellValue = String.valueOf((long) numValue);
                                } else {
                                    cellValue = String.valueOf(numValue);
                                }
                                break;
                            case STRING:
                                cellValue = c.getStringCellValue();
                                break;
                            case BOOLEAN:
                                cellValue = String.valueOf(c.getBooleanCellValue());
                                break;
                            case FORMULA:
                                cellValue = c.getCellFormula();
                                break;
                            default:
                                cellValue = c.toString();
                        }
                    }
                    row.put(headers.get(i), cellValue);
                }
                out.add(row);
            }
            
            System.out.println("📊 ExcelParser: Successfully parsed " + out.size() + " rows");
            wb.close();
            
        } catch (Exception e) {
            System.err.println("❌ ExcelParser Error: " + e.getMessage());
            e.printStackTrace();
            throw new IOException("Failed to parse Excel file: " + e.getMessage(), e);
        }
        
        return out;
    }
}
