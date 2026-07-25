package com.shopcard.catalog.domain.ports.in;

import com.shopcard.catalog.domain.model.ImportJob;
import java.io.InputStream;
import java.util.UUID;

public interface BulkImportUseCase {
    ImportJob startImport(UUID shopId, String fileName, InputStream inputStream);
}
