package com.cognizant.lineage.pyspark.read;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Component
@NoArgsConstructor
public class FileReader {

    private Logger LOGGER = LogManager.getLogger(FileReader.class);
    @Getter
    private String fileName;

    @Getter
    private String folderName;

    private BufferedReader br;


    public FileReader(String fileName) {
        loadFile(fileName);
    }

    private void loadFile(String fileName) {
    	java.io.FileReader fileReader = null;
        try {
            File file = new File(fileName);

            this.folderName = file.getParent();
            this.fileName = file.getName();
            fileReader = new java.io.FileReader(file);
            br = new BufferedReader(fileReader);
        } catch (Exception e) {
            LOGGER.error("Could not able to load file : {}", fileName);
            LOGGER.error("ERROR!!! File loading!!!! ", e);
            close();
        } finally {
        	close();
        	closeFileReader(fileReader);
        }
    }

    public FileReader initializeReader(String fileName) {
        loadFile(fileName);
        return this;
    }
    public String readNextLine() {
        try {
            String str;
            while ((str = br.readLine()) != null) {
                return str.trim();
            }
        } catch (IOException e) {
            close();
            return "EOF";
        }
        close();
        return "EOF";
    }

    public void close() {
        if(br != null) {
            try {
                br.close();
            } catch (IOException e) {
                LOGGER.error("ERROR!!! File closing!!!! ", e);
                throw new RuntimeException(e);
            }
        }
    }
    
    public void closeFileReader(java.io.FileReader fileReader) {
        if(fileReader != null) {
            try {
            	fileReader.close();
            } catch (IOException e) {
                LOGGER.error("ERROR!!! File closing!!!! ", e);
                throw new RuntimeException(e);
            }
        }
    }
}
