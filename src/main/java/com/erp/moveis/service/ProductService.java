package com.erp.moveis.service;

import com.erp.moveis.model.Product;
import com.erp.moveis.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProductService {

    @Autowired
    private ProductRepository repository;

    @Cacheable("products")
    public List<Product> list() {
        return repository.findAll();
    }

    public Page<Product> listPaged(Pageable pageable) {
        return repository.findAll(pageable);
    }

    public Optional<Product> findById(Long id) {
        return repository.findById(id);
    }

    @CacheEvict(value = "products", allEntries = true)
    public Product save(Product product) {
        return repository.save(product);
    }

    @CacheEvict(value = "products", allEntries = true)
    public void delete(Long id) {
        repository.deleteById(id);
    }

    @CacheEvict(value = "products", allEntries = true)
    public Product update(Long id, Product productDetails) {
        Optional<Product> product = repository.findById(id);
        if (product.isPresent()) {
            Product existingProduct = product.get();
            if (productDetails.getName() != null) {
                existingProduct.setName(productDetails.getName());
            }
            if (productDetails.getMaterial() != null) {
                existingProduct.setMaterial(productDetails.getMaterial());
            }
            if (productDetails.getColor() != null) {
                existingProduct.setColor(productDetails.getColor());
            }
            if (productDetails.getBasePrice() != null) {
                existingProduct.setBasePrice(productDetails.getBasePrice());
            }
            return repository.save(existingProduct);
        }
        return null;
    }
}