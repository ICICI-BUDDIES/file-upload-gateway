package com.gateway.template.parser;

import org.junit.jupiter.api.Test;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class TxtParserTest {

    @Test
    void testParseCommaDelimited() throws Exception {
        String data = "Name,Age,Email\nJohn,30,john@example.com\nJane,25,jane@example.com";
        InputStream is = new ByteArrayInputStream(data.getBytes());
        
        List<Map<String, String>> result = TxtParser.parse(is);
        
        assertEquals(2, result.size());
        assertEquals("John", result.get(0).get("Name"));
        assertEquals("30", result.get(0).get("Age"));
    }

    @Test
    void testParsePipeDelimited() throws Exception {
        String data = "Name|Age|Email\nJohn|30|john@example.com\nJane|25|jane@example.com";
        InputStream is = new ByteArrayInputStream(data.getBytes());
        
        List<Map<String, String>> result = TxtParser.parse(is);
        
        assertEquals(2, result.size());
        assertEquals("John", result.get(0).get("Name"));
        assertEquals("30", result.get(0).get("Age"));
        assertEquals("john@example.com", result.get(0).get("Email"));
    }

    @Test
    void testParseTabDelimited() throws Exception {
        String data = "Name\tAge\tEmail\nJohn\t30\tjohn@example.com";
        InputStream is = new ByteArrayInputStream(data.getBytes());
        
        List<Map<String, String>> result = TxtParser.parse(is);
        
        assertEquals(1, result.size());
        assertEquals("John", result.get(0).get("Name"));
    }

    @Test
    void testParseEmptyFile() throws Exception {
        String data = "";
        InputStream is = new ByteArrayInputStream(data.getBytes());
        
        List<Map<String, String>> result = TxtParser.parse(is);
        
        assertTrue(result.isEmpty());
    }

    @Test
    void testParseWithEmptyLines() throws Exception {
        String data = "Name|Age\nJohn|30\n\nJane|25";
        InputStream is = new ByteArrayInputStream(data.getBytes());
        
        List<Map<String, String>> result = TxtParser.parse(is);
        
        assertEquals(2, result.size());
    }
}
