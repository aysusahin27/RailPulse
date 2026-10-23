package com.basespringboot.service;

import com.basespringboot.dto.ProductDto;
import com.basespringboot.entity.Product;
import com.basespringboot.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;

    @Override
    public Product save(ProductDto dto) {
        Product p = new Product();
        p.setName(dto.getName());
        return productRepository.save(p);
    }

    @Override
    public List<Product> findAll() {
        return productRepository.findAll();
    }

    @Override
    public Product findById(Long id) {
        return productRepository.findById(id).orElseThrow();
    }

    @Override
    public Product update(Long id, ProductDto dto) {
        Product p = findById(id);
        p.setName(dto.getName());
        return productRepository.save(p);
    }

    @Override
    public void delete(Long id) {
        productRepository.deleteById(id);
    }
}