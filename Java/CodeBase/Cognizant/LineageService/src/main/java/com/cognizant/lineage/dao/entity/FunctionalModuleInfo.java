package com.cognizant.lineage.dao.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "functional_module_info", schema = "presentation")
public class FunctionalModuleInfo {

    @Column(name = "project_name")
    private String projectName;

    @Id
    @Column(name = "scripts_objects")
    private String scriptsObjects;

    @Column(name = "functional_module")
    private String functionalModule;

    @Column(name = "type")
    private String scriptObjectType;
}