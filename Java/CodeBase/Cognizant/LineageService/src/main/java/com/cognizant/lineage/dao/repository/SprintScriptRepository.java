package com.cognizant.lineage.dao.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.cognizant.lineage.dao.entity.SprintScripts;
import com.cognizant.lineage.upload.model.BarChart;
import com.cognizant.lineage.upload.model.BarChartCount;
import com.cognizant.lineage.upload.model.PiScriptCount;
import com.cognizant.lineage.upload.model.PiScriptCountModuleDto;
import com.cognizant.lineage.upload.model.SprintQueryCount;
import com.cognizant.lineage.upload.model.SprintScriptDetails;
import com.cognizant.lineage.upload.model.WaveData;

@Repository
public interface SprintScriptRepository extends JpaRepository<SprintScripts, Long> {
	
	@Query("SELECT new com.cognizant.lineage.upload.model.SprintScriptDetails(count(s), sum(s.noOfLines)) FROM SprintScripts s where s.projectName = :projectName and s.module = :module")
	SprintScriptDetails findCountSizeByProjectName(@Param("projectName")String projectName, @Param("module")String module);
	
	@Query("SELECT new com.cognizant.lineage.upload.model.SprintScriptDetails(count(s), sum(s.noOfLines)) FROM SprintScripts s where s.projectName = :projectName")
	SprintScriptDetails findCountSizeByProjectNameWithoutModeule(@Param("projectName")String projectName);
	
	@Query("SELECT distinct wave FROM SprintScripts s where s.projectName = :projectName and s.module = :module")
	List<Object> findDistinctWaveByProjectName(@Param("projectName")String projectName, @Param("module")String module);
	
	@Query("SELECT count(s) FROM SprintScripts s where s.projectName = :projectName and s.module = :module and s.wave = :wave")
	Object findCountByWave(@Param("projectName")String projectName, @Param("module")String module, @Param("wave")Object object);
	
	@Query("SELECT new com.cognizant.lineage.upload.model.WaveData(module, wave, technology, complexity, count(complexity)) FROM SprintScripts s where s.projectName = :projectName AND s.module in (:module) group by technology, wave, complexity, module")
	List<WaveData> findWaveAndTechnologyByProjectNameAndModule(@Param("projectName")String projectName, @Param("module")List<String> module);
	
	@Query("SELECT new com.cognizant.lineage.upload.model.WaveData(module, wave, technology, complexity, count(complexity)) FROM SprintScripts s where s.projectName = :projectName group by technology, wave, complexity, module")
	List<WaveData> findWaveAndTechnologyByProjectName(@Param("projectName")String projectName);
	
	@Query(value = "select abc.technology, abc.module, abc.script_name, abc.object_name, abc.complexity, sum(abc.no_of_lines) from\r\n" + 
			"(select o.technology, e.script_name, e.to_node as object_name, o.module, o.complexity, o.no_of_lines from presentation.edges e,presentation.sprint_scripts o\r\n" + 
			"where o.project_name= :projectName and o.technology In (:technology) and o.module= :module and o.sprint= :wave and o.complexity = :complexity\r\n" + 
			"and e.script_name = o.script_name\r\n" + 
			"union\r\n" + 
			"select o.technology, e.script_name, e.from_node as object_name, o.module, o.complexity, o.no_of_lines from presentation.edges e,presentation.sprint_scripts o\r\n" + 
			"where o.project_name= :projectName and o.technology In (:technology) and o.module= :module and o.sprint= :wave and o.complexity = :complexity\r\n" + 
			"and e.script_name = o.script_name) abc group by technology, module, script_name, object_name, complexity;", nativeQuery = true)
	List<Object> findScriptAndSprintByProjectName(@Param("projectName") String projectName, @Param("module") String module, @Param("technology") List<String> technology, @Param("wave") int wave, @Param("complexity")String complexity);

