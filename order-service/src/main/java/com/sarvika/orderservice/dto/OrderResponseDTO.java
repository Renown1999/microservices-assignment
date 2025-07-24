package com.sarvika.orderservice.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderResponseDTO {
    private Long id;
    private String product;
    private Integer quantity;
    private Double price;
    private UserDTO user;
}
