package com.urcodebin.api.enums;

public enum PasteVisibility {
    PUBLIC ("Public"),
    PRIVATE ("Private");

    private final String value;
    PasteVisibility(String value) {
        this.value = value;
    }

    public String getStringValue() {
        return value;
    }
}
