package com.vv.personal.model;

import lombok.Getter;

@Getter
public enum UiMode {
    RANDOM("RANDOM"),
    ACCESSED("ACCESSED"),
    MARKED("MARKED");

    private final String value;

    UiMode(String value) {
        this.value = value;
    }

    private static UiMode getValue(String value) {
        return UiMode.valueOf(value);
    }
}
