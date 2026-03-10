package com.erp.moveis.mapper;

import com.erp.moveis.dto.OrderItemRequest;
import com.erp.moveis.dto.OrderItemResponse;
import com.erp.moveis.dto.OrderRequest;
import com.erp.moveis.dto.OrderResponse;
import com.erp.moveis.model.Client;
import com.erp.moveis.model.Order;
import com.erp.moveis.model.OrderItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface OrderMapper {

    @Mapping(source = "client.id", target = "clientId")
    @Mapping(source = "client.name", target = "clientName")
    OrderResponse toResponse(Order entity);

    OrderItemResponse toItemResponse(OrderItem entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "client", source = "clientId", qualifiedByName = "clientRef")
    @Mapping(target = "items", ignore = true)
    @Mapping(target = "totalValue", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Order toEntity(OrderRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "order", ignore = true)
    @Mapping(target = "subtotal", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    OrderItem toItemEntity(OrderItemRequest request);

    @Named("clientRef")
    default Client clientRef(Long clientId) {
        if (clientId == null) return null;
        Client c = new Client();
        c.setId(clientId);
        return c;
    }
}
