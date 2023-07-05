package br.com.cleo.resource;

import org.bson.types.ObjectId;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import br.com.cleo.dto.ClientDTO;
import br.com.cleo.dto.ClientMapper;
import br.com.cleo.model.Client;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

@Consumes("application/json")
@Produces("application/json")
@Tag(name = "Client")

@APIResponse(responseCode = "200", description = "Success")
@APIResponse(responseCode = "201", description = "Created")
@APIResponse(responseCode = "204", description = "No Content")
@APIResponse(responseCode = "400", description = "Bad Request")
@APIResponse(responseCode = "404", description = "Not Found")
@APIResponse(responseCode = "409", description = "Conflict")
@APIResponse(responseCode = "500", description = "Internal Server Error")
@APIResponse(responseCode = "503", description = "Service Unavailable")
@APIResponse(responseCode = "504", description = "Gateway Timeout")
@Path("/client")
public class ClienteResource {

    @Inject
    ClientMapper clientMapper;

    @GET
    public Uni<Response> listAll() {
        return Client
                .findAll()
                .list()
                .onItem().transform(clients -> {

                    // List<ClientDTO> dtoList = clients.stream()
                    // .map(entity -> clientMapper.toClientDTO((Client) entity))
                    // .collect(Collectors.toList());

                    return Response.status(Response.Status.OK)
                            .entity(clients).build();
                }).onFailure().recoverWithItem(f -> {
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                            .entity(f.getMessage()).build();
                });
    }

    @GET
    @Path("/{id}")
    public Uni<Response> findById(@PathParam("id") String id) {
        return Client
                .findById(new ObjectId(id))
                .onItem().transform(c -> {
                    return Response.status(Response.Status.OK)
                            .entity(c).build();
                }).onFailure().recoverWithItem(f -> {
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                            .entity(f.getMessage()).build();
                });
    }

    @POST
    public Uni<Response> save(ClientDTO clientDTO) {
        return Client.find("name", clientDTO.name)
                .firstResult()
                .flatMap(existingClient -> {
                    if (existingClient != null) {
                        // Já existe um cliente com o mesmo nome, retornar um erro de conflito
                        return Uni.createFrom().failure(new WebApplicationException("Client with name "
                                + clientDTO.name + " already exists",
                                Response.status(Response.Status.CONFLICT).entity("{" + "\"error\":\"Client with name "
                                        + clientDTO.name + " already exists\"" + "}").build()));

                    } else {
                        // Não existe um cliente com o mesmo nome, realizar a conversão e persistência
                        Client client = clientMapper.toClient(clientDTO);
                        return client.persist()
                                .onItem().transform(
                                        savedClient -> Response.status(Response.Status.CREATED).entity(savedClient)
                                                .build());
                    }
                });
    }

    @PUT
    @Path("/{id}")
    public Uni<Response> update(@PathParam("id") String id, ClientDTO clientDTO) {
        Uni<Client> client = Client.findById(new ObjectId(id));

        return client
                .onItem().transform(c -> {
                    c.name = clientDTO.name;
                    c.addLocations(clientDTO.locations);
                    c.update().subscribe().asCompletionStage();
                    return c;
                }).onItem().transform(entity -> {
                    return Response.status(Response.Status.OK).entity(entity).build();
                }).onFailure().recoverWithItem(f -> {
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                            .entity(f.getMessage()).build();
                });
    }

    @DELETE
    @Path("/{id}")
    public Uni<Response> delete(@PathParam("id") String id) {
        return Client
                .deleteById(new ObjectId(id))
                .onItem().transform(deleted -> {
                    if (deleted) {
                        return Response.status(Response.Status.OK).build();
                    } else {
                        return Response.status(Response.Status.NOT_FOUND)
                                .entity("{\"error\":\"Client with id " + id + " not found\"}").build();
                    }
                })
                .onFailure().recoverWithItem(failure -> {
                    // Ocorreu um erro durante a exclusão do cliente
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                            .entity(failure.getMessage()).build();
                });
    }

}