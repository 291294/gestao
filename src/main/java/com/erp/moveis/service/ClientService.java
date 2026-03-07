package com.erp.moveis.service;

import com.erp.moveis.model.Client;
import com.erp.moveis.repository.ClientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ClientService {

    @Autowired
    private ClientRepository repository;

    public List<Client> list() {
        return repository.findAll();
    }

    public Optional<Client> findById(Long id) {
        return repository.findById(id);
    }

    public Client save(Client client) {
        return repository.save(client);
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }

    public Client update(Long id, Client clientDetails) {
        Optional<Client> client = repository.findById(id);
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