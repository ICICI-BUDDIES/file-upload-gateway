package com.gateway.template.parser;

import org.junit.jupiter.api.Test;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class PipeParserTest {

    @Test
    void testParsePipeSeparatedValues() throws Exception {
        String data = "Name|Age|City\nJohn|30|NYC\nJane|25|LA";
        InputStream is = new ByteArrayInputStream(data.getBytes());
        
        List<Map<String, String>> result = PipeParser.parse(is);
        
        assertEquals(2, result.size());
        assertEquals("John", result.get(0).get("Name"));
        assertEquals("30", result.get(0).get("Age"));
        assertEquals("NYC", result.get(0).get("City"));
    }

    @Test
    void testParseWithSpaces() throws Exception {
        String data = "Name | Age | City\nJohn | 30 | NYC";
        InputStream is = new ByteArrayInputStream(data.getBytes());
        
        List<Map<String, String>> result = PipeParser.parse(is);
        
        assertEquals(1, result.size());
        assertEquals("John ", result.get(0).get("Name"));
    }

    @Test
    void testParseEmptyFile() throws Exception {
        String data = "";
        InputStream is = new ByteArrayInputStream(data.getBytes());
        
        List<Map<String, String>> result = PipeParser.parse(is);
        
        assertTrue(result.isEmpty());
    }
}
