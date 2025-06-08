package com.cognizant.lineage.pyspark.read;

import java.io.File;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import com.cognizant.lineage.util.Sanitization;

@Component
@Configuration
@PropertySource("classpath:application.properties")
public class FileLoader {

    private Logger LOGGER = LogManager.getLogger(FileLoader.class);

	//@Getter
    //private static List<String> fileList = new ArrayList<>();

    public List<String> getFileLoader(String path) {
    	List<String> fileList = new ArrayList<>();
		try {	
			Path p = Paths.get(path);
			String normalizedPath = Sanitization.sanitizeInput(p.normalize().toString());	
			File folderOrFile = new File(normalizedPath);
            //File folderOrFile = new File(FilenameUtils.normalize(path));
         
            if(folderOrFile.isFile()) {
                fileList.add(folderOrFile.getPath());
            } else if(folderOrFile.isDirectory()) {
                loadAllFileNames(folderOrFile.listFiles(), fileList);
            } else {
            	URL folderOrFileNameURL = getClass().getResource(normalizedPath);
                folderOrFile = new File(folderOrFileNameURL.toURI());
                if(folderOrFile.isFile()) {
                    fileList.add(folderOrFile.getPath());
                } else if(folderOrFile.isDirectory()) {
                    loadAllFileNames(folderOrFile.listFiles(), fileList);
                }
            }
            if(fileList.isEmpty()) {
                LOGGER.info("No python files found!!");
            } else {
                LOGGER.info("Total python files found: {}", fileList.size());
            }
        } catch (Exception e) {
            LOGGER.error("Error while loading files: ", e);
        }
		return fileList;
    }

    private void loadAllFileNames(File[] arr, List<String> fileList)
    {
        for (File f : arr) {
            if (f.isFile() && f.getName().endsWith(".py"))
                //f.getParent().
                fileList.add(f.getPath());
            else if (f.isDirectory()) {
                loadAllFileNames(f.listFiles(), fileList);
            }
        }
    }
}