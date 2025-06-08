package com.cognizant.lineage.upload.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Objects;

import com.cognizant.lineage.dao.dto.WavePlanDto;
import org.apache.poi.common.usermodel.HyperlinkType;
import org.apache.poi.hssf.usermodel.HSSFDataFormat;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.RegionUtil;
import org.apache.poi.xssf.usermodel.DefaultIndexedColorMap;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFCreationHelper;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFHyperlink;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.cognizant.lineage.upload.constants.GeneralConstants;
import com.cognizant.lineage.upload.dao.GlobalExcelDAO;
import com.cognizant.lineage.upload.model.BusinessObjectCountDetails;
import com.cognizant.lineage.upload.model.BusinessObjectDetails;
import com.cognizant.lineage.upload.model.BusinessObjectNameDetails;
import com.cognizant.lineage.upload.model.ClusterReport;
import com.cognizant.lineage.upload.model.HotSpotDetail;
import com.cognizant.lineage.upload.model.ObjectInventory;
import com.cognizant.lineage.upload.model.PiChartData;
import com.cognizant.lineage.upload.model.PiScriptCount;
import com.cognizant.lineage.upload.model.SpaceAndComplexity;
import com.cognizant.lineage.upload.model.SprintData;

@Service
public class GlobalExcelService {

    @Value("${globalExcelFilePathForLineage}")
    private String globalExcelFilePathForLineage;

    @Autowired
    SprintPlanningService sprintPlanningService;

