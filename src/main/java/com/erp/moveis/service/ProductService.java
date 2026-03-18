package com.erp.moveis.service;

import com.erp.moveis.core.tenant.TenantContext;
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

    @Cacheable(value = "products", key = "T(com.erp.moveis.core.tenant.TenantContext).getTenantId()")
    public List<Product> list() {
        return repository.findByCompanyId(TenantContext.requireTenantId());
    }

    public Page<Product> listPaged(Pageable pageable) {
        return repository.findByCompanyId(TenantContext.requireTenantId(), pageable);
    }

    public Optional<Product> findById(Long id) {
        return repository.findByIdAndCompanyId(id, TenantContext.requireTenantId());
    }

    @CacheEvict(value = "products", allEntries = true)
    public Product save(Product product) {
        product.setCompanyId(TenantContext.requireTenantId());
        return repository.save(product);
    }

    @CacheEvict(value = "products", allEntries = true)
    public void delete(Long id) {
        repository.findByIdAndCompanyId(id, TenantContext.requireTenantId())
                .ifPresent(p -> repository.deleteById(p.getId()));
    }

    @CacheEvict(value = "products", allEntries = true)
    public Product update(Long id, Product productDetails) {
        Optional<Product> product = repository.findByIdAndCompanyId(id, TenantContext.requireTenantId());
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