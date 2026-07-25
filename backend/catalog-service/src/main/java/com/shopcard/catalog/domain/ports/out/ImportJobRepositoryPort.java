package com.shopcard.catalog.domain.ports.out;

import com.shopcard.catalog.domain.model.ImportJob;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ImportJobRepositoryPort {
    ImportJob save(ImportJob job);
    Optional<ImportJob> findById(UUID id);
    List<ImportJob> findByShopId(UUID shopId);
}
