package com.erp.moveis.service;

import com.erp.moveis.core.tenant.TenantContext;
import com.erp.moveis.model.Supplier;
import com.erp.moveis.repository.SupplierRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SupplierService {

    @Autowired
    private SupplierRepository repository;

    @Cacheable(value = "suppliers", key = "T(com.erp.moveis.core.tenant.TenantContext).getTenantId()")
    public List<Supplier> list() {
        return repository.findByCompanyId(TenantContext.requireTenantId());
    }

    public List<Supplier> listActive() {
        return repository.findByCompanyIdAndActiveTrue(TenantContext.requireTenantId());
    }

    public Optional<Supplier> findById(Long id) {
        return repository.findById(id)
                .filter(s -> s.getCompanyId().equals(TenantContext.requireTenantId()));
    }

    @CacheEvict(value = "suppliers", allEntries = true)
    public Supplier save(Supplier supplier) {
        supplier.setCompanyId(TenantContext.requireTenantId());
        return repository.save(supplier);
    }

    @CacheEvict(value = "suppliers", allEntries = true)
    public Supplier update(Long id, Supplier details) {
        return repository.findById(id)
                .filter(s -> s.getCompanyId().equals(TenantContext.requireTenantId()))
                .map(existing -> {
                    if (details.getName() != null) existing.setName(details.getName());
                    if (details.getCnpj() != null) existing.setCnpj(details.getCnpj());
                    if (details.getEmail() != null) existing.setEmail(details.getEmail());
                    if (details.getPhone() != null) existing.setPhone(details.getPhone());
                    if (details.getContactPerson() != null) existing.setContactPerson(details.getContactPerson());
                    if (details.getAddress() != null) existing.setAddress(details.getAddress());
                    if (details.getCity() != null) existing.setCity(details.getCity());
                    if (details.getState() != null) existing.setState(details.getState());
                    if (details.getZipCode() != null) existing.setZipCode(details.getZipCode());
                    if (details.getCategory() != null) existing.setCategory(details.getCategory());
                    if (details.getNotes() != null) existing.setNotes(details.getNotes());
                    if (details.getActive() != null) existing.setActive(details.getActive());
                    return repository.save(existing);
                })
                .orElseThrow(() -> new RuntimeException("Fornecedor não encontrado"));
    }

    @CacheEvict(value = "suppliers", allEntries = true)
    public void delete(Long id) {
        repository.findById(id)
                .filter(s -> s.getCompanyId().equals(TenantContext.requireTenantId()))
                .ifPresent(s -> repository.deleteById(s.getId()));
    }
}
