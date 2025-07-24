package com.sarvika.orderservice.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderDTO {
    private Long id;
    private String product;
    private Integer quantity;
    private Double price;
    private Long userId;
}