	@Query("SELECT new com.cognizant.lineage.upload.model.WaveData(module, wave, technology, complexity, count(complexity)) FROM SprintScripts s where s.projectName = :projectName AND s.module in (:module) AND s.technology IN (:technology) group by technology, wave, complexity, module")
	List<WaveData> findWaveAndTechnologyByProjectNameAndTechnology(@Param("projectName")String projectName, @Param("module")List<String> module, @Param("technology")List<String> technology);
	
	@Query("select new com.cognizant.lineage.upload.model.BarChart(module, count(s.scriptName)) FROM SprintScripts s where s.projectName = :projectName group by module")
	List<BarChart> findCountTableByProjectNameAndModule(@Param("projectName")String projectName);
	
//	@Query("select distinct new com.cognizant.lineage.upload.model.PiScriptCount(technology, count(s) as scriptCount) from SprintScripts s where s.projectName = :projectName and s.module = :module and\r\n" + 
//			"(upper(s.technology) NOT LIKE 'BI-COGNOS%' AND \r\n" + 
//			"upper(s.technology) NOT LIKE 'BI-TABLEAU%' AND upper(s.technology) NOT LIKE 'BI-QLIKVIEW%' AND upper(s.technology) NOT LIKE 'POWER-BI%' and upper(s.technology) NOT LIKE 'TERADATA-VIEW%' and upper(s.technology) NOT LIKE 'ORACLE-VIEW%') GROUP BY s.technology")
//	List<PiScriptCount> getScriptCountByProjectNameAndModule(@Param("projectName")String projectName, @Param("module")String module);
	
	@Query("select distinct new com.cognizant.lineage.upload.model.PiScriptCount(technology, count(s) as scriptCount) from SprintScripts s where s.projectName = :projectName and s.module = :module and\r\n" + 
			"(upper(s.technology) NOT LIKE '%BI%') GROUP BY s.technology")
	List<PiScriptCount> getScriptCountByProjectNameAndModule(@Param("projectName")String projectName, @Param("module")String module);
	
//	@Query("select distinct new com.cognizant.lineage.upload.model.PiScriptCount(technology, count(s) as scriptCount) from SprintScripts s where s.projectName = :projectName and\r\n" + 
//			"(upper(s.technology) NOT LIKE 'BI-COGNOS%' AND \r\n" + 
//			"upper(s.technology) NOT LIKE 'BI-TABLEAU%' AND upper(s.technology) NOT LIKE 'BI-QLIKVIEW%' AND upper(s.technology) NOT LIKE 'POWER-BI%' and upper(s.technology) NOT LIKE 'TERADATA-VIEW%' and upper(s.technology) NOT LIKE 'ORACLE-VIEW%') GROUP BY s.technology")
//	List<PiScriptCount> getScriptCountByProjectName(@Param("projectName")String projectName);
	
	@Query("select distinct new com.cognizant.lineage.upload.model.PiScriptCount(technology, count(s) as scriptCount) from SprintScripts s where s.projectName = :projectName and\r\n" + 
			"(upper(s.technology) NOT LIKE '%BI%') GROUP BY s.technology")
	List<PiScriptCount> getScriptCountByProjectName(@Param("projectName")String projectName);
	
//	@Query("select distinct new com.cognizant.lineage.upload.model.PiScriptCount(technology, count(s) as scriptCount) from SprintScripts s where s.projectName = :projectName and s.module = :module and\r\n" + 
//			"(upper(s.technology) LIKE 'BI-COGNOS%' OR \r\n" + 
//			"upper(s.technology) LIKE 'BI-TABLEAU%' OR upper(s.technology) LIKE 'BI-QLIKVIEW%' OR upper(s.technology) LIKE 'POWER-BI%') GROUP BY s.technology")
//	List<PiScriptCount> getScriptCountByProjectNameAndModuleForReport(@Param("projectName")String projectName, @Param("module")String module);
	
	@Query("select distinct new com.cognizant.lineage.upload.model.PiScriptCount(technology, count(s) as scriptCount) from SprintScripts s where s.projectName = :projectName and s.module = :module and\r\n" + 
			"(upper(s.technology) LIKE '%BI%') GROUP BY s.technology")
	List<PiScriptCount> getScriptCountByProjectNameAndModuleForReport(@Param("projectName")String projectName, @Param("module")String module);
	
