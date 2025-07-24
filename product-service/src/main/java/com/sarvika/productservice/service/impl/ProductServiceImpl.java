package com.sarvika.productservice.service.impl;

import com.sarvika.productservice.dto.ProductDTO;
import com.sarvika.productservice.entity.Product;
import com.sarvika.productservice.repository.ProductRepository;
import com.sarvika.productservice.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository repository;

    @Override
    public ProductDTO create(ProductDTO dto) {
        Product product = new Product();
        BeanUtils.copyProperties(dto, product);
        return convertToDTO(repository.save(product));
    }

    @Override
    public ProductDTO update(String id, ProductDTO dto) {
        Product product = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        BeanUtils.copyProperties(dto, product, "id");
        return convertToDTO(repository.save(product));
    }

    @Override
    public ProductDTO getById(String id) {
        return repository.findById(id)
                .map(this::convertToDTO)
                .orElseThrow(() -> new RuntimeException("Product not found"));
    }

    @Override
    public List<ProductDTO> getAll() {
        return repository.findAll()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public void delete(String id) {
        repository.deleteById(id);
    }

    private ProductDTO convertToDTO(Product product) {
        ProductDTO dto = new ProductDTO();
        BeanUtils.copyProperties(product, dto);
        return dto;
    }
}
