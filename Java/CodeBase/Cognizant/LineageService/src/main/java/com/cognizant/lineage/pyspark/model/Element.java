package com.cognizant.lineage.pyspark.model;

import java.util.ArrayList;
import java.util.List;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@Setter
@Getter
@NoArgsConstructor
@EqualsAndHashCode
@Builder
public class Element {
    private String name;

    private SourceDestinationType type;

    private List<String> list = new ArrayList<>();

    private int level;

    private List<Element> derivedFrom = new ArrayList<>();

    private List<Element> nextElements = new ArrayList<>();

    private String actualCode;

    private String lineNos;

    private String query;

    private String elementId;


    public Element(String name, SourceDestinationType type, List<String> list, int level, List<Element> derivedFrom, List<Element> nextElements, String actualCode, String lineNos, String query, String elementId) {
        this.name = name;
        this.type = type;
        this.list = list != null ? list : this.list;
        this.level = level;
        this.derivedFrom = derivedFrom != null ? derivedFrom : this.derivedFrom;
        this.nextElements = nextElements != null ? nextElements : this.nextElements;
        this.actualCode = actualCode;
        this.lineNos = lineNos;
        this.query = query;
        this.elementId = elementId;
    }
}
