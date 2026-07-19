package com.kawai.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class CartItemDto {
    private Long id;
    private Integer qty;
    private BigDecimal price;
    private String note;
}
