package com.cognizant.lineage.pyspark.model;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "Object")
public class ObjectBean {

    List<FieldBean> fieldBeanList;

    @XmlElement(name = "Field")
    public List<FieldBean> getFieldBeanList() {
        if (fieldBeanList == null)
            fieldBeanList = new ArrayList<FieldBean>();
        return fieldBeanList;
    }
    public void setFieldBeanList(List<FieldBean> fieldBeanList) {
        this.fieldBeanList = fieldBeanList;
    }

}
