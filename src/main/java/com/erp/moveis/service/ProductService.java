package com.erp.moveis.service;

import com.erp.moveis.model.Product;
import com.erp.moveis.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProductService {

    @Autowired
    private ProductRepository repository;

    public List<Product> list() {
        return repository.findAll();
    }

    public Optional<Product> findById(Long id) {
        return repository.findById(id);
    }

    public Product save(Product product) {
        return repository.save(product);
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }

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