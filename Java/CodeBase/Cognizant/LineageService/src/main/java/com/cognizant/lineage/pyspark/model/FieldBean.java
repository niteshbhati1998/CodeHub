package com.cognizant.lineage.pyspark.model;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlValue;

@XmlRootElement(name = "Field")
public class FieldBean {

    String name="";
    String type="";
    String textContent="";

    int start;

    int end;


    @XmlTransient
    public int getStart() {
        return start;
    }


    @XmlTransient
    public int getEnd() {
        return end;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public void setEnd(int end) {
        this.end = end;
    }

    @XmlAttribute(name = "name")
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name=name;
    }

    @XmlAttribute(name = "type")
    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type=type;
    }

    @XmlValue
    public String getTextContent() {
        return textContent;
    }
    public void setTextContent(String textContent) {
        this.textContent=textContent;
    }

    @Override
    public String toString() {
        return "FieldBean [name=" + name + ", type=" + type + ", textContent=" + textContent + "]";
    }


}
