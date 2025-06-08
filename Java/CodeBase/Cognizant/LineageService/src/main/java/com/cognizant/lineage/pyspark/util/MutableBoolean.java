package com.cognizant.lineage.pyspark.util;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MutableBoolean {

    private Boolean aBoolean;

    public boolean getValue() {
        return aBoolean.booleanValue();
    }

    public void setValue(Boolean aBoolean) {
        this.aBoolean = aBoolean;
    }
}
