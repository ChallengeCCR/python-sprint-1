package fiap.tds.controllers; // Ou o pacote dos seus controllers

import fiap.tds.dtos.SearchListDto;
import fiap.tds.dtos.ocorrenciaDto.CreateOcorrenciaDto;
import fiap.tds.dtos.ocorrenciaDto.ResponseOcorrenciaDto; // Para o tipo de retorno
import fiap.tds.dtos.usuarioDto.CreateUsuarioDto;
import fiap.tds.exceptions.BadRequestException;
import fiap.tds.exceptions.NotFoundException; // Se for validar Cidade e ela não for encontrada
import fiap.tds.services.OcorrenciaService;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Path("/ocorrencia")
public class OcorrenciaResource {

    // Logger
    private static final Logger logger = LogManager.getLogger(OcorrenciaResource.class);
    private final OcorrenciaService ocorrenciaService = new OcorrenciaService();

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response registrarOcorrencia(CreateOcorrenciaDto createOcorrenciaDto) {
        logger.info("Iniciando registro de ocorrência com dados: {}", createOcorrenciaDto);

        try {
            ocorrenciaService.registrar(createOcorrenciaDto);
            logger.info("✅ Ocorrência registrada com sucesso.");
            return Response.status(Response.Status.CREATED)
                    .entity("Ocorrência registrada com sucesso")
                    .build();
        } catch (BadRequestException e) {
            logger.warn("❌ Falha de validação ao registrar ocorrência: {}", e.getMessage());
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(e.getMessage())
                    .build();
        } catch (NotFoundException e) { // Exemplo se você validar a existência da cidade e ela não for encontrada
            logger.warn("❌ Recurso não encontrado durante o registro da ocorrência: {}", e.getMessage());
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(e.getMessage())
                    .build();
        } catch (RuntimeException e) { // Pega outras exceções não esperadas
            logger.error("❌ Erro inesperado ao registrar ocorrência: {}", e.getMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Ocorreu um erro inesperado ao processar sua solicitação.")
                    .build();
        }
    }


    @GET
    @Path("/search") // Mantém o mesmo sub-path do exemplo
    @Produces(MediaType.APPLICATION_JSON)
    public Response buscar(
           @QueryParam("page") @DefaultValue("1") int page,
           @QueryParam("tipoOcorrencia") String tipoOcorrencia, // Alterado de "nome" e "tipo"
           @QueryParam("nivelGravidade") String nivelGravidade, // Adicionado novo filtro relevante
           @QueryParam("direction") @DefaultValue("asc") String direction
    ) {
        // Log adaptado para os novos parâmetros
        logger.info("🔍 Buscando ocorrências: page={}, tipoOcorrencia={}, nivelGravidade={}, direction={}",
                page, tipoOcorrencia, nivelGravidade, direction);

        try {
            // Chamada ao service adaptada para OcorrenciaService e seus parâmetros
            SearchListDto<ResponseOcorrenciaDto> resultado = ocorrenciaService.buscar(page, tipoOcorrencia, nivelGravidade, direction);
            return Response.ok(resultado).build(); // HTTP 200 OK com o SearchListDto no corpo

        } catch (fiap.tds.exceptions.BadRequestException e) { // Mantém o tratamento de BadRequestException
            logger.warn("❌ Falha de validação ao buscar ocorrências: {}", e.getMessage());
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();

        } catch (Exception e) { // Pega outras exceções inesperadas
            logger.error("❌ Erro inesperado ao buscar ocorrências: {}", e.getMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Erro interno ao buscar ocorrências.") // Mensagem de erro adaptada
                    .build();
        }
    }

    @GET
    @Path("/cidade/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response buscarOcorrenciasPorCidade(
            @PathParam("id") int idCidade, // Mapeia o {id} do path para idCidade
            @QueryParam("page") @DefaultValue("1") int page,
            @QueryParam("tipoOcorrencia") String tipoOcorrencia,
            @QueryParam("nivelGravidade") String nivelGravidade,
            @QueryParam("direction") @DefaultValue("asc") String direction
    ) {
        logger.info("🔍 Buscando ocorrências para Cidade ID [{}]: page={}, tipoOcorrencia={}, nivelGravidade={}, direction={}",
                idCidade, page, tipoOcorrencia, nivelGravidade, direction);

        try {
            // Chama o service com todos os parâmetros, incluindo idCidade
            SearchListDto<ResponseOcorrenciaDto> resultado = ocorrenciaService.buscarPorCidade(
                    idCidade, page, tipoOcorrencia, nivelGravidade, direction
            );
            return Response.ok(resultado).build();

        } catch (fiap.tds.exceptions.BadRequestException e) {
            logger.warn("❌ Falha de validação ao buscar ocorrências para Cidade ID [{}]: {}", idCidade, e.getMessage());
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();

        } catch (Exception e) {
            logger.error("❌ Erro inesperado ao buscar ocorrências para Cidade ID [{}]: {}", idCidade, e.getMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Erro interno ao buscar ocorrências para a cidade especificada.")
                    .build();
        }
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response buscarPorId(@PathParam("id") int id) {
        logger.info("Iniciando busca de ocorrência com ID: {}", id);

        try {
            ResponseOcorrenciaDto ocorrenciaDto = ocorrenciaService.buscarPorId(id);

            logger.info("✅ Ocorrência com ID {} encontrada.", id);
            return Response.ok(ocorrenciaDto).build();

        } catch (NotFoundException e) {
            logger.warn("⚠️ Ocorrência com ID {} não encontrada: {}", id, e.getMessage());
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(e.getMessage())
                    .build();

        } catch (Exception e) {
            logger.error("❌ Erro inesperado ao buscar ocorrência com ID {}: {}", id, e.getMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR) // HTTP 500 Internal Server Error
                    .entity("Erro interno ao buscar ocorrência.")
                    .build();
        }
    }

    @PUT
    @Path("{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response atualizar(@PathParam("id") int id, CreateOcorrenciaDto createOcorrenciaDto) {
        logger.info("Iniciando atualização de ocorrência...");

        try {
            ocorrenciaService.atualizar(id, createOcorrenciaDto);
            return Response.ok("Ocorrência atualizada com sucesso.").build();

        } catch (fiap.tds.exceptions.NotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(e.getMessage())
                    .build();

        } catch (fiap.tds.exceptions.BadRequestException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(e.getMessage())
                    .build();

        } catch (Exception e) {
            logger.error("❌ Erro ao atualizar ocorrência", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Erro interno ao atualizar ocorrência.")
                    .build();
        }
    }

    @DELETE
    @Path("{id}")
    public Response deletar(@PathParam("id") int id) {
        try {
            ocorrenciaService.deletar(id);
            return Response.ok("Ocorrência deletada com sucesso.").build();

        } catch (fiap.tds.exceptions.NotFoundException e){
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();

        } catch (RuntimeException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Erro interno ao excluir ocorr~encia.")
                    .build();
        }
    }

}