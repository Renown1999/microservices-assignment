package com.sarvika.orderservice.service.impl;

import com.sarvika.orderservice.client.UserClient;
import com.sarvika.orderservice.dto.OrderResponseDTO;
import com.sarvika.orderservice.dto.OrderDTO;
import com.sarvika.orderservice.dto.UserDTO;
import com.sarvika.orderservice.entity.Order;
import com.sarvika.orderservice.repository.OrderRepository;
import com.sarvika.orderservice.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository repository;
    private final UserClient userClient; // Injected UserClient using constructor

    private OrderDTO toDto(Order order) {
        return OrderDTO.builder()
                .id(order.getId())
                .product(order.getProduct())
                .quantity(order.getQuantity())
                .price(order.getPrice())
                .userId(order.getUserId())
                .build();
    }

    private Order toEntity(OrderDTO dto) {
        return Order.builder()
                .id(dto.getId())
                .product(dto.getProduct())
                .quantity(dto.getQuantity())
                .price(dto.getPrice())
                .userId(dto.getUserId())
                .build();
    }

    @Override
    public OrderResponseDTO create(OrderDTO dto) {
        // Validate user
        UserDTO user = userClient.getUserById(dto.getUserId());
        if (user == null) {
            throw new RuntimeException("User not found with ID: " + dto.getUserId());
        }

        // Save order
        Order saved = repository.save(toEntity(dto));

        return OrderResponseDTO.builder()
                .id(saved.getId())
                .product(saved.getProduct())
                .quantity(saved.getQuantity())
                .price(saved.getPrice())
                .user(user)
                .build();
    }

    @Override
    public OrderResponseDTO getById(Long id) {
        Order order = repository.findById(id).orElseThrow();

        UserDTO user = userClient.getUserById(order.getUserId());
        if (user == null) {
            throw new RuntimeException("User not found for order: " + id);
        }

        return OrderResponseDTO.builder()
                .id(order.getId())
                .product(order.getProduct())
                .quantity(order.getQuantity())
                .price(order.getPrice())
                .user(user)
                .build();
    }

    @Override
    public List<OrderDTO> getAll() {
        return repository.findAll().stream().map(this::toDto).collect(Collectors.toList());
    }

    @Override
    public OrderDTO update(Long id, OrderDTO dto) {
        Order order = repository.findById(id).orElseThrow();

        // Optional: Re-validate user again
        userClient.getUserById(dto.getUserId());

        order.setProduct(dto.getProduct());
        order.setQuantity(dto.getQuantity());
        order.setPrice(dto.getPrice());
        order.setUserId(dto.getUserId());

        return toDto(repository.save(order));
    }

    @Override
    public void delete(Long id) {
        repository.deleteById(id);
    }
}
