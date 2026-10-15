package com.ems.model;

/** One item in a dropdown list: the value that is sent, and the text that is shown. */
public class Option {
    private final String value;
    private final String label;

    public Option(String value, String label) {
        this.value = value;
        this.label = label;
    }

    public String getValue() { return value; }
    public String getLabel() { return label; }
}
