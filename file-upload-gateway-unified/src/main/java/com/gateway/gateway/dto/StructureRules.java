 package com.gateway.gateway.dto;

public class StructureRules {
    private int minRows;
    private int maxRows;
    private boolean strictColumnOrder;
    private boolean allowExtraColumns;

    public int getMinRows() {
        return minRows;
    }

    public void setMinRows(int minRows) {
        this.minRows = minRows;
    }

    public int getMaxRows() {
        return maxRows;
    }

    public void setMaxRows(int maxRows) {
        this.maxRows = maxRows;
    }

    public boolean isStrictColumnOrder() {
        return strictColumnOrder;
    }

    public void setStrictColumnOrder(boolean strictColumnOrder) {
        this.strictColumnOrder = strictColumnOrder;
    }

    public boolean isAllowExtraColumns() {
        return allowExtraColumns;
    }

    public void setAllowExtraColumns(boolean allowExtraColumns) {
        this.allowExtraColumns = allowExtraColumns;
    }
}