    @Autowired
    GlobalExcelDAO globalExcelDAO;
    private static final String noDataMessage = "No data available for the specified project";

    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExcelService.class);

    @Async
    public void createGlobalExcel(String projectName, String fileName) {
        LOGGER.info("Cross System Lineage Global Excel creation started for project: {}", projectName);
        try {
            globalExcelDAO.updateGlobalExcelStatusIntoDB(projectName, GeneralConstants.STATUS_IN_PROGRESS);
            XSSFWorkbook workbook = new XSSFWorkbook();
            prepareTableOfContents(workbook, fileName);
            prepareApplicationInventory(workbook, fileName, projectName);
            prepareObjectsInventory(workbook, fileName, projectName);
            prepareClusterReport(workbook, fileName, projectName);
            prepareHotSpot(workbook, fileName, projectName);
            prepareToolRecommendedWavePlan(workbook, fileName, projectName);
            prepareCustomWavePlan(workbook, fileName, projectName);
            prepareBusinessLineageObjectDetails(workbook, fileName, projectName);
            prepareBusinessLineageObjectNameDetails(workbook, fileName, projectName);
            prepareBusinessLineageObjectCount(workbook, fileName, projectName);
            prepareE2EBusinessLineage(workbook, fileName, projectName);
            LOGGER.info("Lineage Global Excel creation completed");
            globalExcelDAO.updateGlobalExcelStatusIntoDB(projectName, GeneralConstants.COMPLETED);
        } catch (Exception ex) {
            globalExcelDAO.updateGlobalExcelStatusIntoDB(projectName, GeneralConstants.ERROR);
            LOGGER.info("Exception occurred while creating the global excel for lineage: " + ex.getMessage());
        }
    }

    private void saveWorkbookToExcel(XSSFWorkbook workbook, String fileName) throws IOException {
        File f = new File(this.globalExcelFilePathForLineage + fileName);
        f.setExecutable(false);
        f.setReadable(true);
        f.setWritable(true);
        FileOutputStream outputStream = new FileOutputStream(f.getAbsolutePath());
        try {
            workbook.write(outputStream);
            outputStream.close();
        } catch (Throwable throwable) {
            try {
                outputStream.close();
            } catch (Throwable throwable1) {
                throwable.addSuppressed(throwable1);
            }
            throw throwable;
        }
    }

    private void prepareTableOfContents(XSSFWorkbook workbook, String fileName)  {

        try {
            LOGGER.info("Lineage Global Excel Table of Contents start");
            XSSFSheet sheet = workbook.createSheet("TOC");

            // TOC (Table Of Contents) Constants
            String Lineage_Report_Inventory = "Lineage Report Inventory";
            String Report_Name = "Report Name";
            String Sheet = "Sheet";
            String technicalLineage = "A) Technical Lineage:";
            String wavePlan = "B) Wave Plan:";
            String businessLineage = "C) Business Lineage:";
            String[] technicalLineageReportNames = new String[]{
                    "1. Applications Inventory", "2. Objects Inventory", "3. Cluster Report", "4. Hot Spot"};
            String[] wavePlanReportNames = new String[]{
                    "1. Tool Recommended Wave Plan", "2. Custom Wave Plan"};
            String[] businessLineageReportNames = new String[]{
                    "1. Business Wise Object Details", "2. Business Wise Object Name", "3. Business Wise Object Count",
                    "4. Business Lineage E2E Report"};
            String[] technicalLineageSheetNames = new String[]{
                    "Applications Inventory", "Objects Inventory", "Cluster Report", "Hot Spot"};
            String[] wavePlanSheetNames = new String[]{
                    "Tool Recommended Wave Plan", "Custom Wave Plan"};
            String[] businessLineageSheetNames = new String[]{
                    "Business Wise Object Details", "Business Wise Object Name", "Business Wise Object Count",
                    "Business Lineage"};

            XSSFCreationHelper helper = workbook.getCreationHelper();

            sheet.setDisplayGridlines(false);
            sheet.setColumnWidth(1, 8000);
            sheet.setColumnWidth(2, 8000);

            XSSFCellStyle titleStyle = getTitleStyle(workbook);
            XSSFCellStyle reportNameStyle = createReportCellStyle(workbook);
            XSSFCellStyle sheetNameStyle = getValueStyle(workbook);
            XSSFCellStyle subHeadingStyle = getSubHeadingStyle(workbook);
            XSSFCellStyle linkStyle = getLinkStyle(workbook);

            XSSFRow xSSFRow1 = sheet.createRow(0);

            sheet.addMergedRegion(CellRangeAddress.valueOf("B1:C1"));

            Cell cell_1_1 = xSSFRow1.createCell(0);
            Cell cell_1_2 = xSSFRow1.createCell(1);
            cell_1_2.setCellValue(Lineage_Report_Inventory);
            cell_1_2.setCellStyle(titleStyle);

            XSSFRow xSSFRow2 = sheet.createRow(1);
            Cell cell_2 = xSSFRow2.createCell(0);
            Cell cell_2_1 = xSSFRow2.createCell(1);
            cell_2_1.setCellValue(Report_Name);
            cell_2_1.setCellStyle(titleStyle);
            Cell cell_2_2 = xSSFRow2.createCell(2);
            cell_2_2.setCellStyle(titleStyle);
            cell_2_2.setCellValue(Sheet);

            int rowNumber = 2;

            XSSFRow xSSFRow3 = sheet.createRow(rowNumber++);

            Cell cell_3_1 = xSSFRow3.createCell(0);
            Cell cell_3_2 = xSSFRow3.createCell(1);
            cell_3_2.setCellValue(technicalLineage);
            cell_3_2.setCellStyle(subHeadingStyle);

            Cell cell_3_3 = xSSFRow3.createCell(2);
            cell_3_3.setCellStyle(sheetNameStyle);

            for (int i = 0; i < technicalLineageReportNames.length; i++) {
                XSSFRow xSSFRow = sheet.createRow(rowNumber++);
                Cell cell1 = xSSFRow.createCell(0);
                Cell cell2 = xSSFRow.createCell(1);
                cell2.setCellValue(technicalLineageReportNames[i]);
                cell2.setCellStyle(reportNameStyle);
                Cell cell_3 = xSSFRow.createCell(2);
                cell_3.setCellValue(technicalLineageSheetNames[i]);
                cell_3.setCellStyle(sheetNameStyle);

                XSSFHyperlink file_link_Inventory = helper.createHyperlink(HyperlinkType.DOCUMENT);
                file_link_Inventory.setAddress("'"+technicalLineageSheetNames[i]+"'!A1");
                cell_3.setHyperlink(file_link_Inventory);
                cell_3.setCellStyle(linkStyle);
            }

            XSSFRow xSSFRow4 = sheet.createRow(rowNumber++);
            Cell cell_4_1 = xSSFRow4.createCell(0);
            Cell cell_4_2 = xSSFRow4.createCell(1);
            cell_4_2.setCellValue(wavePlan);
            cell_4_2.setCellStyle(subHeadingStyle);

            Cell cell_4_3 = xSSFRow4.createCell(2);
            cell_4_3.setCellStyle(sheetNameStyle);

            for (int i = 0; i < wavePlanReportNames.length; i++) {
                XSSFRow xSSFRow = sheet.createRow(rowNumber++);

                Cell cell1 = xSSFRow.createCell(0);
                Cell cell2 = xSSFRow.createCell(1);
                cell2.setCellValue(wavePlanReportNames[i]);
                cell2.setCellStyle(reportNameStyle);

                Cell cell_3 = xSSFRow.createCell(2);
                cell_3.setCellValue(wavePlanSheetNames[i]);
                cell_3.setCellStyle(sheetNameStyle);

                XSSFHyperlink file_link_Inventory = helper.createHyperlink(HyperlinkType.DOCUMENT);
                file_link_Inventory.setAddress("'" + wavePlanSheetNames[i] + "'!A1");
                cell_3.setHyperlink(file_link_Inventory);
                cell_3.setCellStyle(linkStyle);
            }

            XSSFRow xSSFRow5 = sheet.createRow(rowNumber++);

            Cell cell_5_1 = xSSFRow5.createCell(0);
            Cell cell_5_2 = xSSFRow5.createCell(1);
            cell_5_2.setCellValue(businessLineage);
            cell_5_2.setCellStyle(subHeadingStyle);

            Cell cell_5_3 = xSSFRow5.createCell(2);
            cell_5_3.setCellStyle(sheetNameStyle);

            for (int i = 0; i < businessLineageReportNames.length; i++) {
                XSSFRow xSSFRow = sheet.createRow(rowNumber++);

                Cell cell1 = xSSFRow.createCell(0);
                Cell cell2 = xSSFRow.createCell(1);
                cell2.setCellValue(businessLineageReportNames[i]);
                cell2.setCellStyle(reportNameStyle);

                Cell cell_3 = xSSFRow.createCell(2);
                cell_3.setCellValue(businessLineageSheetNames[i]);
                cell_3.setCellStyle(sheetNameStyle);

                XSSFHyperlink file_link_Inventory = helper.createHyperlink(HyperlinkType.DOCUMENT);
                file_link_Inventory.setAddress("'" + businessLineageSheetNames[i] + "'!A1");
                cell_3.setHyperlink(file_link_Inventory);
                cell_3.setCellStyle(linkStyle);
            }

            saveWorkbookToExcel(workbook, fileName);
            LOGGER.info("Lineage Global Excel Table of contents end");
        } catch (IOException ex) {
            LOGGER.info("Exception occurred in creating TOC: " + ex.getMessage());
        }
    }

    private void prepareApplicationInventory(XSSFWorkbook workbook, String fileName, String projectName) {
        try {
            LOGGER.info("Lineage Global Excel Application Inventory start");
            // Application Inventory Constants
            String applicationInventory = "Applications Inventory";
            String applicationType = "Application Type";
            String technology = "Technology";
            String countOfScripts = "Count of Scripts";
            String etlAndEltApplications = "ETL & ELT Applications";
            String analytics = "Analytics";

            PiChartData applicationInventoryData = getApplicationInventoryData(projectName);

            XSSFSheet sheet = workbook.createSheet(applicationInventory);
            XSSFCellStyle titleStyle = getTitleStyle(workbook);
            XSSFCellStyle valueStyle = getValueStyle(workbook);

            if (applicationInventoryData.getReportPiChartDetails().isEmpty() ||
                    applicationInventoryData.getTechPiChartDetails().isEmpty()) {
                XSSFRow xSSFRow1 = sheet.createRow(0);
                Cell cell_1_1 = xSSFRow1.createCell(0);
                cell_1_1.setCellValue(noDataMessage);
                cell_1_1.setCellStyle(valueStyle);
                sheet.autoSizeColumn(0);
                saveWorkbookToExcel(workbook, fileName);
                LOGGER.info("Lineage Global Excel Application Inventory end as there is no data present for this project");
                return;
            }

            sheet.addMergedRegion(CellRangeAddress.valueOf("A1:C1"));
            sheet.setColumnWidth(0, 8000);
            sheet.setColumnWidth(1, 8000);
            sheet.setColumnWidth(2, 8000);

            // Below parenthesis added to keep a block of code at one place for better readability
            {
                XSSFRow xSSFRow1 = sheet.createRow(0);
                Cell cell_1_1 = xSSFRow1.createCell(0);
                cell_1_1.setCellValue(applicationInventory);
                cell_1_1.setCellStyle(titleStyle);

                XSSFRow xSSFRow2 = sheet.createRow(1);
                Cell cell_2_1 = xSSFRow2.createCell(0);
                cell_2_1.setCellValue(applicationType);
                cell_2_1.setCellStyle(titleStyle);

                Cell cell_2_2 = xSSFRow2.createCell(1);
                cell_2_2.setCellValue(technology);
                cell_2_2.setCellStyle(titleStyle);

                Cell cell_2_3 = xSSFRow2.createCell(2);
                cell_2_3.setCellValue(countOfScripts);
                cell_2_3.setCellStyle(titleStyle);
            }
            XSSFRow xSSFRow3 = sheet.createRow(2);
            Cell cell_3_1 = xSSFRow3.createCell(0);
            cell_3_1.setCellValue(etlAndEltApplications);
            cell_3_1.setCellStyle(valueStyle);

            boolean isRowCreated = true;

            int firstRow = 2;
            int lastRow = 2 + applicationInventoryData.getTechPiChartDetails().size() - 1;
            int firstColumn = 0;
            int lastColumn = 0;

            CellRangeAddress rangeAddress1 = new CellRangeAddress(firstRow, lastRow, firstColumn, lastColumn);
            sheet.addMergedRegion(rangeAddress1);

            List<PiScriptCount> etlData = applicationInventoryData.getTechPiChartDetails();
            List<PiScriptCount> analyticsData = applicationInventoryData.getReportPiChartDetails();

            XSSFRow xSSFRow = xSSFRow3;
            for (int i = 0; i < etlData.size(); i++) {
                if (!isRowCreated) {
                    xSSFRow = sheet.createRow(firstRow + i);
                }
                if (isRowCreated) {
                    isRowCreated = false;
                }
                Cell cell_2 = xSSFRow.createCell(1);
                cell_2.setCellValue(etlData.get(i).getTechnology());
                cell_2.setCellStyle(valueStyle);

                Cell cell_3 = xSSFRow.createCell(2);
                cell_3.setCellValue(Double.parseDouble(String.valueOf(etlData.get(i).getScriptCount())));
                cell_3.setCellStyle(valueStyle);
            }

            firstRow = lastRow + 1;
            lastRow = firstRow + analyticsData.size() - 1;

            XSSFRow xSSFRow4 = sheet.createRow(firstRow);
            Cell cell_4_1 = xSSFRow4.createCell(0);
            cell_4_1.setCellValue(analytics);
            cell_4_1.setCellStyle(valueStyle);

            CellRangeAddress rangeAddress2 = new CellRangeAddress(firstRow, lastRow, firstColumn, lastColumn);
            sheet.addMergedRegion(rangeAddress2);

            isRowCreated = true;
            xSSFRow = xSSFRow4;
            for (int i = 0; i < analyticsData.size(); i++) {
                if (!isRowCreated) {
                    xSSFRow = sheet.createRow(firstRow + i);
                }
                if (isRowCreated) {
                    isRowCreated = false;
                }
                Cell cell_2 = xSSFRow.createCell(1);
                cell_2.setCellValue(analyticsData.get(i).getTechnology());
                cell_2.setCellStyle(valueStyle);

                Cell cell_3 = xSSFRow.createCell(2);
                cell_3.setCellValue(Double.parseDouble(String.valueOf(analyticsData.get(i).getScriptCount())));
                cell_3.setCellStyle(valueStyle);
            }

            setBorderForMergedRegion(sheet, rangeAddress1);
            setBorderForMergedRegion(sheet, rangeAddress2);
            saveWorkbookToExcel(workbook, fileName);
            LOGGER.info("Lineage Global Excel Application Inventory end");
        } catch (Exception ex) {
            LOGGER.info("Exception occurred while creating Application Inventory sheet: " + ex.getMessage());
        }
    }

    private void prepareObjectsInventory(XSSFWorkbook workbook, String fileName, String projectName) {
        try {
            LOGGER.info("Lineage Global Excel Objects Inventory start");
            // Objects Inventory Constants
            String objectsInventory = "Objects Inventory";
            String objectType = "Object Type";
            String countOfObjects = "Count of Objects";

            List<ObjectInventory> inventoryList = globalExcelDAO.getObjectsInventoryDetails(projectName);

            XSSFSheet sheet = workbook.createSheet(objectsInventory);
            XSSFCellStyle titleStyle = getTitleStyle(workbook);
            XSSFCellStyle valueStyle = getValueStyle(workbook);

            if (inventoryList.isEmpty()) {
                XSSFRow xSSFRow1 = sheet.createRow(0);
                Cell cell_1_1 = xSSFRow1.createCell(0);
                cell_1_1.setCellValue(noDataMessage);
                cell_1_1.setCellStyle(valueStyle);
                sheet.autoSizeColumn(0);
                saveWorkbookToExcel(workbook, fileName);
                LOGGER.info("Lineage Global Excel Objects Inventory end as there is no data present for this project");
                return;
            }

            sheet.addMergedRegion(CellRangeAddress.valueOf("A1:B1"));
            sheet.setColumnWidth(0, 6000);
            sheet.setColumnWidth(1, 6000);
            // Below parenthesis added to keep a block of code at one place for better readability
            {
                XSSFRow xSSFRow1 = sheet.createRow(0);
                Cell cell_1_1 = xSSFRow1.createCell(0);
                cell_1_1.setCellValue(objectsInventory);
                cell_1_1.setCellStyle(titleStyle);

                XSSFRow xSSFRow2 = sheet.createRow(1);
                Cell cell_2_1 = xSSFRow2.createCell(0);
                cell_2_1.setCellValue(objectType);
                cell_2_1.setCellStyle(titleStyle);

                Cell cell_2_2 = xSSFRow2.createCell(1);
                cell_2_2.setCellValue(countOfObjects);
                cell_2_2.setCellStyle(titleStyle);
            }
            int rowCounter = 2;
            for (ObjectInventory inventory : inventoryList) {

                XSSFRow xSSFRow = sheet.createRow(rowCounter++);

                Cell cell_1 = xSSFRow.createCell(0);
                cell_1.setCellValue(inventory.getNodeType());
                cell_1.setCellStyle(valueStyle);

                Cell cell_2 = xSSFRow.createCell(1);
                cell_2.setCellValue(inventory.getCountOfObjects());
                cell_2.setCellStyle(valueStyle);
            }
            saveWorkbookToExcel(workbook, fileName);
            LOGGER.info("Lineage Global Excel Objects Inventory end");
        } catch (Exception ex) {
            LOGGER.info("Exception occurred while preparing objects inventory sheet: {}", ex.getMessage());
        }
    }

    private void prepareClusterReport(XSSFWorkbook workbook, String fileName, String projectName) {
        try {
            LOGGER.info("Lineage Global Excel Cluster Report start");
            // Cluster Report constants
            String clusterReport = "Cluster Report";
            String islandId = "Island id";
            String sourceObject = "Source Object";
            String targetObject = "Target Object";
            String scriptName = "Script Name";
            String scriptType = "Script Type";
            String statementType = "Statement Type";

            List<ClusterReport> reportList = globalExcelDAO.getClusterReportDetails(projectName);

            XSSFSheet sheet = workbook.createSheet(clusterReport);
            XSSFCellStyle titleStyle = getTitleStyle(workbook);
            XSSFCellStyle valueStyle = getValueStyle(workbook);

            if (reportList.isEmpty()) {
                XSSFRow xSSFRow1 = sheet.createRow(0);
                Cell cell_1_1 = xSSFRow1.createCell(0);
                cell_1_1.setCellValue(noDataMessage);
                cell_1_1.setCellStyle(valueStyle);
                sheet.autoSizeColumn(0);
                saveWorkbookToExcel(workbook, fileName);
                LOGGER.info("Lineage Global Excel Cluster Report end as there is no data present for this project");
                return;
            }

            // Below parenthesis added to keep a block of code at one place for better readability
            {
                XSSFRow xSSFRow1 = sheet.createRow(0);
                Cell cell_1_1 = xSSFRow1.createCell(0);
                cell_1_1.setCellValue(islandId);
                cell_1_1.setCellStyle(titleStyle);

                Cell cell_1_2 = xSSFRow1.createCell(1);
                cell_1_2.setCellValue(sourceObject);
                cell_1_2.setCellStyle(titleStyle);

                Cell cell_1_3 = xSSFRow1.createCell(2);
                cell_1_3.setCellValue(targetObject);
                cell_1_3.setCellStyle(titleStyle);

                Cell cell_1_4 = xSSFRow1.createCell(3);
                cell_1_4.setCellValue(scriptName);
                cell_1_4.setCellStyle(titleStyle);

                Cell cell_1_5 = xSSFRow1.createCell(4);
                cell_1_5.setCellValue(scriptType);
                cell_1_5.setCellStyle(titleStyle);

                Cell cell_1_6 = xSSFRow1.createCell(5);
                cell_1_6.setCellValue(statementType);
                cell_1_6.setCellStyle(titleStyle);
            }
            int rowCounter = 1;

            for (ClusterReport report : reportList) {
                XSSFRow xSSFRow = sheet.createRow(rowCounter++);

                Cell cell_1 = xSSFRow.createCell(0);
                cell_1.setCellValue(report.getIslandId());
                cell_1.setCellStyle(valueStyle);

                Cell cell_2 = xSSFRow.createCell(1);
                cell_2.setCellValue(report.getSourceObject());
                cell_2.setCellStyle(valueStyle);

                Cell cell_3 = xSSFRow.createCell(2);
                cell_3.setCellValue(report.getTargetObject());
                cell_3.setCellStyle(valueStyle);

                Cell cell_4 = xSSFRow.createCell(3);
                cell_4.setCellValue(report.getScriptName());
                cell_4.setCellStyle(valueStyle);

                Cell cell_5 = xSSFRow.createCell(4);
                cell_5.setCellValue(report.getScriptType());
                cell_5.setCellStyle(valueStyle);

                Cell cell_6 = xSSFRow.createCell(5);
                cell_6.setCellValue(report.getStatementType());
                cell_6.setCellStyle(valueStyle);
            }

            sheet.autoSizeColumn(0);
            sheet.autoSizeColumn(1);
            sheet.autoSizeColumn(2);
            sheet.autoSizeColumn(3);
            sheet.autoSizeColumn(4);
            sheet.autoSizeColumn(5);

            saveWorkbookToExcel(workbook, fileName);
            LOGGER.info("Lineage Global Excel Cluster Report end");
        } catch (Exception ex) {
            LOGGER.info("Exception occurred while preparing cluster report sheet: {}", ex.getMessage());
        }
    }

    private void prepareHotSpot(XSSFWorkbook workbook, String fileName, String projectName) {
        try {
            LOGGER.info("Lineage Global Excel Hot Spot Report start");
            // Hot spot constants
            String hotSpot = "Hot Spot";
            String projectNameHeading = "Project Name";
            String nodeName = "Node Name";
            String incomingEdges = "Incoming Edges";
            String outgoingEdges = "Outgoing Edges";
            String degree = "Degree";

            List<HotSpotDetail> details = globalExcelDAO.getHotSpotDetails(projectName);

            XSSFSheet sheet = workbook.createSheet(hotSpot);
            XSSFCellStyle titleStyle = getTitleStyle(workbook);
            XSSFCellStyle valueStyle = getValueStyle(workbook);

            if (details.isEmpty()) {
                XSSFRow xSSFRow1 = sheet.createRow(0);
                Cell cell_1_1 = xSSFRow1.createCell(0);
                cell_1_1.setCellValue(noDataMessage);
                cell_1_1.setCellStyle(valueStyle);
                sheet.autoSizeColumn(0);
                saveWorkbookToExcel(workbook, fileName);
                LOGGER.info("Lineage Global Excel Hot Spot Report end as there is no data present for this project");
                return;
            }

            // Below parenthesis added to keep a block of code at one place for better readability
            {
                XSSFRow xSSFRow1 = sheet.createRow(0);
                Cell cell_1_1 = xSSFRow1.createCell(0);
                cell_1_1.setCellValue(projectNameHeading);
                cell_1_1.setCellStyle(titleStyle);

                Cell cell_1_2 = xSSFRow1.createCell(1);
                cell_1_2.setCellValue(nodeName);
                cell_1_2.setCellStyle(titleStyle);

                Cell cell_1_3 = xSSFRow1.createCell(2);
                cell_1_3.setCellValue(incomingEdges);
                cell_1_3.setCellStyle(titleStyle);

                Cell cell_1_4 = xSSFRow1.createCell(3);
                cell_1_4.setCellValue(outgoingEdges);
                cell_1_4.setCellStyle(titleStyle);

                Cell cell_1_5 = xSSFRow1.createCell(4);
                cell_1_5.setCellValue(degree);
                cell_1_5.setCellStyle(titleStyle);
            }

            int rowCounter = 1;
            for (HotSpotDetail detail : details) {
                XSSFRow xSSFRow = sheet.createRow(rowCounter++);

                Cell cell_1 = xSSFRow.createCell(0);
                cell_1.setCellValue(detail.getProjectName());
                cell_1.setCellStyle(valueStyle);

                Cell cell_2 = xSSFRow.createCell(1);
                cell_2.setCellValue(detail.getNodeName());
                cell_2.setCellStyle(valueStyle);

                Cell cell_3 = xSSFRow.createCell(2);
                cell_3.setCellValue(detail.getIncomingEdges());
                cell_3.setCellStyle(valueStyle);

                Cell cell_4 = xSSFRow.createCell(3);
                cell_4.setCellValue(detail.getOutgoingEdges());
                cell_4.setCellStyle(valueStyle);

                Cell cell_5 = xSSFRow.createCell(4);
                cell_5.setCellValue(detail.getDegree());
                cell_5.setCellStyle(valueStyle);
            }

            sheet.autoSizeColumn(0);
            sheet.autoSizeColumn(1);
            sheet.autoSizeColumn(2);
            sheet.autoSizeColumn(3);
            sheet.autoSizeColumn(4);

            saveWorkbookToExcel(workbook, fileName);
            LOGGER.info("Lineage Global Excel Hot Spot Report end");
        } catch (Exception ex) {
            LOGGER.info("Exception occurred while preparing hot spot sheet: {}", ex.getMessage());
        }
    }

    private void prepareToolRecommendedWavePlan(XSSFWorkbook workbook, String fileName, String projectName) {
        try {
            LOGGER.info("Lineage Global Excel Tool Recommended Wave Plan Report start");
            // Tool Recommended Wave Plan constants
            String wavePlan = "Tool Recommended Wave Plan";
            String projectNameHeading = "Project Name";
            String wave = "Wave";
            String waveSprint = "Objects";
            String sprint = "Sprint";
            String migrationType = "Migration Type";
            String scriptType = "Script Type";
            String nodeType = "Node Type";
            String startDate = "Start Date";
            String endDate = "End Date";
            String duration = "Duration";
            String size = "Size (MB)";
            String complexity = "Complexity";
            String status = "Status";

            List<WavePlanDto> details = getToolRecommendedWavePlan(projectName);

            XSSFSheet sheet = workbook.createSheet(wavePlan);
            XSSFCellStyle titleStyle = getTitleStyle(workbook);
            XSSFCellStyle valueStyle = getValueStyle(workbook);

            if (details.isEmpty()) {
                XSSFRow xSSFRow1 = sheet.createRow(0);
                Cell cell_1_1 = xSSFRow1.createCell(0);
                cell_1_1.setCellValue(noDataMessage);
                cell_1_1.setCellStyle(valueStyle);
                sheet.autoSizeColumn(0);
                saveWorkbookToExcel(workbook, fileName);
                LOGGER.info("Lineage Global Excel Tool Recommended Wave Plan Report end as there is no data present for this project");
                return;
            }

            // Below parenthesis added to keep a block of code at one place for better readability
            {
                XSSFRow xSSFRow1 = sheet.createRow(0);
                Cell cell_1_1 = xSSFRow1.createCell(0);
                cell_1_1.setCellValue(projectNameHeading);
                cell_1_1.setCellStyle(titleStyle);

                Cell cell_1_2 = xSSFRow1.createCell(1);
                cell_1_2.setCellValue(wave);
                cell_1_2.setCellStyle(titleStyle);

                Cell cell_1_3 = xSSFRow1.createCell(2);
                cell_1_3.setCellValue(waveSprint);
                cell_1_3.setCellStyle(titleStyle);

                Cell cell_1_4 = xSSFRow1.createCell(3);
                cell_1_4.setCellValue(sprint);
                cell_1_4.setCellStyle(titleStyle);

                Cell cell_1_5 = xSSFRow1.createCell(4);
                cell_1_5.setCellValue(migrationType);
                cell_1_5.setCellStyle(titleStyle);

                Cell cell_1_6 = xSSFRow1.createCell(5);
                cell_1_6.setCellValue(scriptType);
                cell_1_6.setCellStyle(titleStyle);

                Cell cell_1_7 = xSSFRow1.createCell(6);
                cell_1_7.setCellValue(nodeType);
                cell_1_7.setCellStyle(titleStyle);

                Cell cell_1_8 = xSSFRow1.createCell(7);
                cell_1_8.setCellValue(startDate);
                cell_1_8.setCellStyle(titleStyle);

                Cell cell_1_9 = xSSFRow1.createCell(8);
                cell_1_9.setCellValue(endDate);
                cell_1_9.setCellStyle(titleStyle);

                Cell cell_1_10 = xSSFRow1.createCell(9);
                cell_1_10.setCellValue(duration);
                cell_1_10.setCellStyle(titleStyle);

                Cell cell_1_11 = xSSFRow1.createCell(10);
                cell_1_11.setCellValue(size);
                cell_1_11.setCellStyle(titleStyle);

                Cell cell_1_12 = xSSFRow1.createCell(11);
                cell_1_12.setCellValue(complexity);
                cell_1_12.setCellStyle(titleStyle);

                Cell cell_1_13 = xSSFRow1.createCell(12);
                cell_1_13.setCellValue(status);
                cell_1_13.setCellStyle(titleStyle);
            }

            int rowCounter = 1;
            for (WavePlanDto detail: details) {
                XSSFRow xSSFRow = sheet.createRow(rowCounter++);

                Cell cell_1 = xSSFRow.createCell(0);
                cell_1.setCellValue(detail.getProjectName());
                cell_1.setCellStyle(valueStyle);

                Cell cell_2 = xSSFRow.createCell(1);
                cell_2.setCellValue(detail.getWave());
                cell_2.setCellStyle(valueStyle);

                Cell cell_3 = xSSFRow.createCell(2);
                cell_3.setCellValue(detail.getWaveSprint());
                cell_3.setCellStyle(valueStyle);

                Cell cell_4 = xSSFRow.createCell(3);
                cell_4.setCellValue(detail.getSprint());
                cell_4.setCellStyle(valueStyle);

                Cell cell_5 = xSSFRow.createCell(4);
                cell_5.setCellValue(detail.getMigrationType());
                cell_5.setCellStyle(valueStyle);

                Cell cell_6 = xSSFRow.createCell(5);
                cell_6.setCellValue(detail.getScriptType());
                cell_6.setCellStyle(valueStyle);

                Cell cell_7 = xSSFRow.createCell(6);
                cell_7.setCellValue(detail.getNodeType());
                cell_7.setCellStyle(valueStyle);

                Cell cell_8 = xSSFRow.createCell(7);
                cell_8.setCellValue(
                        Objects.isNull(detail.getStartDate()) ? "" : detail.getStartDate().toString());
                cell_8.setCellStyle(valueStyle);

                Cell cell_9 = xSSFRow.createCell(8);
                cell_9.setCellValue(
                        Objects.isNull(detail.getEndDate()) ? "" : detail.getEndDate().toString());
                cell_9.setCellStyle(valueStyle);

                Cell cell_10 = xSSFRow.createCell(9);
                cell_10.setCellValue(detail.getDuration());
                cell_10.setCellStyle(valueStyle);

                Cell cell_11 = xSSFRow.createCell(10);
                cell_11.setCellValue(detail.getSize());
                cell_11.setCellStyle(valueStyle);

                Cell cell_12 = xSSFRow.createCell(11);
                cell_12.setCellValue(detail.getComplexity());
                cell_12.setCellStyle(valueStyle);

                Cell cell_13 = xSSFRow.createCell(12);
                cell_13.setCellValue(detail.getStatus());
                cell_13.setCellStyle(valueStyle);
            }

            for (int i = 0; i < 13; i++) {
                sheet.autoSizeColumn(i);
            }

            CellRangeAddress rangeAddress = new CellRangeAddress(1, sheet.getLastRowNum(), 0, 0);
            sheet.addMergedRegion(rangeAddress);

            saveWorkbookToExcel(workbook, fileName);
            LOGGER.info("Lineage Global ExcelTool Recommended Wave Plan Report end");
        } catch (Exception ex) {
            LOGGER.error("Exception occurred while preparing Tool Recommended Wave Plan sheet: ", ex);
        }
    }

    private List<WavePlanDto> getToolRecommendedWavePlan(String projectName) {
        return sprintPlanningService.getToolRecommendedWavePlan(projectName);
    }

    private void prepareCustomWavePlan(XSSFWorkbook workbook, String fileName, String projectName) {
        try {
            LOGGER.info("Lineage Global Excel Custom Wave Plan Report start");
            // Custom Wave Plan constants
            String wavePlan = "Custom Wave Plan";
            String projectNameHeading = "Project Name";
            String wave = "Wave";
            String waveSprint = "Objects";
            String sprint = "Sprint";
            String migrationType = "Migration Type";
            String scriptType = "Script Type";
            String nodeType = "Node Type";
            String startDate = "Start Date";
            String endDate = "End Date";
            String duration = "Duration";
            String size = "Size (MB)";
            String complexity = "Complexity";
            String status = "Status";

            List<WavePlanDto> details = getCustomWavePlan(projectName);

            XSSFSheet sheet = workbook.createSheet(wavePlan);
            XSSFCellStyle titleStyle = getTitleStyle(workbook);
            XSSFCellStyle valueStyle = getValueStyle(workbook);

            if (details.isEmpty()) {
                XSSFRow xSSFRow1 = sheet.createRow(0);
                Cell cell_1 = xSSFRow1.createCell(0);
                cell_1.setCellValue(noDataMessage);
                cell_1.setCellStyle(valueStyle);
                sheet.autoSizeColumn(0);
                saveWorkbookToExcel(workbook, fileName);
                LOGGER.info("Lineage Global Excel Custom Wave Plan Report end as there is no data present for this project");
                return;
            }
            // Below parenthesis added to keep a block of code at one place for better readability
            {
                XSSFRow xSSFRow1 = sheet.createRow(0);
                Cell cell_1_1 = xSSFRow1.createCell(0);
                cell_1_1.setCellValue(projectNameHeading);
                cell_1_1.setCellStyle(titleStyle);

                Cell cell_1_2 = xSSFRow1.createCell(1);
                cell_1_2.setCellValue(wave);
                cell_1_2.setCellStyle(titleStyle);

                Cell cell_1_3 = xSSFRow1.createCell(2);
                cell_1_3.setCellValue(waveSprint);
                cell_1_3.setCellStyle(titleStyle);

                Cell cell_1_4 = xSSFRow1.createCell(3);
                cell_1_4.setCellValue(sprint);
                cell_1_4.setCellStyle(titleStyle);

                Cell cell_1_5 = xSSFRow1.createCell(4);
                cell_1_5.setCellValue(migrationType);
                cell_1_5.setCellStyle(titleStyle);

                Cell cell_1_6 = xSSFRow1.createCell(5);
                cell_1_6.setCellValue(scriptType);
                cell_1_6.setCellStyle(titleStyle);

                Cell cell_1_7 = xSSFRow1.createCell(6);
                cell_1_7.setCellValue(nodeType);
                cell_1_7.setCellStyle(titleStyle);

                Cell cell_1_8 = xSSFRow1.createCell(7);
                cell_1_8.setCellValue(startDate);
                cell_1_8.setCellStyle(titleStyle);

                Cell cell_1_9 = xSSFRow1.createCell(8);
                cell_1_9.setCellValue(endDate);
                cell_1_9.setCellStyle(titleStyle);

                Cell cell_1_10 = xSSFRow1.createCell(9);
                cell_1_10.setCellValue(duration);
                cell_1_10.setCellStyle(titleStyle);

                Cell cell_1_11 = xSSFRow1.createCell(10);
                cell_1_11.setCellValue(size);
                cell_1_11.setCellStyle(titleStyle);

                Cell cell_1_12 = xSSFRow1.createCell(11);
                cell_1_12.setCellValue(complexity);
                cell_1_12.setCellStyle(titleStyle);

                Cell cell_1_13 = xSSFRow1.createCell(12);
                cell_1_13.setCellValue(status);
                cell_1_13.setCellStyle(titleStyle);
            }

            int rowCounter = 1;
            for (WavePlanDto detail: details) {
                XSSFRow xSSFRow = sheet.createRow(rowCounter++);

                Cell cell_1 = xSSFRow.createCell(0);
                cell_1.setCellValue(detail.getProjectName());
                cell_1.setCellStyle(valueStyle);

                Cell cell_2 = xSSFRow.createCell(1);
                cell_2.setCellValue(detail.getWave());
                cell_2.setCellStyle(valueStyle);

                Cell cell_3 = xSSFRow.createCell(2);
                cell_3.setCellValue(detail.getWaveSprint());
                cell_3.setCellStyle(valueStyle);

                Cell cell_4 = xSSFRow.createCell(3);
                cell_4.setCellValue(detail.getSprint());
                cell_4.setCellStyle(valueStyle);

                Cell cell_5 = xSSFRow.createCell(4);
                cell_5.setCellValue(detail.getMigrationType());
                cell_5.setCellStyle(valueStyle);

                Cell cell_6 = xSSFRow.createCell(5);
                cell_6.setCellValue(detail.getScriptType());
                cell_6.setCellStyle(valueStyle);

                Cell cell_7 = xSSFRow.createCell(6);
                cell_7.setCellValue(detail.getNodeType());
                cell_7.setCellStyle(valueStyle);

                Cell cell_8 = xSSFRow.createCell(7);
                cell_8.setCellValue(
                        Objects.isNull(detail.getStartDate()) ? "" : detail.getStartDate().toString());
                cell_8.setCellStyle(valueStyle);

                Cell cell_9 = xSSFRow.createCell(8);
                cell_9.setCellValue(
                        Objects.isNull(detail.getEndDate()) ? "" : detail.getEndDate().toString());
                cell_9.setCellStyle(valueStyle);

                Cell cell_10 = xSSFRow.createCell(9);
                cell_10.setCellValue(detail.getDuration());
                cell_10.setCellStyle(valueStyle);

                Cell cell_11 = xSSFRow.createCell(10);
                cell_11.setCellValue(detail.getSize());
                cell_11.setCellStyle(valueStyle);

                Cell cell_12 = xSSFRow.createCell(11);
                cell_12.setCellValue(detail.getComplexity());
                cell_12.setCellStyle(valueStyle);

                Cell cell_13 = xSSFRow.createCell(12);
                cell_13.setCellValue(detail.getStatus());
                cell_13.setCellStyle(valueStyle);
            }

            for (int i = 0; i < 13; i++) {
                sheet.autoSizeColumn(i);
            }

            CellRangeAddress rangeAddress = new CellRangeAddress(1, sheet.getLastRowNum(), 0, 0);
            sheet.addMergedRegion(rangeAddress);

            saveWorkbookToExcel(workbook, fileName);
            LOGGER.info("Lineage Global Excel Custom Wave Plan Report end");
        } catch (Exception ex) {
            LOGGER.error("Exception occurred while preparing Custom Wave Plan sheet: ", ex);
        }
    }

    public List<WavePlanDto> getCustomWavePlan(String projectName) {
        return sprintPlanningService.getCustomWavePlan(projectName);
    }

    private void prepareBusinessLineageObjectDetails(XSSFWorkbook workbook, String fileName, String projectName) {
        try {
            LOGGER.info("Lineage Global Excel Business Wise Object Report start");
            // Business Lineage Object Details constants
            String businessWiseObjectDetails = "Business Wise Object Details";
            String projectNameHeading = "Project Name";
            String module = "Module";
            String nodeName = "Node Name (Object Name)";
            String nodeType = "Node Type";
            String sizeMB = "Size (MB)";
            String complexity = "Complexity";

            XSSFSheet sheet = workbook.createSheet(businessWiseObjectDetails);
            XSSFCellStyle titleStyle = getTitleStyle(workbook);
            XSSFCellStyle valueStyle = getValueStyle(workbook);

            List<BusinessObjectDetails> details = globalExcelDAO.getBusinessObjectDetails(projectName);

            if (details.isEmpty()) {
                XSSFRow xSSFRow1 = sheet.createRow(0);
                Cell cell_1 = xSSFRow1.createCell(0);
                cell_1.setCellValue(noDataMessage);
                cell_1.setCellStyle(valueStyle);
                sheet.autoSizeColumn(0);
                saveWorkbookToExcel(workbook, fileName);
                LOGGER.info("Lineage Global Excel Business Wise Object Report end as there is no data present for this project");
                return;
            }
            // Below parenthesis added to keep a block of code at one place for better readability
            {
                XSSFRow xSSFRow1 = sheet.createRow(0);
                Cell cell_1_1 = xSSFRow1.createCell(0);
                cell_1_1.setCellValue(projectNameHeading);
                cell_1_1.setCellStyle(titleStyle);

                Cell cell_1_2 = xSSFRow1.createCell(1);
                cell_1_2.setCellValue(module);
                cell_1_2.setCellStyle(titleStyle);

                Cell cell_1_3 = xSSFRow1.createCell(2);
                cell_1_3.setCellValue(nodeName);
                cell_1_3.setCellStyle(titleStyle);

                Cell cell_1_4 = xSSFRow1.createCell(3);
                cell_1_4.setCellValue(nodeType);
                cell_1_4.setCellStyle(titleStyle);

                Cell cell_1_5 = xSSFRow1.createCell(4);
                cell_1_5.setCellValue(sizeMB);
                cell_1_5.setCellStyle(titleStyle);

                Cell cell_1_6 = xSSFRow1.createCell(5);
                cell_1_6.setCellValue(complexity);
                cell_1_6.setCellStyle(titleStyle);
            }

            int rowCounter = 1;
            for (BusinessObjectDetails detail : details) {
                String objectType = globalExcelDAO.getObjectTypeForObjectDetails(detail.getNodeName());
                SpaceAndComplexity spaceAndCompObject = globalExcelDAO.getSizeAndComplexityForObjectDetails(detail.getNodeName());
                String space = "";
                String complexityValue = "";
                if (Objects.isNull(objectType) ||
                        !"TABLE".equalsIgnoreCase(objectType) || Objects.isNull(spaceAndCompObject)) {
                    space = "N/A";
                    complexityValue = "N/A";
                } else {
                    space = spaceAndCompObject.getSpace();
                    complexityValue = spaceAndCompObject.getComplexity();
                }
                XSSFRow xSSFRow = sheet.createRow(rowCounter++);

                Cell cell_1 = xSSFRow.createCell(0);
                cell_1.setCellValue(projectName);
                cell_1.setCellStyle(valueStyle);

                Cell cell_2 = xSSFRow.createCell(1);
                cell_2.setCellValue(detail.getModuleName());
                cell_2.setCellStyle(valueStyle);

                Cell cell_3 = xSSFRow.createCell(2);
                cell_3.setCellValue(detail.getNodeName());
                cell_3.setCellStyle(valueStyle);

                Cell cell_4 = xSSFRow.createCell(3);
                cell_4.setCellValue(Objects.isNull(objectType) ? detail.getNodeType() : objectType);
                cell_4.setCellStyle(valueStyle);

                Cell cell_5 = xSSFRow.createCell(4);
                cell_5.setCellValue(space);
                cell_5.setCellStyle(valueStyle);

                Cell cell_6 = xSSFRow.createCell(5);
                cell_6.setCellValue(complexityValue);
                cell_6.setCellStyle(valueStyle);
            }
            CellRangeAddress rangeAddress = new CellRangeAddress(
                    1, sheet.getLastRowNum(), 0, 0);
            sheet.addMergedRegion(rangeAddress);

            mergeSameNameRowsInAColumn(sheet, 1);
            for (int i = 0; i < sheet.getRow(0).getPhysicalNumberOfCells(); i++) {
                sheet.autoSizeColumn(i);
            }
            saveWorkbookToExcel(workbook, fileName);
            LOGGER.info("Lineage Global Excel Business Wise Object Report end");
        } catch (Exception ex) {
            LOGGER.error("Exception occurred while preparing business lineage object details sheet: ", ex);
        }
    }

    private void prepareBusinessLineageObjectNameDetails(XSSFWorkbook workbook, String fileName, String projectName) {
        try {
            LOGGER.info("Lineage Global Excel Business Wise Object Name details Report start");
            // Business Lineage Object Name details constants
            String businessLineageObjectName = "Business Wise Object Name";
            String projectNameHeading = "Project Name";
            String sourceModule = "Source Module";
            String targetModule = "Target Module";
            String tableName = "Table Name";
            String viewName = "View Name";
            String materializedViewName = "Materialized View Name";
            String userDefinedFunctionName = "User Defined Function Name";
            String procedureName = "Procedure Name";
            String triggerName = "Trigger Name";
            String jobName = "Job Name";

            XSSFSheet sheet = workbook.createSheet(businessLineageObjectName);
            XSSFCellStyle titleStyle = getTitleStyle(workbook);
            XSSFCellStyle valueStyle = getValueStyle(workbook);

            List<BusinessObjectNameDetails> details = globalExcelDAO.getBusinessObjectNameDetails(projectName);

            if (details.isEmpty()) {
                XSSFRow xSSFRow = sheet.createRow(0);
                Cell cell_1 = xSSFRow.createCell(0);
                cell_1.setCellValue(noDataMessage);
                cell_1.setCellStyle(valueStyle);
                sheet.autoSizeColumn(0);
                saveWorkbookToExcel(workbook, fileName);
                LOGGER.info("Lineage Global Excel Business Wise Object Name details Report end as there is no data present for this project");
                return;
            }
            // Below parenthesis added to keep a block of code at one place for better readability
            {
                XSSFRow xSSFRow = sheet.createRow(0);
                Cell cell = xSSFRow.createCell(0);
                cell.setCellValue(projectNameHeading);
                cell.setCellStyle(titleStyle);

                Cell cell_2 = xSSFRow.createCell(1);
                cell_2.setCellValue(sourceModule);
                cell_2.setCellStyle(titleStyle);

                Cell cell_3 = xSSFRow.createCell(2);
                cell_3.setCellValue(targetModule);
                cell_3.setCellStyle(titleStyle);

                Cell cell_4 = xSSFRow.createCell(3);
                cell_4.setCellValue(tableName);
                cell_4.setCellStyle(titleStyle);

                Cell cell_5 = xSSFRow.createCell(4);
                cell_5.setCellValue(viewName);
                cell_5.setCellStyle(titleStyle);

                Cell cell_6 = xSSFRow.createCell(5);
                cell_6.setCellValue(materializedViewName);
                cell_6.setCellStyle(titleStyle);

                Cell cell_7 = xSSFRow.createCell(6);
                cell_7.setCellValue(userDefinedFunctionName);
                cell_7.setCellStyle(titleStyle);

                Cell cell_8 = xSSFRow.createCell(7);
                cell_8.setCellValue(procedureName);
                cell_8.setCellStyle(titleStyle);

                Cell cell_9 = xSSFRow.createCell(8);
                cell_9.setCellValue(triggerName);
                cell_9.setCellStyle(titleStyle);

                Cell cell_10 = xSSFRow.createCell(9);
                cell_10.setCellValue(jobName);
                cell_10.setCellStyle(titleStyle);
            }

            int rowCounter = 1;
            for (BusinessObjectNameDetails detail : details) {
                XSSFRow xSSFRow = sheet.createRow(rowCounter++);

                Cell cell = xSSFRow.createCell(0);
                cell.setCellValue(detail.getProjectName());
                cell.setCellStyle(valueStyle);

                Cell cell_2 = xSSFRow.createCell(1);
                cell_2.setCellValue(detail.getSourceModule());
                cell_2.setCellStyle(valueStyle);

                Cell cell_3 = xSSFRow.createCell(2);
                cell_3.setCellValue(detail.getTargetModule());
                cell_3.setCellStyle(valueStyle);

                Cell cell_4 = xSSFRow.createCell(3);
                cell_4.setCellValue(detail.getTableName());
                cell_4.setCellStyle(valueStyle);

                Cell cell_5 = xSSFRow.createCell(4);
                cell_5.setCellValue(detail.getViewName());
                cell_5.setCellStyle(valueStyle);

                Cell cell_6 = xSSFRow.createCell(5);
                cell_6.setCellValue(detail.getMaterializedViewName());
                cell_6.setCellStyle(valueStyle);

                Cell cell_7 = xSSFRow.createCell(6);
                cell_7.setCellValue(detail.getUserDefinedFunctionName());
                cell_7.setCellStyle(valueStyle);

                Cell cell_8 = xSSFRow.createCell(7);
                cell_8.setCellValue(detail.getProcedureName());
                cell_8.setCellStyle(valueStyle);

                Cell cell_9 = xSSFRow.createCell(8);
                cell_9.setCellValue(detail.getTriggerName());
                cell_9.setCellStyle(valueStyle);

                Cell cell_10 = xSSFRow.createCell(9);
                cell_10.setCellValue(detail.getJobName());
                cell_10.setCellStyle(valueStyle);
            }

            CellRangeAddress rangeAddress = new CellRangeAddress(
                    1, sheet.getLastRowNum(), 0, 0);
            sheet.addMergedRegion(rangeAddress);

            mergeSameNameRowsInAColumn(sheet, 1);
            for (int i = 0; i < sheet.getRow(0).getPhysicalNumberOfCells(); i++) {
                sheet.autoSizeColumn(i);
            }
            saveWorkbookToExcel(workbook, fileName);
            LOGGER.info("Lineage Global Excel Business Wise Object Name details Report end");
        } catch (Exception ex) {
            LOGGER.error("Exception occurred while preparing business lineage object name details sheet: ", ex);
        }
    }

    private void prepareBusinessLineageObjectCount(XSSFWorkbook workbook, String fileName, String projectName) {
        try {
            LOGGER.info("Lineage Global Excel Business Wise Object Count details Report start");
            // Business Lineage Object Count constants
            String businessLineageObjectCount = "Business Wise Object Count";
            String projectNameHeading = "Project Name";
            String sourceModule = "Source Module";
            String targetModule = "Target Module";
            String tableCount = "Table count";
            String viewCount = "View count";
            String materializedViewCount = "Materialized View count";
            String userDefinedFunctionCount = "User Defined Function count";
            String procedureCount = "Procedure count";
            String triggerCount = "Trigger count";
            String jobCount = "Join count";

            XSSFSheet sheet = workbook.createSheet(businessLineageObjectCount);
            XSSFCellStyle titleStyle = getTitleStyle(workbook);
            XSSFCellStyle valueStyle = getValueStyle(workbook);

            List<BusinessObjectCountDetails> details = globalExcelDAO.getBusinessObjectCountDetails(projectName);

            if (details.isEmpty()) {
                XSSFRow xSSFRow = sheet.createRow(0);
                Cell cell_1 = xSSFRow.createCell(0);
                cell_1.setCellValue(noDataMessage);
                cell_1.setCellStyle(valueStyle);
                sheet.autoSizeColumn(0);
                saveWorkbookToExcel(workbook, fileName);
                LOGGER.info("Lineage Global Excel Business Wise Object Count details Report end as there is no data present for this project");
                return;
            }

            // Below parenthesis added to keep a block of code at one place for better readability
            {
                XSSFRow xSSFRow = sheet.createRow(0);
                Cell cell = xSSFRow.createCell(0);
                cell.setCellValue(projectNameHeading);
                cell.setCellStyle(titleStyle);

                Cell cell_2 = xSSFRow.createCell(1);
                cell_2.setCellValue(sourceModule);
                cell_2.setCellStyle(titleStyle);

                Cell cell_3 = xSSFRow.createCell(2);
                cell_3.setCellValue(targetModule);
                cell_3.setCellStyle(titleStyle);

                Cell cell_4 = xSSFRow.createCell(3);
                cell_4.setCellValue(tableCount);
                cell_4.setCellStyle(titleStyle);

                Cell cell_5 = xSSFRow.createCell(4);
                cell_5.setCellValue(viewCount);
                cell_5.setCellStyle(titleStyle);

                Cell cell_6 = xSSFRow.createCell(5);
                cell_6.setCellValue(materializedViewCount);
                cell_6.setCellStyle(titleStyle);

                Cell cell_7 = xSSFRow.createCell(6);
                cell_7.setCellValue(userDefinedFunctionCount);
                cell_7.setCellStyle(titleStyle);

                Cell cell_8 = xSSFRow.createCell(7);
                cell_8.setCellValue(procedureCount);
                cell_8.setCellStyle(titleStyle);

                Cell cell_9 = xSSFRow.createCell(8);
                cell_9.setCellValue(triggerCount);
                cell_9.setCellStyle(titleStyle);

                Cell cell_10 = xSSFRow.createCell(9);
                cell_10.setCellValue(jobCount);
                cell_10.setCellStyle(titleStyle);
            }

            int rowCounter = 1;
            for (BusinessObjectCountDetails detail : details) {
                XSSFRow xSSFRow = sheet.createRow(rowCounter++);

                Cell cell = xSSFRow.createCell(0);
                cell.setCellValue(detail.getProjectName());
                cell.setCellStyle(valueStyle);

                Cell cell_2 = xSSFRow.createCell(1);
                cell_2.setCellValue(detail.getSourceModule());
                cell_2.setCellStyle(valueStyle);

                Cell cell_3 = xSSFRow.createCell(2);
                cell_3.setCellValue(detail.getTargetModule());
                cell_3.setCellStyle(valueStyle);

                Cell cell_4 = xSSFRow.createCell(3);
                cell_4.setCellValue(detail.getTableCount());
                cell_4.setCellStyle(valueStyle);

                Cell cell_5 = xSSFRow.createCell(4);
                cell_5.setCellValue(detail.getViewCount());
                cell_5.setCellStyle(valueStyle);

                Cell cell_6 = xSSFRow.createCell(5);
                cell_6.setCellValue(detail.getMaterializedViewCount());
                cell_6.setCellStyle(valueStyle);

                Cell cell_7 = xSSFRow.createCell(6);
                cell_7.setCellValue(detail.getUserDefinedFunctionCount());
                cell_7.setCellStyle(valueStyle);

                Cell cell_8 = xSSFRow.createCell(7);
                cell_8.setCellValue(detail.getProcedureCount());
                cell_8.setCellStyle(valueStyle);

                Cell cell_9 = xSSFRow.createCell(8);
                cell_9.setCellValue(detail.getTriggerCount());
                cell_9.setCellStyle(valueStyle);

                Cell cell_10 = xSSFRow.createCell(9);
                cell_10.setCellValue(detail.getJobCount());
                cell_10.setCellStyle(valueStyle);
            }
            CellRangeAddress rangeAddress = new CellRangeAddress(
                    1, sheet.getLastRowNum(), 0, 0);
            sheet.addMergedRegion(rangeAddress);

            mergeSameNameRowsInAColumn(sheet, 1);
            for (int i = 0; i < sheet.getRow(0).getPhysicalNumberOfCells(); i++) {
                sheet.autoSizeColumn(i);
            }
            saveWorkbookToExcel(workbook, fileName);
            LOGGER.info("Lineage Global Excel Business Wise Object Count details Report end");
        } catch (Exception ex) {
            LOGGER.error("Exception occurred while preparing business lineage object count details sheet: ", ex);
        }
    }

    private void prepareE2EBusinessLineage(XSSFWorkbook workbook, String fileName, String projectName) {
        try {
            LOGGER.info("Lineage Global Excel E2E Business Lineage Report start");
            // E2E Business Lineage Constants
            String businessLineage = "Business Lineage";
            String businessLevel = "Business_Level";

            XSSFSheet sheet = workbook.createSheet(businessLineage);
            XSSFCellStyle titleStyle = getTitleStyle(workbook);
            XSSFCellStyle valueStyle = getValueStyle(workbook);

            Integer maxNoOfColumns = globalExcelDAO.getMaxNoOfBusinessLevels(projectName);

            if (Objects.isNull(maxNoOfColumns)) {
                XSSFRow xSSFRow1 = sheet.createRow(0);
                Cell cell_1 = xSSFRow1.createCell(0);
                cell_1.setCellValue(noDataMessage);
                cell_1.setCellStyle(valueStyle);
                sheet.autoSizeColumn(0);
                saveWorkbookToExcel(workbook, fileName);
                LOGGER.info("Lineage Global Excel E2E Business Lineage Report end as there is no data present for this project");
                return;
            }

            List<String> e2eList = globalExcelDAO.getE2EBusinessLineageDetails(projectName);
            XSSFRow xSSFRow1 = sheet.createRow(0);
            for (int i = 0; i < maxNoOfColumns + 1; i++) {
                Cell cell_1 = xSSFRow1.createCell(i);
                cell_1.setCellValue(businessLevel.concat(String.valueOf(i + 1)));
                cell_1.setCellStyle(titleStyle);
            }
            int rowCounter = 1;
            for (String str : e2eList) {
                XSSFRow xSSFRow = sheet.createRow(rowCounter++);
                String[] strArray = str.split(",");
                for (int i = 0; i < maxNoOfColumns + 1; i++) {
                    Cell cell = xSSFRow.createCell(i);
                    cell.setCellValue(i >= strArray.length ? null : strArray[i]);
                    cell.setCellStyle(valueStyle);
                }
            }
            for (int i = 0; i < maxNoOfColumns + 1; i++) {
                sheet.autoSizeColumn(i);
            }
            saveWorkbookToExcel(workbook, fileName);
            LOGGER.info("Lineage Global Excel E2E Business Lineage Report end");
        } catch (Exception ex) {
            LOGGER.info("Exception occurred while preparing E2E business lineage sheet: {}", ex.getMessage());
        }
    }

    public String getStatusOfGlobalExcel(String projectName) {
        try {
            return globalExcelDAO.getStatusOfGlobalExcel(projectName);
        } catch (Exception ex) {
            LOGGER.info("Exception occurred while getting status of global excel: {}", ex.getMessage());
        }
        return "";
    }

    public String deleteGlobalExcelExistingFile(String projectName, String fileName) {
        try {
            File f = new File(this.globalExcelFilePathForLineage + fileName);
            f.setExecutable(false);
            f.setReadable(true);
            f.setWritable(true);
            if (!f.exists()) {
                return deleteExistingGlobalExcelRecordFromDB(projectName);
            }
            LOGGER.info("Deleting existing global excel file for project: {}", projectName);
            if (!f.delete()) {
                LOGGER.info("File is not able to delete. Please try again after some time.");
                return GeneralConstants.FAILED;
            }
            LOGGER.info("Existing global excel file is deleted.");
            LOGGER.info("Deleting existing global excel status record for project: {}", projectName);
            return deleteExistingGlobalExcelRecordFromDB(projectName);
        } catch (Exception ex) {
            LOGGER.info("Exception occurred while deleting existing global excel file for project: {}, error: {}",
                    projectName, ex.getMessage());
            return GeneralConstants.FAILED;
        }
    }

    public String deleteExistingGlobalExcelRecordFromDB(String projectName) {
        Integer result = globalExcelDAO.deleteGlobalExcelStatusForProject(projectName);
        if (result >= 0) {
            LOGGER.info("Deleted existing global excel status record for project: {}", projectName);
            globalExcelDAO.insertGlobalExcelStatusIntoDB(projectName, GeneralConstants.STARTED);
            return GeneralConstants.SUCCESS;
        }
        return GeneralConstants.FAILED;
    }

    public PiChartData getApplicationInventoryData(String projectName) {
        SprintData sprintData = new SprintData();
        sprintData.setProjectName(projectName);
        return sprintPlanningService.getPieChartDetailsByProjectNameAndModule(sprintData);
    }

    public XSSFCellStyle getTitleStyle(XSSFWorkbook workbook) {

        XSSFColor myColor = new XSSFColor(new java.awt.Color(49, 134, 155),
                new DefaultIndexedColorMap());

        XSSFFont xSSFFont1 = workbook.createFont();
        xSSFFont1.setColor(IndexedColors.WHITE.getIndex());
        xSSFFont1.setUnderline((byte) 1);

        XSSFCellStyle titleStyle = workbook.createCellStyle();
        titleStyle.setFillForegroundColor(myColor);
        titleStyle.setFillPattern(FillPatternType.forInt((short) 1));
        titleStyle.setFont(xSSFFont1);
        titleStyle.setAlignment(HorizontalAlignment.forInt((short) 2));
        titleStyle.setBorderLeft(BorderStyle.THIN);
        titleStyle.setBorderRight(BorderStyle.THIN);
        titleStyle.setBorderTop(BorderStyle.THIN);
        titleStyle.setBorderBottom(BorderStyle.THIN);

        return titleStyle;
    }

    public XSSFCellStyle getValueStyle(XSSFWorkbook workbook) {
        XSSFFont xSSFFont3 = workbook.createFont();
        xSSFFont3.setColor(IndexedColors.BLACK.getIndex());

        XSSFCellStyle valueStyle = workbook.createCellStyle();
        valueStyle.setWrapText(true);
        valueStyle.setFont(xSSFFont3);
        valueStyle.setAlignment(HorizontalAlignment.forInt((short) 1));
        valueStyle.setBorderLeft(BorderStyle.THIN);
        valueStyle.setBorderRight(BorderStyle.THIN);
        valueStyle.setBorderTop(BorderStyle.THIN);
        valueStyle.setBorderBottom(BorderStyle.THIN);
        valueStyle.setVerticalAlignment(VerticalAlignment.CENTER);

        return valueStyle;
    }

    public XSSFCellStyle createReportCellStyle(XSSFWorkbook workbook) {
        XSSFColor myColor1 = new XSSFColor(new java.awt.Color(146, 205, 220), new DefaultIndexedColorMap());
        XSSFFont xSSFFont2 = workbook.createFont();
        xSSFFont2.setColor(IndexedColors.BLACK.getIndex());

        XSSFCellStyle reportNameStyle = workbook.createCellStyle();
        reportNameStyle.setFillForegroundColor(myColor1);
        reportNameStyle.setFillPattern(FillPatternType.forInt((short) 1));
        reportNameStyle.setFont(xSSFFont2);
        reportNameStyle.setAlignment(HorizontalAlignment.forInt((short) 1));
        reportNameStyle.setBorderLeft(BorderStyle.THIN);
        reportNameStyle.setBorderRight(BorderStyle.THIN);
        reportNameStyle.setBorderTop(BorderStyle.THIN);
        reportNameStyle.setBorderBottom(BorderStyle.THIN);
        reportNameStyle.setVerticalAlignment(VerticalAlignment.CENTER);

        return reportNameStyle;
    }

    public XSSFCellStyle getSubHeadingStyle(XSSFWorkbook workbook) {
        XSSFFont xSSFFont4 = workbook.createFont();
        xSSFFont4.setColor(IndexedColors.BLACK.getIndex());
        xSSFFont4.setBold(true);

        XSSFCellStyle subHeadingStyle = workbook.createCellStyle();
        subHeadingStyle.setFont(xSSFFont4);
        subHeadingStyle.setBorderLeft(BorderStyle.THIN);
        subHeadingStyle.setBorderRight(BorderStyle.THIN);
        subHeadingStyle.setBorderTop(BorderStyle.THIN);
        subHeadingStyle.setBorderBottom(BorderStyle.THIN);
        subHeadingStyle.setDataFormat(HSSFDataFormat.getBuiltinFormat("#,##0"));
        subHeadingStyle.setAlignment(HorizontalAlignment.LEFT);
        subHeadingStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        return subHeadingStyle;
    }

    public XSSFCellStyle getLinkStyle(XSSFWorkbook workbook) {
        XSSFFont xSSFFont5 = workbook.createFont();
        xSSFFont5.setUnderline((byte) 1);
        xSSFFont5.setColor((short) 54);

        XSSFCellStyle linkStyle = workbook.createCellStyle();
        linkStyle.setFont(xSSFFont5);
        linkStyle.setAlignment(HorizontalAlignment.LEFT);
        linkStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        linkStyle.setBorderLeft(BorderStyle.THIN);
        linkStyle.setBorderRight(BorderStyle.THIN);
        linkStyle.setBorderTop(BorderStyle.THIN);
        linkStyle.setBorderBottom(BorderStyle.THIN);

        return linkStyle;
    }

    public void setBorderForMergedRegion(Sheet sheet, CellRangeAddress rangeAddress) {
        RegionUtil.setBorderTop(BorderStyle.THIN, rangeAddress, sheet);
        RegionUtil.setBorderLeft(BorderStyle.THIN, rangeAddress, sheet);
        RegionUtil.setBorderRight(BorderStyle.THIN, rangeAddress, sheet);
        RegionUtil.setBorderBottom(BorderStyle.THIN, rangeAddress, sheet);
    }

    public void mergeSameNameRowsInAColumn(Sheet sheet, Integer columnNo) {
        int maxRow = sheet.getLastRowNum();
        Row firstRow = sheet.getRow(1);
        String previousRowValue = firstRow.getCell(columnNo).getStringCellValue();
        int previousRowNumber = 1;
        for (int i = 2; i <= maxRow; i++) {
            String presentRowValue = sheet.getRow(i).getCell(columnNo).getStringCellValue();
            if (!previousRowValue.equalsIgnoreCase(presentRowValue)) {
                if (i - 1 != previousRowNumber) {
                    CellRangeAddress rangeAddress = new CellRangeAddress(
                            previousRowNumber, i - 1, columnNo, columnNo);
                    sheet.addMergedRegion(rangeAddress);
                }
                previousRowNumber = i;
                previousRowValue = presentRowValue;
            }
        }
        if (previousRowNumber != maxRow) {
            CellRangeAddress rangeAddress = new CellRangeAddress(
                    previousRowNumber, maxRow, columnNo, columnNo);
            sheet.addMergedRegion(rangeAddress);
        }
    }
}
