package com.cognizant.lineage.dao.repository;

import java.util.List;

import com.cognizant.lineage.dao.dto.WavePlanDto;
import org.springframework.data.jpa.repository.Query;

import com.cognizant.lineage.dao.entity.SprintDetailsObjectBased;

public interface SprintDetailsObjectBasedRepository extends CrudRepository<SprintDetailsObjectBased,Long> {
	
	@Query(value = "select distinct s.wave from presentation.sprint_details s where s.project_name = :projectName order by s.wave asc", nativeQuery = true)
	List<Integer> getWaveList(String projectName);
	
	@Query(value = "select distinct s.sprint from presentation.sprint_details s where s.project_name = :projectName and s.wave = :wave order by s.sprint asc" , nativeQuery = true)
	List<Integer> getSprintList(String projectName, Integer wave);
	
	@Query(value = "select * from presentation.sprint_details s where s.project_name = :projectName and s.wave = :wave and s.sprint = :sprint" , nativeQuery = true)
	List<SprintDetailsObjectBased> getDataForEachSprint(String projectName, Integer wave, Integer sprint);
	
	//OnlyObject
	@Query(value = "select distinct s.wave from presentation.sprint_details s where s.project_name = :projectName and s.migration_type = 'Data' order by s.wave asc", nativeQuery = true)
	List<Integer> getWaveListOnlyObject(String projectName);
	
	@Query(value = "select distinct s.sprint from presentation.sprint_details s where s.project_name = :projectName and s.wave = :wave and s.migration_type = 'Data' order by s.sprint asc" , nativeQuery = true)
	List<Integer> getSprintListOnlyObject(String projectName, Integer wave);
	
	@Query(value = "select * from presentation.sprint_details s where s.project_name = :projectName and s.wave = :wave and s.sprint = :sprint and s.migration_type = 'Data'" , nativeQuery = true)
	List<SprintDetailsObjectBased> getDataForEachSprintOnlyObject(String projectName, Integer wave, Integer sprint);

	@Query(nativeQuery = true, value =
			"SELECT DISTINCT a.project_name as projectName, a.wave, a.wave_sprint as waveSprint,\n" +
			"  a.sprint, a.migration_type as migrationType, b.script_type as scriptType, c.node_type as nodeType,\n" +
			"  startdate, enddate, duration, size, complexity, status\n" +
			"FROM presentation.sprint_details a\n" +
			"  LEFT JOIN presentation.edges b\n" +
			"         ON TRIM (a.project_name) = TRIM (b.project_name)\n" +
			"        AND UPPER (TRIM (a.wave_sprint)) = UPPER (TRIM (b.script_name))\n" +
			"  LEFT JOIN presentation.nodes c\n" +
			"         ON TRIM (a.project_name) = TRIM (c.project_name)\n" +
			"        AND UPPER (TRIM (a.wave_sprint)) = UPPER (TRIM (c.node_name))\n" +
			"WHERE a.project_name = :projectName ORDER BY 2,3")
	List<WavePlanDto> getToolRecommendedWavePlanDetails(String projectName);
}