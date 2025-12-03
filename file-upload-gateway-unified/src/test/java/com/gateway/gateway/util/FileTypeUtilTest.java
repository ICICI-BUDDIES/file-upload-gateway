package com.gateway.gateway.util;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import static org.junit.jupiter.api.Assertions.*;

class FileTypeUtilTest {

    @Test
    void testDetectTypeCSV() {
        MockMultipartFile file = new MockMultipartFile("file", "test.csv", "text/csv", "data".getBytes());
        String type = FileTypeUtil.detectTypeByName(file);
        assertEquals("csv", type);
    }

    @Test
    void testDetectTypeExcel() {
        MockMultipartFile file1 = new MockMultipartFile("file", "test.xls", "application/vnd.ms-excel", "data".getBytes());
        assertEquals("xls", FileTypeUtil.detectTypeByName(file1));
        
        MockMultipartFile file2 = new MockMultipartFile("file", "test.xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", "data".getBytes());
        assertEquals("xlsx", FileTypeUtil.detectTypeByName(file2));
    }

    @Test
    void testDetectTypeTxt() {
        MockMultipartFile file = new MockMultipartFile("file", "test.txt", "text/plain", "data".getBytes());
        String type = FileTypeUtil.detectTypeByName(file);
        assertEquals("txt", type);
    }

    @Test
    void testDetectTypeUnknown() {
        MockMultipartFile file = new MockMultipartFile("file", "test.pdf", "application/pdf", "data".getBytes());
        String type = FileTypeUtil.detectTypeByName(file);
        assertEquals("unknown", type);
    }

    @Test
    void testDetectTypeNoExtension() {
        MockMultipartFile file = new MockMultipartFile("file", "testfile", "text/plain", "data".getBytes());
        String type = FileTypeUtil.detectTypeByName(file);
        assertEquals("unknown", type);
    }
}
