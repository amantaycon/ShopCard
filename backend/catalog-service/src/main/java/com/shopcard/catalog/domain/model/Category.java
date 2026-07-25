package com.shopcard.catalog.domain.model;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Category {
    private Integer id;
    private String name;
    private String slug;
    private Category parent;
}
