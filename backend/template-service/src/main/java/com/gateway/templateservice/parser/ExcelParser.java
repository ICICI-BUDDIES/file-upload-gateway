package com.gateway.templateservice.parser;

import org.apache.poi.ss.usermodel.*;

import java.io.*;
import java.util.*;

public class ExcelParser {
    public static List<Map<String, String>> parse(InputStream is) throws IOException {
        List<Map<String, String>> out = new ArrayList<>();
        Workbook wb = WorkbookFactory.create(is);
        Sheet sheet = wb.getSheetAt(0);
        Iterator<Row> iter = sheet.iterator();
        if (!iter.hasNext())
            return out;
        Row headerRow = iter.next();
        List<String> headers = new ArrayList<>();
        for (int i = 0; i < headerRow.getLastCellNum(); i++) {
            Cell c = headerRow.getCell(i);
            headers.add(c == null ? "" : c.toString());
        }
        while (iter.hasNext()) {
            Row r = iter.next();
            Map<String, String> row = new LinkedHashMap<>();
            for (int i = 0; i < headers.size(); i++) {
                Cell c = r.getCell(i);
                row.put(headers.get(i), c == null ? "" : c.toString());
            }
            out.add(row);
        }
        return out;
    }
}
