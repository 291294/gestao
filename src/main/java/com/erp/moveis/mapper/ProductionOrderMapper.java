package com.erp.moveis.mapper;

import com.erp.moveis.dto.ProductionOrderResponse;
import com.erp.moveis.manufacturing.entity.ProductionOrder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ProductionOrderMapper {

    @Mapping(source = "status", target = "status")
    ProductionOrderResponse toResponse(ProductionOrder entity);

    default String mapStatus(com.erp.moveis.manufacturing.entity.ProductionStatus status) {
        return status != null ? status.name() : null;
    }
}
