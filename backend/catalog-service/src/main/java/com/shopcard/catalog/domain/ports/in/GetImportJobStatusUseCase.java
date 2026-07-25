package com.shopcard.catalog.domain.ports.in;

import com.shopcard.catalog.domain.model.ImportJob;
import java.util.UUID;

public interface GetImportJobStatusUseCase {
    ImportJob getImportJobStatus(UUID jobId);
}
