package com.sarvika.productservice.service;

import com.sarvika.productservice.dto.ProductDTO;
import java.util.List;

public interface ProductService {
    ProductDTO create(ProductDTO dto);
    ProductDTO update(String id, ProductDTO dto);
    ProductDTO getById(String id);
    List<ProductDTO> getAll();
    void delete(String id);
}
