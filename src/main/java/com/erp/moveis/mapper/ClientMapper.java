package com.erp.moveis.mapper;

import com.erp.moveis.dto.ClientRequest;
import com.erp.moveis.dto.ClientResponse;
import com.erp.moveis.model.Client;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface ClientMapper {

    ClientResponse toResponse(Client entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    Client toEntity(ClientRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    void updateEntity(ClientRequest request, @MappingTarget Client entity);
}
