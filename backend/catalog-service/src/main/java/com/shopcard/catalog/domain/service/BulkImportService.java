package com.shopcard.catalog.domain.service;

import com.shopcard.catalog.domain.model.ImportJob;
import com.shopcard.catalog.domain.ports.in.BulkImportUseCase;
import com.shopcard.catalog.domain.ports.in.CreateProductUseCase;
import com.shopcard.catalog.domain.ports.in.GetImportJobStatusUseCase;
import com.shopcard.catalog.domain.ports.in.ProductCommand;
import com.shopcard.catalog.domain.ports.out.ImportJobRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
public class BulkImportService implements BulkImportUseCase, GetImportJobStatusUseCase {

    private final ImportJobRepositoryPort importJobRepositoryPort;
    private final CreateProductUseCase createProductUseCase;

    @Override
    public ImportJob startImport(UUID shopId, String fileName, InputStream inputStream) {
        ImportJob job = ImportJob.builder()
                .shopId(shopId)
                .fileName(fileName)
                .status("PENDING")
                .build();

        ImportJob savedJob = importJobRepositoryPort.save(job);

        // Run async import processing
        CompletableFuture.runAsync(() -> {
            processBulkImport(savedJob.getId(), inputStream, fileName);
        });

        return savedJob;
    }

    @Override
    public ImportJob getImportJobStatus(UUID jobId) {
        return importJobRepositoryPort.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Job not found"));
    }

    public void processBulkImport(UUID jobId, InputStream fileStream, String fileType) {
        ImportJob job = importJobRepositoryPort.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Job not found"));

        job.setStatus("PROCESSING");
        importJobRepositoryPort.save(job);

        int total = 0;
        int processed = 0;
        int failed = 0;
        StringBuilder errorLog = new StringBuilder();

        try {
            if (fileType.endsWith("csv")) {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(fileStream, StandardCharsets.UTF_8))) {
                    String line;
                    boolean isHeader = true;
                    while ((line = reader.readLine()) != null) {
                        if (isHeader) {
                            isHeader = false; // skip headers
                            continue;
                        }
                        total++;
                        String[] parts = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)"); // Split ignoring commas in quotes
                        try {
                            if (parts.length < 4) {
                                throw new Exception("Incomplete row values");
                            }
                            String name = parts[0].replace("\"", "").trim();
                            String description = parts.length > 1 ? parts[1].replace("\"", "").trim() : "";
                            String sku = parts[2].replace("\"", "").trim();
                            BigDecimal price = new BigDecimal(parts[3].replace("\"", "").trim());
                            String categoryName = parts.length > 4 ? parts[4].replace("\"", "").trim() : "General";
                            
                            ProductCommand cmdMapped = new ProductCommand(name, description, sku, price, null, categoryName, true);

                            createProductUseCase.createProduct(job.getShopId(), cmdMapped);
                            processed++;
                        } catch (Exception e) {
                            failed++;
                            errorLog.append(String.format("Row %d Error: %s\n", total, e.getMessage()));
                        }

                        // Update progress in batches
                        if (total % 20 == 0) {
                            job.setTotalRecords(total);
                            job.setProcessedRecords(processed);
                            job.setFailedRecords(failed);
                            importJobRepositoryPort.save(job);
                        }
                    }
                }
            } else { // Excel processing
                try (Workbook workbook = new XSSFWorkbook(fileStream)) {
                    Sheet sheet = workbook.getSheetAt(0);
                    for (int i = 1; i <= sheet.getLastRowNum(); i++) { // Skip header row at 0
                        Row row = sheet.getRow(i);
                        if (row == null) continue;
                        total++;

                        try {
                            String name = getCellValueAsString(row.getCell(0));
                            String description = getCellValueAsString(row.getCell(1));
                            String sku = getCellValueAsString(row.getCell(2));
                            BigDecimal price = new BigDecimal(getCellValueAsString(row.getCell(3)));
                            String categoryName = getCellValueAsString(row.getCell(4));
                            
                            ProductCommand cmdMapped = new ProductCommand(name, description, sku, price, null, categoryName, true);

                            createProductUseCase.createProduct(job.getShopId(), cmdMapped);
                            processed++;
                        } catch (Exception e) {
                            failed++;
                            errorLog.append(String.format("Row %d Error: %s\n", total, e.getMessage()));
                        }

                        if (total % 20 == 0) {
                            job.setTotalRecords(total);
                            job.setProcessedRecords(processed);
                            job.setFailedRecords(failed);
                            importJobRepositoryPort.save(job);
                        }
                    }
                }
            }

            job.setStatus(failed > 0 ? "COMPLETED_WITH_ERRORS" : "COMPLETED");
            job.setErrorLog(errorLog.toString());
        } catch (Exception e) {
            job.setStatus("FAILED");
            job.setErrorLog("Import process crashed: " + e.getMessage());
        } finally {
            job.setTotalRecords(total);
            job.setProcessedRecords(processed);
            job.setFailedRecords(failed);
            importJobRepositoryPort.save(job);
        }
    }

    private String getCellValueAsString(Cell cell) {
        if (cell == null) return "";
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                }
                return BigDecimal.valueOf(cell.getNumericCellValue()).toPlainString();
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            default:
                return "";
        }
    }
}
