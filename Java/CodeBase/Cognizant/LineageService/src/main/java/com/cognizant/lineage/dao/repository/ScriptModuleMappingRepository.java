package com.cognizant.lineage.dao.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.cognizant.lineage.dao.entity.ScriptModuleMapping;

@Repository
public interface ScriptModuleMappingRepository extends JpaRepository<ScriptModuleMapping, Long> {
	
	@Query("SELECT s FROM ScriptModuleMapping s where s.projectName = :projectName and s.scriptName = :scriptName")
	Optional<ScriptModuleMapping> findByScriptName(@Param("projectName") String projectName, @Param("scriptName") String scriptName);

	@Query("SELECT DISTINCT s.projectName from ScriptModuleMapping s ORDER BY s.projectName")
	List<String> getAllProjectNamesFromSprintMapping();

	interface NodeAndScriptDto {
		String getName();
		String getType();
		String getProjectName();
	}

	@Query(nativeQuery = true, value = "select count(node_name) from ( \n" +
			"select from_node as node_name from presentation.edges where project_name = :projectName \n" +
			"and script_name = :fileName and script_type = :technology \n" +
			"union \n" +
			"select to_node as node_name from presentation.edges where project_name = :projectName \n" +
			"and script_name = :fileName and script_type = :technology) a")
	Integer getTableCountForComplexityForTableauAndCognos(@Param("projectName") String projectName,
														  @Param("fileName") String fileName,
														  @Param("technology") String technology);
}
