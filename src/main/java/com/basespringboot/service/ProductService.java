package com.basespringboot.service;

import com.basespringboot.dto.ProductDto;
import com.basespringboot.entity.Product;
import java.util.List;

public interface ProductService {
    Product save(ProductDto dto);
    List<Product> findAll();
    Product findById(Long id);
    Product update(Long id, ProductDto dto);
    void delete(Long id);
}