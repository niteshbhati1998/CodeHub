package com.cognizant.lineage.dao.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Query;

import com.cognizant.lineage.dao.entity.NodeBasedSprintPlanObjectBased;

public interface NodeBasedSprintPlanObjectBasedRepository extends CrudRepository<NodeBasedSprintPlanObjectBased,Long> {
	
	@Query(value = "select distinct s.wave from presentation.node_based_sprint_plan s where s.project_name = :projectName and s.selected_node = :selectedNode order by s.wave asc", nativeQuery = true)
	List<Integer> getWaveList(String projectName, String selectedNode);
	
	@Query(value = "select distinct s.sprint from presentation.node_based_sprint_plan s where s.project_name = :projectName and s.selected_node = :selectedNode and s.wave = :wave order by s.sprint asc" , nativeQuery = true)
	List<Integer> getSprintList(String projectName, String selectedNode, Integer wave);
	
	@Query(value = "select * from presentation.node_based_sprint_plan s where s.project_name = :projectName and s.selected_node = :selectedNode and s.wave = :wave and s.sprint = :sprint" , nativeQuery = true)
	List<NodeBasedSprintPlanObjectBased> getDataForEachSprint(String projectName, String selectedNode, Integer wave, Integer sprint);
	
	//OnlyObject
	@Query(value = "select distinct s.wave from presentation.node_based_sprint_plan s where s.project_name = :projectName and s.selected_node = :selectedNode and s.migration_type = 'Data' order by s.wave asc", nativeQuery = true)
	List<Integer> getWaveListOnlyObject(String projectName, String selectedNode);
	
	@Query(value = "select distinct s.sprint from presentation.node_based_sprint_plan s where s.project_name = :projectName and s.selected_node = :selectedNode and s.wave = :wave and s.migration_type = 'Data' order by s.sprint asc" , nativeQuery = true)
	List<Integer> getSprintListOnlyObject(String projectName, String selectedNode, Integer wave);
	
	@Query(value = "select * from presentation.node_based_sprint_plan s where s.project_name = :projectName and s.selected_node = :selectedNode and s.wave = :wave and s.sprint = :sprint and s.migration_type = 'Data'" , nativeQuery = true)
	List<NodeBasedSprintPlanObjectBased> getDataForEachSprintOnlyObject(String projectName, String selectedNode, Integer wave, Integer sprint);

}