	@Query("select distinct new com.cognizant.lineage.upload.model.PiScriptCount(technology, count(s) as scriptCount) from SprintScripts s where s.projectName = :projectName and\r\n" + 
			"(upper(s.technology) LIKE '%BI%') GROUP BY s.technology")
	List<PiScriptCount> getScriptCountByProjectNameForReport(@Param("projectName")String projectName);
	

	@Query("select new com.cognizant.lineage.upload.model.SprintQueryCount(s.technology, s.module, count(s) as count) from SprintScripts s where s.projectName= :projectName group by s.module, s.technology")
	List<SprintQueryCount> getModuleRelatedScriptCountByProjectName(@Param("projectName") String projectName);

	@Query("SELECT distinct technology FROM SprintScripts s where s.projectName = :projectName and s.module in (:module)")
	List<String> findTechnologyByProjectName(@Param("projectName")String projectName, @Param("module")List<String> module);
	
	@Query("SELECT distinct wave FROM SprintScripts s where s.projectName= :projectName and s.module= :module and s.technology IN (:technology)")
	List<String> findDistinctWaveByProjectNameAndTechnology(@Param("projectName")String projectName, @Param("module")String module, @Param("technology")List<String> technology);
	
	@Query(value = "select abc.technology, abc.module, abc.script_name, abc.object_name, abc.complexity, sum(abc.no_of_lines) from\r\n" + 
			"(select o.technology, e.script_name, e.to_node as object_name, o.module, o.complexity, o.no_of_lines from presentation.edges e,presentation.sprint_scripts o\r\n" + 
			"where o.project_name= :projectName and o.technology In (:technology) and o.module= :module and o.sprint= :wave\r\n" + 
			"and e.script_name = o.script_name\r\n" + 
			"union\r\n" + 
			"select o.technology, e.script_name, e.from_node as object_name, o.module, o.complexity, o.no_of_lines from presentation.edges e,presentation.sprint_scripts o\r\n" + 
			"where o.project_name= :projectName and o.technology In (:technology) and o.module= :module and o.sprint= :wave\r\n" + 
			"and e.script_name = o.script_name) abc group by technology, module, script_name, object_name, complexity;", nativeQuery = true)
	List<Object> findScriptAndSprintByProjectNameAndWithoutComplexity(@Param("projectName") String projectName, @Param("module") String module, @Param("technology") List<String> technology, @Param("wave") int wave);
	
	@Query(value = "select sprint, count(s) from presentation.sprint_scripts s where s.project_name = :projectName and s.module = :module and\r\n" + 
			"(upper(s.technology) NOT LIKE '%BI%') GROUP BY  s.sprint;", nativeQuery =  true)
	List<BarChartCount> getEtlAndEltCountByProjectNameAndModule(@Param("projectName")String projectName, @Param("module")String module);
	
	@Query(value = "select  sprint, count(s) from presentation.sprint_scripts s where \r\n" + 
			"s.project_name = :projectName and s.module = :module and (upper(s.technology) LIKE '%BI%') GROUP BY 1;", nativeQuery = true)
	List<BarChartCount> getAnalyticsCountByProjectNameAndModule(@Param("projectName")String projectName, @Param("module")String module);
	
	 @Query(value = "select s.module, count(s) as scriptCount from presentation.sprint_scripts s where s.project_name= :projectName and s.technology like '%BI%' group by 1;", nativeQuery =  true)
	 List<PiScriptCountModuleDto> getScriptCountByProjectNameForReportModule(@Param("projectName")String projectName);
	 
	 @Query(value = "select module, count(*) as scriptCount from presentation.sprint_scripts where project_name= :projectName and technology not like '%BI%' group by 1;", nativeQuery = true)
	 List<PiScriptCountModuleDto> getScriptCountByProjectNameForModule(@Param("projectName")String projectName);
}