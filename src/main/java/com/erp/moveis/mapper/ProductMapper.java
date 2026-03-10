package com.erp.moveis.mapper;

import com.erp.moveis.dto.ProductRequest;
import com.erp.moveis.dto.ProductResponse;
import com.erp.moveis.model.Product;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface ProductMapper {

    ProductResponse toResponse(Product entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    Product toEntity(ProductRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    void updateEntity(ProductRequest request, @MappingTarget Product entity);
}
