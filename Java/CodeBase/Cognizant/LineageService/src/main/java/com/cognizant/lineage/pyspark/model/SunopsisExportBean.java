package com.cognizant.lineage.pyspark.model;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "SunopsisExport")
public class SunopsisExportBean {

    List<ObjectBean> objectBeanList;

    @XmlElement(name = "Object")
    public List<ObjectBean> getObjectBeanList() {
        if (objectBeanList == null)
            objectBeanList = new ArrayList<ObjectBean>();
        return objectBeanList;
    }

    public void setObjectBeanList(List<ObjectBean> objectBeanList) {
        this.objectBeanList = objectBeanList;
    }

    @Override
    public String toString() {
        return "SunopsisExportBean [objectBeanList=" + objectBeanList + "]";
    }

}
