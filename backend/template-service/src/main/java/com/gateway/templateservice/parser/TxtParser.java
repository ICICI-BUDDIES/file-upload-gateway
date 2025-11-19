package com.gateway.templateservice.parser;

import java.io.*;
import java.util.*;

public class TxtParser {
    public static List<Map<String, String>> parse(InputStream is) throws IOException {
        List<Map<String, String>> out = new ArrayList<>();
        BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8"));
        String header = br.readLine();
        if (header == null)
            return out;
        String[] headers = header.split(",");
        String line;
        while ((line = br.readLine()) != null) {
            String[] cols = line.split(",");
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
