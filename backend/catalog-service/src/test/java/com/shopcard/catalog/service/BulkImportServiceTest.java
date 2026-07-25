package com.shopcard.catalog.service;

import com.shopcard.catalog.domain.model.ImportJob;
import com.shopcard.catalog.domain.ports.in.CreateProductUseCase;
import com.shopcard.catalog.domain.ports.in.ProductCommand;
import com.shopcard.catalog.domain.ports.out.ImportJobRepositoryPort;
import com.shopcard.catalog.domain.service.BulkImportService;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BulkImportServiceTest {

    @Mock
    private ImportJobRepositoryPort importJobRepositoryPort;

    @Mock
    private CreateProductUseCase createProductUseCase;

    @InjectMocks
    private BulkImportService bulkImportService;

    @Test
    void processBulkImport_csvFile_shouldParseRowsAndSaveProducts() throws Exception {
        UUID jobId = UUID.randomUUID();
        UUID shopId = UUID.randomUUID();
        ImportJob job = ImportJob.builder()
                .id(jobId)
                .shopId(shopId)
                .status("PENDING")
                .build();

        String csvData = "Name,Description,SKU,Price,Category\n" +
                "CSV Product,CSV Description,CSV-SKU,12.99,CSV Category\n";
        ByteArrayInputStream csvStream = new ByteArrayInputStream(csvData.getBytes(StandardCharsets.UTF_8));

        when(importJobRepositoryPort.findById(jobId)).thenReturn(Optional.of(job));
        when(importJobRepositoryPort.save(any(ImportJob.class))).thenAnswer(invocation -> invocation.getArgument(0));

        bulkImportService.processBulkImport(jobId, csvStream, "products.csv");

        verify(createProductUseCase).createProduct(eq(shopId), eq(new ProductCommand(
                "CSV Product",
                "CSV Description",
                "CSV-SKU",
                BigDecimal.valueOf(12.99),
                null,
                "CSV Category",
                true
        )));

        assertThat(job.getStatus()).isEqualTo("COMPLETED");
        assertThat(job.getTotalRecords()).isEqualTo(1);
        assertThat(job.getProcessedRecords()).isEqualTo(1);
        assertThat(job.getFailedRecords()).isZero();
    }

    @Test
    void processBulkImport_excelFile_shouldParseRowsAndSaveProducts() throws Exception {
        UUID jobId = UUID.randomUUID();
        UUID shopId = UUID.randomUUID();
        ImportJob job = ImportJob.builder()
                .id(jobId)
                .shopId(shopId)
                .status("PENDING")
                .build();

        // Create in-memory Excel file
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet();
        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("Name");
        header.createCell(1).setCellValue("Description");
        header.createCell(2).setCellValue("SKU");
        header.createCell(3).setCellValue("Price");
        header.createCell(4).setCellValue("Category");

        Row row = sheet.createRow(1);
        row.createCell(0).setCellValue("Excel Product");
        row.createCell(1).setCellValue("Excel Description");
        row.createCell(2).setCellValue("EX-SKU");
        row.createCell(3).setCellValue(24.50);
        row.createCell(4).setCellValue("Excel Category");

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        workbook.write(out);
        ByteArrayInputStream excelStream = new ByteArrayInputStream(out.toByteArray());
        workbook.close();

        when(importJobRepositoryPort.findById(jobId)).thenReturn(Optional.of(job));
        when(importJobRepositoryPort.save(any(ImportJob.class))).thenAnswer(invocation -> invocation.getArgument(0));

        bulkImportService.processBulkImport(jobId, excelStream, "products.xlsx");

        verify(createProductUseCase).createProduct(eq(shopId), eq(new ProductCommand(
                "Excel Product",
                "Excel Description",
                "EX-SKU",
                BigDecimal.valueOf(24.5),
                null,
                "Excel Category",
                true
        )));

        assertThat(job.getStatus()).isEqualTo("COMPLETED");
        assertThat(job.getTotalRecords()).isEqualTo(1);
        assertThat(job.getProcessedRecords()).isEqualTo(1);
        assertThat(job.getFailedRecords()).isZero();
    }
}
