package com.cognizant.lineage.upload.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.cognizant.lineage.upload.constants.TechnologyConstants;

@Service
public class ScriptLineageService {

    @Value("${scriptLineageScriptOutputLocation}")
    String scriptLineageScriptOutputLocation;

    @Value("${scriptLineageCleansingScript}")
    String scriptLineageCleansingScript;

    @Value("${base.path.location}")
    private String basePathLocation;

    @Value("${csrfToken}")
    private String csrfToken;

    @Value("${scriptLineageLoadingScript}")
    String scriptLineageLoadingScript;

    @Autowired
    JobStatusService jobStatusService;

    private static final Logger LOGGER = LoggerFactory.getLogger(ScriptLineageService.class);

    public Integer invokeScriptLineageCleansing(Long jobId, String inputFilePath, String parentTech, String tech) {
        try {
            LOGGER.info("Script lineage cleansing - jobId: {}, inputFilePath: {}, parentTech: {}, tech: {}",
                    jobId, inputFilePath, parentTech, tech);
            File newOutputDirectory = new File(scriptLineageScriptOutputLocation + jobId);
            newOutputDirectory.mkdir();

            // tech variable is modified as per python script technology dictionary
            if ((TechnologyConstants.ORACLE.equalsIgnoreCase(parentTech) ||
                    TechnologyConstants.MS_SQL_SERVER.equalsIgnoreCase(parentTech))
                    && !TechnologyConstants.ODI.equalsIgnoreCase(tech)) {
                tech = parentTech.replaceAll(" ", "").concat(tech);
            }
            ProcessBuilder processBuilder = new ProcessBuilder("python3.9", scriptLineageCleansingScript,
                    String.valueOf(jobId), inputFilePath,
                    scriptLineageScriptOutputLocation.concat(String.valueOf(jobId)), tech.toLowerCase());
            StringBuilder command = new StringBuilder();
            StringBuilder output = new StringBuilder();

            for (String commandItem : processBuilder.command()) {
                command.append(commandItem).append(" ");
            }
            LOGGER.info("Script Lineage Cleansing Command: {}", command);

//            processBuilder.directory(new File(basePathLocation.concat("/")));
            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
            int exitCode = process.waitFor();
            if (exitCode == 0) {
                LOGGER.info("Script Lineage Cleansing script executed successfully");
            } else {
                LOGGER.info("Script Lineage Cleansing script execution failed with exit code: {}", exitCode);
            }
            LOGGER.info("Script Lineage Cleansing output: {}", output);
            return exitCode;
        } catch (IOException ex1) {
            LOGGER.info("Exception occurred in invokeScriptLineageScript Service: {}", ex1.getMessage());
        } catch (InterruptedException ex2) {
            LOGGER.info("Exception occurred in invokeScriptLineageScript Service: {}", ex2.getMessage());
            Thread.currentThread().interrupt();
        }
        return 1;
    }

    public void invokeScriptLineageIdentification(Long jobId, String projectName) {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder("python3.9", scriptLineageLoadingScript,
                    csrfToken, String.valueOf(jobId), projectName);
            StringBuilder command = new StringBuilder();
            StringBuilder output = new StringBuilder();

            for (String commandItem : processBuilder.command()) {
                command.append(commandItem).append(" ");
            }
            LOGGER.info("Script Lineage Loading Command: {}", command);

//            processBuilder.directory(new File(basePathLocation.concat("/")));
            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
            int exitCode = process.waitFor();
            if (exitCode == 0) {
                LOGGER.info("Script Lineage Loading script executed successfully");
                jobStatusService.updateJobStatusDetailsWithScriptLineageString(jobId);
            } else {
                LOGGER.info("Script Lineage Loading execution failed with exit code: {}", exitCode);
            }
            LOGGER.info("Script Lineage Loading output: {}", output);
        } catch (IOException ex1) {
            LOGGER.info("Exception occurred in invokeScriptLineageScript Service: {}", ex1.getMessage());
        } catch (InterruptedException ex2) {
            LOGGER.info("Exception occurred in invokeScriptLineageScript Service: {}", ex2.getMessage());
            Thread.currentThread().interrupt();
        }
    }
}