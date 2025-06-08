package com.cognizant.lineage.dao.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.cognizant.lineage.dao.entity.NodeBasedSprintPlanCustomObjectBased;

import jakarta.transaction.Transactional;

public interface NodeBasedSprintPlanCustomObjectBasedRepository extends CrudRepository<NodeBasedSprintPlanCustomObjectBased,Long> {
	
	@Modifying
	@Transactional
	void deleteByProjectNameAndSelectedNodeAndWave(String projectName, String selectedNode, Integer wave);
	
	@Query(value = "select count(*) from presentation.node_based_sprint_plan_custom s where s.project_name = :projectName and s.selected_node = :selectedNode", nativeQuery = true)
	int getCount(String projectName, String selectedNode);
	
	@Query(value = "select distinct s.wave from presentation.node_based_sprint_plan_custom s where s.project_name = :projectName and s.selected_node = :selectedNode order by s.wave asc", nativeQuery = true)
	List<Integer> getWaveList(String projectName, String selectedNode);
	
	@Query(value = "select distinct s.sprint from presentation.node_based_sprint_plan_custom s where s.project_name = :projectName and s.selected_node = :selectedNode and s.wave = :wave order by s.sprint asc" , nativeQuery = true)
	List<Integer> getSprintList(String projectName, String selectedNode, Integer wave);
	
	@Query(value = "select * from presentation.node_based_sprint_plan_custom s where s.project_name = :projectName and s.selected_node = :selectedNode and s.wave = :wave and s.sprint = :sprint" , nativeQuery = true)
	List<NodeBasedSprintPlanCustomObjectBased> getDataForEachSprint(String projectName, String selectedNode, Integer wave, Integer sprint);

    //OnlyObject
	@Modifying
	@Transactional
	@Query(value = "delete from presentation.node_based_sprint_plan_custom s where s.project_name = :projectName and s.selected_node = :selectedNode and s.wave = :wave and s.migration_type = 'Data'", nativeQuery = true)
	void deleteByProjectNameAndSelectedNodeAndWaveOnlyObject(String projectName, String selectedNode, Integer wave);

	@Query(value = "select distinct s.wave from presentation.node_based_sprint_plan_custom s where s.project_name = :projectName and s.selected_node = :selectedNode and s.migration_type = 'Data' order by s.wave asc", nativeQuery = true)
	List<Integer> getWaveListOnlyObject(String projectName, String selectedNode);
	
	@Query(value = "select distinct s.sprint from presentation.node_based_sprint_plan_custom s where s.project_name = :projectName and s.selected_node = :selectedNode and s.wave = :wave and s.migration_type = 'Data' order by s.sprint asc" , nativeQuery = true)
	List<Integer> getSprintListOnlyObject(String projectName, String selectedNode, Integer wave);
	
	@Query(value = "select * from presentation.node_based_sprint_plan_custom s where s.project_name = :projectName and s.selected_node = :selectedNode and s.wave = :wave and s.sprint = :sprint and s.migration_type = 'Data'" , nativeQuery = true)
	List<NodeBasedSprintPlanCustomObjectBased> getDataForEachSprintOnlyObject(String projectName, String selectedNode, Integer wave, Integer sprint);
}