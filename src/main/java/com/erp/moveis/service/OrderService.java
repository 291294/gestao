package com.erp.moveis.service;

import com.erp.moveis.model.Order;
import com.erp.moveis.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class OrderService {

    @Autowired
    private OrderRepository repository;

    public List<Order> list() {
        return repository.findAll();
    }

    public Optional<Order> findById(Long id) {
        return repository.findById(id);
    }

    public List<Order> findByClientId(Long clientId) {
        return repository.findByClientId(clientId);
    }

    public Order save(Order order) {
        return repository.save(order);
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }

    public Order update(Long id, Order orderDetails) {
        Optional<Order> order = repository.findById(id);
        if (order.isPresent()) {
            Order existingOrder = order.get();
            if (orderDetails.getTotalValue() != null) {
                existingOrder.setTotalValue(orderDetails.getTotalValue());
            }
            if (orderDetails.getStatus() != null) {
                existingOrder.setStatus(orderDetails.getStatus());
            }
            return repository.save(existingOrder);
        }
        return null;
    }
}