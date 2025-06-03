package fiap.tds.controllers;

import fiap.tds.dtos.cidadeDto.CreateCidadeDto;
import fiap.tds.dtos.cidadeDto.ResponseCidadeDto; // Certifique-se que este DTO existe
import fiap.tds.dtos.SearchListDto; // Certifique-se que este DTO genérico existe
import fiap.tds.exceptions.BadRequestException;
import fiap.tds.exceptions.NotFoundException;

import fiap.tds.services.CidadeService;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Path("/cidade")
public class CidadeResource {

    private static final Logger logger = LogManager.getLogger(CidadeResource.class);
    private final CidadeService cidadeService = new CidadeService();

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response registrar(CreateCidadeDto createCidadeDto) {
        logger.info("Iniciando registro de cidade com CEP: {}", createCidadeDto.cep());
        try {
            cidadeService.registrar(createCidadeDto);
            logger.info("✅ Cidade registrada com sucesso");
            return Response.status(Response.Status.CREATED).entity("Cidade registrada com sucesso").build();
        } catch (fiap.tds.exceptions.BadRequestException e) {
            logger.error("❌ Falha de validação ao registrar cidade: {}", e.getMessage());
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        } catch (fiap.tds.exceptions.ServiceUnavailableException e) {
            logger.error("❌ Serviço externo indisponível ao registrar cidade: {}", e.getMessage());
            return Response.status(Response.Status.SERVICE_UNAVAILABLE).entity(e.getMessage()).build();
        } catch (RuntimeException e) {
            logger.error("❌ Erro inesperado ao registrar cidade: {}", e.getMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Erro ao registrar cidade.").build();
        }
    }

    @GET
    @Path("/search") // Endpoint para busca com filtros
    @Produces(MediaType.APPLICATION_JSON)
    public Response buscar(
            @QueryParam("page") @DefaultValue("1") int page,
            @QueryParam("nome") String nome, // Removido o @QueryParam("tipo")
            @QueryParam("direction") @DefaultValue("asc") String direction) {
        logger.info("🔍 Buscando cidades: page={}, nome={}, direction={}", page, nome, direction);
        try {
            SearchListDto<ResponseCidadeDto> resultado = cidadeService.buscar(page, nome, direction);
            return Response.ok(resultado).build();
        } catch (fiap.tds.exceptions.BadRequestException e) {
            logger.error("❌ Falha de validação ao buscar cidades: {}", e.getMessage());
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        } catch (Exception e) {
            logger.error("❌ Erro ao buscar cidades: {}", e.getMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Erro interno ao buscar cidades.").build();
        }
    }

    @GET
    @Path("/{id}") // Parâmetro de path para o ID
    @Produces(MediaType.APPLICATION_JSON) // Adicionado para retornar o DTO
    public Response buscarCidadePorId(@PathParam("id") int id) {
        logger.info("Iniciando busca de cidade com ID {}", id);
        try {
            ResponseCidadeDto cidade = cidadeService.buscarPorId(id);
            return Response.ok(cidade).build();
        } catch (NotFoundException e) {
            logger.error("❌ Cidade com ID {} não encontrada: {}", id, e.getMessage());
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        } catch (Exception e) {
            logger.error("❌ Erro ao buscar cidade por ID {}: {}", id, e.getMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Erro ao buscar cidade.").build();
        }
    }

    @PUT
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response atualizar(@PathParam("id") int id, CreateCidadeDto createCidadeDto) {
        logger.info("Iniciando atualização da cidade com ID: {}", id);
        try {
            cidadeService.atualizar(id, createCidadeDto);
            logger.info("✅ Cidade com ID {} atualizada com sucesso.", id);
            return Response.ok("Cidade atualizada com sucesso").build();
        } catch (fiap.tds.exceptions.NotFoundException e) {
            logger.error("❌ Cidade com ID {} não encontrada para atualização: {}", id, e.getMessage());
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        } catch (fiap.tds.exceptions.BadRequestException e) {
            logger.error("❌ Falha de validação ao atualizar cidade com ID {}: {}", id, e.getMessage());
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        } catch (fiap.tds.exceptions.ServiceUnavailableException e) {
            logger.error("❌ Serviço externo indisponível ao atualizar cidade com ID {}: {}", id, e.getMessage());
            return Response.status(Response.Status.SERVICE_UNAVAILABLE).entity(e.getMessage()).build();
        } catch (Exception e) {
            logger.error("❌ Erro ao atualizar cidade com ID {}: {}", id, e.getMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Erro interno ao atualizar cidade.").build();
        }
    }

    @DELETE
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON) // Consistência para retornar uma mensagem JSON
    public Response deletar(@PathParam("id") int id) {
        logger.info("Iniciando deleção da cidade com ID {}", id);
        try {
            cidadeService.deletar(id);
            logger.info("✅ Cidade com ID {} deletada com sucesso.", id);
            return Response.ok("Cidade deletada com sucesso.").build();
        } catch (fiap.tds.exceptions.NotFoundException e) {
            logger.error("❌ Cidade com ID {} não encontrada para deleção: {}", id, e.getMessage());
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        } catch (Exception e) {
            logger.error("❌ Erro ao deletar cidade com ID {}: {}", id, e.getMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Erro ao excluir cidade.").build();
        }
    }
}
