package com.shopcard.catalog.infrastructure.search.repository;

import com.shopcard.catalog.infrastructure.search.model.ProductIndex;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductIndexRepository extends ElasticsearchRepository<ProductIndex, String> {
    List<ProductIndex> findByNameOrDescription(String name, String description);
    List<ProductIndex> findByShopId(String shopId);
}
