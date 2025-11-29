package com.gateway.gateway.dto;

import java.util.List;
import java.util.Map;

public class ExtractedFile {
     private List<String> headers;
    private List<Map<String, Object>> rows;
    public List<String> getHeaders() {
        return headers;
    }

    public void setHeaders(List<String> headers) {
        this.headers = headers;
    }

    public List<Map<String, Object>> getRows() {
        return rows;
    }

    public void setRows(List<Map<String, Object>> rows) {
        this.rows = rows;
    }
}
