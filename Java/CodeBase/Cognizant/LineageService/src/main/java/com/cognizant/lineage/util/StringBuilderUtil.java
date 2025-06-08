package com.cognizant.lineage.util;

public class StringBuilderUtil {
    private StringBuilder stringBuilder;

    public StringBuilderUtil() {
        this.stringBuilder = new StringBuilder();
    }

    public StringBuilderUtil(StringBuilder stringBuilder) {
        this.stringBuilder = stringBuilder;
    }

    public StringBuilderUtil(String string) {
        this.stringBuilder = new StringBuilder(string);
    }

    public StringBuilderUtil append(String string) {
        if(this.stringBuilder.length() > 0) {
            this.stringBuilder.append(",");
        }
        this.stringBuilder.append(string);
        return this;
    }

    public StringBuilderUtil append(StringBuilder stringBuilder) {
        if(this.stringBuilder.length() > 0) {
            this.stringBuilder.append(",");
        }
        this.stringBuilder.append(stringBuilder);
        return this;
    }

    @Override
    public String toString() {
        return this.stringBuilder.toString();
    }

}
