package com.gateway.template.parser;

import org.junit.jupiter.api.Test;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class CSVParserTest {

    @Test
    void testParseValidCSV() throws Exception {
        String csvData = "Name,Age,Email\nJohn,30,john@example.com\nJane,25,jane@example.com";
        InputStream is = new ByteArrayInputStream(csvData.getBytes());
        
        List<Map<String, String>> result = CSVParser.parse(is);
        
        assertEquals(2, result.size());
        assertEquals("John", result.get(0).get("Name"));
        assertEquals("30", result.get(0).get("Age"));
        assertEquals("john@example.com", result.get(0).get("Email"));
    }

    @Test
    void testParseEmptyCSV() throws Exception {
        String csvData = "";
        InputStream is = new ByteArrayInputStream(csvData.getBytes());
        
        List<Map<String, String>> result = CSVParser.parse(is);
        
        assertTrue(result.isEmpty());
    }

    @Test
    void testParseCSVWithQuotes() throws Exception {
        String csvData = "Name,Description\n\"John Doe\",\"A, B, C\"";
        InputStream is = new ByteArrayInputStream(csvData.getBytes());
        
        List<Map<String, String>> result = CSVParser.parse(is);
        
        assertEquals(1, result.size());
        assertEquals("John Doe", result.get(0).get("Name"));
    }
}
