package br.com.cleo.dto;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import br.com.cleo.model.Client;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
@Mapper(componentModel = "cdi")
public interface ClientMapper {

    ClientDTO toClientDTO(Client client);

    @Mapping(target = "id", ignore = true)
    Client toClient(ClientDTO clientDTO);

}
