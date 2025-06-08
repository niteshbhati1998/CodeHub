package com.cognizant.lineage.util;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.cognizant.lineage.exception.LineageBusinessException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class CommonUtil {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(CommonUtil.class);

	public static void uploadAllScriptsToInputLocation(MultipartFile[] files, String extractedZippedFilesLocation) throws LineageBusinessException {
		File newInputDirectory = new File(extractedZippedFilesLocation);
		newInputDirectory.mkdir();
		try {
			for (MultipartFile file : files) {
				if (file.getOriginalFilename().toLowerCase().endsWith("zip")) {
					unzipMultipartFileForAllScripts(file.getInputStream(), newInputDirectory.getAbsolutePath());
				} else {
					Sanitization.sanitizeFileName(file);
					byte[] bytes = file.getBytes();
					Path path = Paths.get(newInputDirectory.getAbsolutePath() + File.separator + file.getOriginalFilename());
					Path canonicalPath = path.normalize();
					Files.write(canonicalPath, bytes);
				}
			}
		} catch (LineageBusinessException e) {
			throw e;
		} catch (Exception ex) {
			LOGGER.error("Exception occurred in uploadAllScriptsToInputLocation ", ex);
		}
	}
	
	public static void unzipMultipartFileForAllScripts(InputStream is, String destinationPath) throws IOException {
		byte[] buffer = new byte[1024];
		ZipInputStream zipInputStream = new ZipInputStream(is);
		ZipEntry zipEntry = zipInputStream.getNextEntry();
		try  {
			while (zipEntry != null) {
				String entryFileNameArr[] = zipEntry.getName().split("/"); 
				String entryFileName = entryFileNameArr[entryFileNameArr.length-1];
				String path = destinationPath + File.separator + entryFileName;
				
				if (!zipEntry.isDirectory()) {
					new File(path).getParentFile().mkdirs();
					try (BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(path))) {
						int bytesRead;
						while ((bytesRead = zipInputStream.read(buffer)) != -1) {
							outputStream.write(buffer, 0, bytesRead);
						}
						outputStream.close();
					}
					if(entryFileName.endsWith(".zip")) {
						File f = new File(path);
						FileInputStream input = new FileInputStream(f);
						unzipMultipartFileForAllScripts(input, destinationPath);
						input.close();
						f.delete();
					}
				} 
	        	zipEntry = zipInputStream.getNextEntry();
	        }
		} catch (Exception ex) {
			LOGGER.error("Exception occurred: ", ex);
		} finally {
			zipInputStream.close();
		}
	}

	public static String prepareStringFromObject(Object object) {
		ObjectMapper mapper = new ObjectMapper();
		try {
            String res= mapper.writeValueAsString(object);
			LOGGER.info("res: "+res);
			return res;
		} catch (JsonProcessingException e) {
			LOGGER.error("Error occurred: ", e);
			throw new RuntimeException("Parsing Error");
		}
	}
	
	public static void uploadAllScriptsToInputLocationForIdmc(MultipartFile[] files, String extractedZippedFilesLocation) throws LineageBusinessException {
		File newInputDirectory = new File(extractedZippedFilesLocation);
		newInputDirectory.mkdir();
		try {
			for (MultipartFile file : files) {
				Sanitization.sanitizeFileName(file);
				byte[] bytes = file.getBytes();
				Path path = Paths.get(newInputDirectory.getAbsolutePath() + "/" + file.getOriginalFilename());
				Path canonicalPath = path.normalize();
				Files.write(canonicalPath, bytes);
			}
		} catch (LineageBusinessException e) {
			throw e;
		} catch (Exception ex) {
			LOGGER.error("Exception occurred in uploadAllScriptsToInputLocation ", ex);
		}
	}
	
	public static void searchZipFileRecursivelyAndUnzip(String directoryName) throws IOException {
        File directory = new File(directoryName);
        File[] fList = directory.listFiles();
        if (fList != null)
            for (File file : fList) {
                if (file.isDirectory()) {
                	searchZipFileRecursivelyAndUnzip(file.getAbsolutePath());
                } else if (file.isFile()) {
                    String ext = getFileExtension(file.getName());
                    if (ext.equals(".zip")) {
                        //System.out.println("Zip file found");
                        String zipFilePath = file.getAbsolutePath();
                        String unZipOutputPath = getFileNameWithoutExtension(zipFilePath); // using apache commons i.o library
                        //System.out.println("unZipOutputPath..."+unZipOutputPath);
                        unzipFile(zipFilePath, unZipOutputPath);
                        searchZipFileRecursivelyAndUnzip(unZipOutputPath); // for nested zip file
                    }
                }
            }
    }

	public static void unzipFile(String zipFilePath, String outputDir) throws IOException {
        byte[] buffer = new byte[1024];
        ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFilePath));
        ZipEntry zipEntry = zis.getNextEntry();
        try {
        	while (zipEntry != null) {
        		File newFile = new File(outputDir + File.separator, zipEntry.getName());
        		if (zipEntry.isDirectory()) {
        			if (!newFile.isDirectory() && !newFile.mkdirs()) {
        				throw new IOException("Failed to create directory " + newFile);
        			}
        		} else {
        			File parent = newFile.getParentFile();
        			if (!parent.isDirectory() && !parent.mkdirs()) {
        				throw new IOException("Failed to create directory " + parent);
        			}
        			FileOutputStream fos = new FileOutputStream(newFile);
        			try {
        				int len = 0;
        				while ((len = zis.read(buffer)) > 0) {
        					fos.write(buffer, 0, len);
        				}
        			} catch (Exception ex) {
        			} finally {
        				fos.close();
        			}
        		}
        		zipEntry = zis.getNextEntry();
        	}
        } catch(Exception ex) {	
        } finally {
        	zis.close();
        }
    }
	
	public static String getFileExtension(String fileName) {
        int lastIndexOf = fileName.lastIndexOf(".");
        if (lastIndexOf == -1) {
            return ""; // empty extension
        }
        return fileName.substring(lastIndexOf).toLowerCase();
    }
	
	public static String getFileNameWithoutExtension(String name) {
        return FilenameUtils.removeExtension(name);
    }
}
