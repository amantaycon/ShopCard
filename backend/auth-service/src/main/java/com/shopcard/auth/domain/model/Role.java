package com.shopcard.auth.domain.model;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Role {
    private Integer id;
    private String name;
}
