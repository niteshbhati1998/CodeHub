package com.cognizant.lineage.dao.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Query;

import com.cognizant.lineage.dao.entity.NodeBasedSprintPlanApplicationBased;

public interface NodeBasedSprintPlanApplicationBasedRepository extends CrudRepository<NodeBasedSprintPlanApplicationBased,Long> {
	
	@Query(value = "select distinct s.wave from presentation.node_based_sprint_plan_scripts s where s.project_name = :projectName and s.selected_node = :selectedNode order by s.wave asc", nativeQuery = true)
	List<Integer> getWaveList(String projectName, String selectedNode);
	
	@Query(value = "select distinct s.sprint from presentation.node_based_sprint_plan_scripts s where s.project_name = :projectName and s.selected_node = :selectedNode and s.wave = :wave order by s.sprint asc" , nativeQuery = true)
	List<Integer> getSprintList(String projectName, String selectedNode, Integer wave);
	
	@Query(value = "select * from presentation.node_based_sprint_plan_scripts s where s.project_name = :projectName and s.selected_node = :selectedNode and s.wave = :wave and s.sprint = :sprint" , nativeQuery = true)
	List<NodeBasedSprintPlanApplicationBased> getDataForEachSprint(String projectName, String selectedNode, Integer wave, Integer sprint);
}