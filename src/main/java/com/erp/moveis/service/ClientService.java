package com.erp.moveis.service;

import com.erp.moveis.core.tenant.TenantContext;
import com.erp.moveis.model.Client;
import com.erp.moveis.repository.ClientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ClientService {

    @Autowired
    private ClientRepository repository;

    @Cacheable(value = "clients", key = "T(com.erp.moveis.core.tenant.TenantContext).getTenantId()")
    public List<Client> list() {
        return repository.findByCompanyId(TenantContext.requireTenantId());
    }

    public Page<Client> listPaged(Pageable pageable) {
        return repository.findByCompanyId(TenantContext.requireTenantId(), pageable);
    }

    public Optional<Client> findById(Long id) {
        return repository.findByIdAndCompanyId(id, TenantContext.requireTenantId());
    }

    @CacheEvict(value = "clients", allEntries = true)
    public Client save(Client client) {
        client.setCompanyId(TenantContext.requireTenantId());
        return repository.save(client);
    }

    @CacheEvict(value = "clients", allEntries = true)
    public void delete(Long id) {
        repository.findByIdAndCompanyId(id, TenantContext.requireTenantId())
                .ifPresent(c -> repository.deleteById(c.getId()));
    }

    @CacheEvict(value = "clients", allEntries = true)
    public Client update(Long id, Client clientDetails) {
        Optional<Client> client = repository.findByIdAndCompanyId(id, TenantContext.requireTenantId());
        if (client.isPresent()) {
            Client existingClient = client.get();
            if (clientDetails.getName() != null) {
                existingClient.setName(clientDetails.getName());
            }
            if (clientDetails.getPhone() != null) {
                existingClient.setPhone(clientDetails.getPhone());
            }
            if (clientDetails.getEmail() != null) {
                existingClient.setEmail(clientDetails.getEmail());
            }
            if (clientDetails.getProfession() != null) {
                existingClient.setProfession(clientDetails.getProfession());
            }
            if (clientDetails.getPreferences() != null) {
                existingClient.setPreferences(clientDetails.getPreferences());
            }
            return repository.save(existingClient);
        }
        return null;
    }
}