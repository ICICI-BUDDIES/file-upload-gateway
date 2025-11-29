package com.gateway.template.parser;

import com.opencsv.CSVReader;

import java.io.*;
import java.util.*;

public class CSVParser {
    public static List<Map<String, String>> parse(InputStream is) throws IOException {
        List<Map<String, String>> out = new ArrayList<>();
        BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8"));
        String header = br.readLine();
        if (header == null)
            return out;

        String[] headers;
        try {
            CSVReader reader = new CSVReader(new StringReader(header));
            headers = reader.readNext();
            reader.close();
        } catch (Exception e) {
            headers = header.split(",");
        }

        String line;
        while ((line = br.readLine()) != null) {
            String[] cols;
            try {
                CSVReader r2 = new CSVReader(new StringReader(line));
                cols = r2.readNext();
                r2.close();
            } catch (Exception e) {
                cols = line.split(",");
            }
            Map<String, String> row = new LinkedHashMap<>();
            for (int i = 0; i < headers.length; i++) {
                String key = headers[i].trim();
                String val = i < cols.length ? cols[i] : "";
                row.put(key, val);
            }
            out.add(row);
        }
        return out;
    }
}
