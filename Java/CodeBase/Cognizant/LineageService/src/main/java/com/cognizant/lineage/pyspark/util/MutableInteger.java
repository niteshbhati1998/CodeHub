package com.cognizant.lineage.pyspark.util;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MutableInteger {
    Integer integer;

    public void setValue(Integer integer) {
        this.integer = integer;
    }

    public Integer getValue() {
        return integer;
    }

    public Integer increase() {
        integer = integer + 1;
        return integer;
    }
}
