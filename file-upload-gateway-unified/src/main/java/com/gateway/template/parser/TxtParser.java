package com.gateway.template.parser;

import java.io.*;
import java.util.*;

public class TxtParser {
    public static List<Map<String, String>> parse(InputStream is) throws IOException {
        List<Map<String, String>> out = new ArrayList<>();
        BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8"));
        
        String firstLine = br.readLine();
        if (firstLine == null) return out;
        
        // Auto-detect delimiter
        char delimiter = detectDelimiter(firstLine);
        
        // Parse headers from first line
        String delimiterRegex = delimiter == '|' ? "\\|" : String.valueOf(delimiter);
        String[] headers = firstLine.split(delimiterRegex, -1);
        for (int i = 0; i < headers.length; i++) {
            headers[i] = headers[i].trim();
        }
        
        // Process data lines
        String line;
        while ((line = br.readLine()) != null) {
            if (line.trim().isEmpty()) continue;
            
            String[] cols = line.split(delimiterRegex, -1);
            Map<String, String> row = new LinkedHashMap<>();
            for (int i = 0; i < headers.length; i++) {
                String key = headers[i];
                String val = i < cols.length ? cols[i].trim() : "";
                row.put(key, val);
            }
            out.add(row);
        }
        
        return out;
    }
    
    private static char detectDelimiter(String line) {
        // Count occurrences of common delimiters
        int commaCount = (int) line.chars().filter(ch -> ch == ',').count();
        int pipeCount = (int) line.chars().filter(ch -> ch == '|').count();
        int tabCount = (int) line.chars().filter(ch -> ch == '\t').count();
        int semicolonCount = (int) line.chars().filter(ch -> ch == ';').count();
        
        // Return the delimiter with highest count
        if (pipeCount > 0 && pipeCount >= commaCount && pipeCount >= tabCount && pipeCount >= semicolonCount) {
            return '|';
        } else if (tabCount > 0 && tabCount >= commaCount && tabCount >= semicolonCount) {
            return '\t';
        } else if (semicolonCount > 0 && semicolonCount >= commaCount) {
            return ';';
        } else {
            return ','; // Default to comma
        }
    }
}
