package com.sarvika.orderservice.service;

import com.sarvika.orderservice.dto.OrderDTO;
import com.sarvika.orderservice.dto.OrderResponseDTO;
import java.util.List;

public interface OrderService {
   // OrderDTO create(OrderDTO dto);
   // OrderDTO getById(Long id);
    OrderResponseDTO create(OrderDTO dto);
    OrderResponseDTO getById(Long id);
    List<OrderDTO> getAll();
    OrderDTO update(Long id, OrderDTO dto);
    void delete(Long id);
}
