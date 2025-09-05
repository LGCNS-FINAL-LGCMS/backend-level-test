package com.lgcms.leveltest.common.kafka.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryEvent {
    private String categoryName;
    private String key;
}
