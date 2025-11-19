package com.example.gateway.service;

import com.example.gateway.dto.ExtractedFile;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.CSVParserBuilder;
import com.opencsv.exceptions.CsvValidationException;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.*;

@Service
public class ExtractionService {

    /**
     * Extracts headers + rows from file
     */
    public ExtractedFile extractWithHeaders(MultipartFile file, String format, Character delimiter) throws IOException {

        String fmt = (format != null ? format.toLowerCase() : detectFormat(file));

        switch (fmt) {
            case "csv":
                return extractCsv(file.getInputStream(), ',');

            case "pipe":
                return extractCsv(file.getInputStream(), delimiter != null ? delimiter : '|');

            case "txt":
                return extractTxt(file.getInputStream(), delimiter);

            case "xlsx":
            case "xls":
                return extractExcel(file.getInputStream());

            default:
                return extractCsv(file.getInputStream(), ',');
        }
    }

    private String detectFormat(MultipartFile file) {
        if (file.getOriginalFilename() == null) return "csv";

        String name = file.getOriginalFilename().toLowerCase();

        if (name.endsWith(".csv")) return "csv";
        if (name.endsWith(".txt")) return "txt";
        if (name.endsWith(".xlsx") || name.endsWith(".xls")) return "xlsx";

        return "csv";
    }

    // ===========================================================
    //  CSV / PIPE HANDLING (OpenCSV 5.x)
    // ===========================================================
    private ExtractedFile extractCsv(InputStream is, char delimiter) throws IOException {

        List<String> headers = new ArrayList<>();
        List<Map<String, Object>> rows = new ArrayList<>();

        try (
                InputStreamReader isr = new InputStreamReader(is, "UTF-8");
                CSVReader reader = new CSVReaderBuilder(isr)
                        .withCSVParser(new CSVParserBuilder().withSeparator(delimiter).build())
                        .build()
        ) {
            String[] headerLine = reader.readNext();

            if (headerLine == null) return emptyResult();

            headers = Arrays.asList(headerLine);

            String[] line;
            while ((line = reader.readNext()) != null) {
                Map<String, Object> row = new LinkedHashMap<>();

                for (int i = 0; i < headers.size(); i++) {
                    row.put(headers.get(i), i < line.length ? line[i] : null);
                }

                rows.add(row);
            }

        } catch (CsvValidationException e) {
            throw new IOException("CSV parsing failed: " + e.getMessage(), e);
        }

        ExtractedFile ef = new ExtractedFile();
        ef.setHeaders(headers);
        ef.setRows(rows);
        return ef;
    }

    // ===========================================================
    //  TXT HANDLING
    // ===========================================================
    private ExtractedFile extractTxt(InputStream is, Character delimiter) throws IOException {

        List<String> headers = new ArrayList<>();
        List<Map<String, Object>> rows = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8"))) {

            String headerLine = br.readLine();
            if (headerLine == null) return emptyResult();

            if (delimiter != null) {
                headers = Arrays.asList(headerLine.split(Character.toString(delimiter)));
            } else {
                // Auto-detect delimiter
                if (headerLine.contains("|")) {
                    delimiter = '|';
                } else if (headerLine.contains(",")) {
                    delimiter = ',';
                } else {
                    delimiter = null;
                }

                if (delimiter != null) {
                    headers = Arrays.asList(headerLine.split(Character.toString(delimiter)));
                } else {
                    headers = Collections.singletonList(headerLine.trim());
                }
            }

            String line;
            while ((line = br.readLine()) != null) {

                String[] parts = delimiter != null
                        ? line.split(Character.toString(delimiter))
                        : new String[]{ line.trim() };

                Map<String, Object> row = new LinkedHashMap<>();

                for (int i = 0; i < headers.size(); i++) {
                    row.put(headers.get(i), i < parts.length ? parts[i] : null);
                }

                rows.add(row);
            }
        }

        ExtractedFile ef = new ExtractedFile();
        ef.setHeaders(headers);
        ef.setRows(rows);
        return ef;
    }

    // ===========================================================
    //  EXCEL HANDLING (.xlsx)
    // ===========================================================
    private ExtractedFile extractExcel(InputStream is) throws IOException {

        List<String> headers = new ArrayList<>();
        List<Map<String, Object>> rows = new ArrayList<>();

        Workbook workbook = new XSSFWorkbook(is);

        try {
            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> iterator = sheet.iterator();

            if (!iterator.hasNext()) return emptyResult();

            // Header Row
            Row headerRow = iterator.next();
            for (Cell cell : headerRow) {
                headers.add(cellToString(cell));
            }

            // Data Rows
            while (iterator.hasNext()) {

                Row row = iterator.next();
                Map<String, Object> rowMap = new LinkedHashMap<>();

                for (int i = 0; i < headers.size(); i++) {
                    Cell c = row.getCell(i);
                    rowMap.put(headers.get(i), c == null ? null : cellToString(c));
                }

                rows.add(rowMap);
            }

        } finally {
            workbook.close();
        }

        ExtractedFile ef = new ExtractedFile();
        ef.setHeaders(headers);
        ef.setRows(rows);
        return ef;
    }

    private String cellToString(Cell cell) {
        if (cell == null) return null;

        switch (cell.getCellType()) {

            case STRING:
                return cell.getStringCellValue();

            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                } else {
                    double d = cell.getNumericCellValue();
                    if (d == (long) d) return String.valueOf((long) d);
                    return String.valueOf(d);
                }

            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());

            case FORMULA:
                try {
                    return cell.getStringCellValue();
                } catch (IllegalStateException ex) {
                    return String.valueOf(cell.getNumericCellValue());
                }

            default:
                return "";
        }
    }

    private ExtractedFile emptyResult() {
        ExtractedFile ef = new ExtractedFile();
        ef.setHeaders(new ArrayList<>());
        ef.setRows(new ArrayList<>());
        return ef;
    }
}
