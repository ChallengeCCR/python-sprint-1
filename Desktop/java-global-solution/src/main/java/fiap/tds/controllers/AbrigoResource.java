package fiap.tds.controllers; // Ou o pacote onde seus controllers JAX-RS estão

import fiap.tds.dtos.abrigoDto.CreateAbrigoDto;   // DTO para criar Abrigo
import fiap.tds.dtos.abrigoDto.ResponseAbrigoDto; // DTO de resposta para Abrigo
import fiap.tds.dtos.SearchListDto;               // DTO genérico para listas paginadas
import fiap.tds.dtos.ocorrenciaDto.ResponseOcorrenciaDto;
import fiap.tds.exceptions.BadRequestException;
import fiap.tds.exceptions.NotFoundException;
import fiap.tds.services.AbrigoService;          // Serviço de Abrigo

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

// Import para ServiceUnavailableException que o AbrigoService pode lançar
import javax.naming.ServiceUnavailableException;


@Path("/abrigo")
public class AbrigoResource {

    // Logger e service
    private static final Logger logger = LogManager.getLogger(AbrigoResource.class);
    private final AbrigoService abrigoService = new AbrigoService();

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response registrar(CreateAbrigoDto createAbrigoDto) {
        logger.info("Iniciando registro de abrigo com nome: {}", createAbrigoDto.nomeAbrigo());
        try {
            // Chamando o service
            abrigoService.registrar(createAbrigoDto);
            logger.info("✅ Abrigo registrado com sucesso");
            return Response.status(Response.Status.CREATED).entity("Abrigo registrado com sucesso").build();
        } catch (BadRequestException e) {
            logger.error("❌ Falha de validação ao registrar abrigo: {}", e.getMessage());
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        } catch (ServiceUnavailableException e) { // Erro do ViaCep
            logger.error("❌ Serviço externo (ViaCEP) indisponível ao registrar abrigo: {}", e.getMessage());
            return Response.status(Response.Status.SERVICE_UNAVAILABLE).entity("Serviço externo indisponível: " + e.getMessage()).build();
        } catch (RuntimeException e) {
            logger.error("❌ Erro inesperado ao registrar abrigo: {}", e.getMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Erro ao registrar abrigo.").build();
        }
    }

    @GET
    @Path("/search")
    @Produces(MediaType.APPLICATION_JSON)
    public Response buscar(
            @QueryParam("page") @DefaultValue("1") int page,
            @QueryParam("nomeAbrigo") String nomeAbrigo,
            @QueryParam("cep") String cep,
            @QueryParam("statusFuncionamento") String status,
            @QueryParam("direction") @DefaultValue("asc") String direction) {
        logger.info("🔍 Buscando abrigos: page={}, nomeAbrigo={}, cep={}, status={}, direction={}",
                page, nomeAbrigo, cep, status, direction);
        try {
            SearchListDto<ResponseAbrigoDto> resultado = abrigoService.buscar(page, nomeAbrigo, cep, status, direction);
            return Response.ok(resultado).build();
        } catch (BadRequestException e) {
            logger.warn("❌ Falha de validação ao buscar abrigos: {}", e.getMessage());
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        } catch (Exception e) {
            logger.error("❌ Erro ao buscar abrigos: {}", e.getMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Erro interno ao buscar abrigos.").build();
        }
    }

    @GET
    @Path("/cidade/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response buscarAbrigosPorCidade(
            @PathParam("id") int idCidade,
            @QueryParam("page") @DefaultValue("1") int page,
            @QueryParam("nomeAbrigo") String nomeAbrigo,
            @QueryParam("cep") String cep,
            @QueryParam("statusFuncionamento") String statusFuncionamento,
            @QueryParam("direction") @DefaultValue("asc") String direction
    ) {
        logger.info("🔍 Buscando abrigos para Cidade ID [{}], Página [{}], Nome [{}], CEP [{}], Status [{}], Direção [{}]",
                idCidade, page, nomeAbrigo, cep, statusFuncionamento, direction);

        try {
            SearchListDto<ResponseAbrigoDto> resultado = abrigoService.buscarAbrigosPorCidade(
                    idCidade,
                    page,
                    nomeAbrigo,
                    cep,
                    statusFuncionamento,
                    direction
            );
            logger.info("✅ {} abrigos encontrados para Cidade ID [{}]", resultado.data().size(), idCidade);
            return Response.ok(resultado).build();

        } catch (fiap.tds.exceptions.BadRequestException e) { // Certifique-se que o pacote da exceção está correto
            logger.warn("❌ Falha de validação ao buscar abrigos para Cidade ID [{}]: {}", idCidade, e.getMessage());
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();

        } catch (Exception e) {
            logger.error("❌ Erro inesperado ao buscar abrigos para Cidade ID [{}]: {}", idCidade, e.getMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Erro interno ao buscar abrigos para a cidade especificada.")
                    .build();
        }
    }


    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response buscarAbrigoPorId(@PathParam("id") int id) {
        logger.info("Iniciando busca de abrigo com ID {}", id);
        try {
            ResponseAbrigoDto abrigo = abrigoService.buscarPorId(id);
            return Response.ok(abrigo).build();
        } catch (NotFoundException e) {
            logger.warn("❌ Abrigo com ID {} não encontrado: {}", id, e.getMessage());
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        } catch (Exception e) {
            logger.error("❌ Erro ao buscar abrigo por ID {}: {}", id, e.getMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Erro ao buscar abrigo.").build();
        }
    }

    @PUT
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response atualizar(@PathParam("id") int id, CreateAbrigoDto createAbrigoDto) {
        logger.info("Iniciando atualização do abrigo com ID: {}", id);
        try {
            abrigoService.atualizar(id, createAbrigoDto);
            logger.info("✅ Abrigo com ID {} atualizado com sucesso.", id);
            return Response.ok("Abrigo atualizado com sucesso").build();
        } catch (NotFoundException e) {
            logger.warn("❌ Abrigo com ID {} não encontrado para atualização: {}", id, e.getMessage());
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        } catch (BadRequestException e) {
            logger.warn("❌ Falha de validação ao atualizar abrigo com ID {}: {}", id, e.getMessage());
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        } catch (ServiceUnavailableException e) { // Capturando a exceção específica do serviço
            logger.error("❌ Serviço externo (ViaCEP) indisponível ao atualizar abrigo com ID {}: {}", id, e.getMessage());
            return Response.status(Response.Status.SERVICE_UNAVAILABLE).entity("Serviço externo indisponível: " + e.getMessage()).build();
        } catch (Exception e) {
            logger.error("❌ Erro ao atualizar abrigo com ID {}: {}", id, e.getMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Erro interno ao atualizar abrigo.").build();
        }
    }

    @DELETE
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deletar(@PathParam("id") int id) {
        logger.info("Iniciando deleção do abrigo com ID {}", id);
        try {
            abrigoService.deletar(id); // O serviço já lança NotFoundException se não encontrar
            logger.info("✅ Abrigo com ID {} deletado com sucesso.", id);
            return Response.ok("Abrigo deletado com sucesso.").build(); // Mensagem de sucesso
        } catch (NotFoundException e) {
            logger.warn("❌ Abrigo com ID {} não encontrado para deleção: {}", id, e.getMessage());
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        } catch (Exception e) {
            logger.error("❌ Erro ao deletar abrigo com ID {}: {}", id, e.getMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Erro ao excluir abrigo.").build();
        }
    }
}