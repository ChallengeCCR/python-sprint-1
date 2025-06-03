package fiap.tds.services;

import fiap.tds.dtos.SearchListDto;
import fiap.tds.dtos.SearchResult;
import fiap.tds.dtos.abrigoDto.CreateAbrigoDto;
import fiap.tds.dtos.abrigoDto.ResponseAbrigoDto;
import fiap.tds.entities.Abrigo;
import fiap.tds.exceptions.BadRequestException;
import fiap.tds.exceptions.NotFoundException;
import fiap.tds.infrastructure.ViaCep;
import fiap.tds.repositories.AbrigoRepository;

import javax.naming.ServiceUnavailableException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

public class AbrigoService {

    private final AbrigoRepository abrigoRepository = new AbrigoRepository();
    private final ViaCep viaCepService = new ViaCep();
    private final CidadeService cidadeService = new CidadeService();

    // Tamanho máximo da página
    private static final int PAGE_SIZE = 10;
    // Regex para o CEP
    private static final Pattern CEP_PATTERN = Pattern.compile("^\\d{5}-?\\d{3}$");
    // Regex para telefone
    private static final Pattern PHONE_NUMBER_PATTERN = Pattern.compile("^\\d{10,11}$");

    public void registrar(CreateAbrigoDto dto) throws ServiceUnavailableException {

        // Validando a cidade que o abrigo será relacionada
        if (dto.idCidade() <= 0) { // Validação do idCidade
            throw new BadRequestException("O ID da cidade é obrigatório e deve ser válido.");
        }

        var cidadeExistente = cidadeService.buscarPorId(dto.idCidade());

        if (cidadeExistente == null){
            throw new BadRequestException("A cidade fornecida não existe ou foi deletada");
        }

        // Validando o nome do abrigo
        if (dto.nomeAbrigo() == null || dto.nomeAbrigo().isBlank()) {
            throw new BadRequestException("O nome do abrigo é obrigatório.");
        }

        // Validando o CEP
        if (dto.cep() == null || dto.cep().isBlank()) {
            throw new BadRequestException("O CEP é obrigatório.");
        }

        String cepLimpo = dto.cep().replaceAll("[^0-9]", "");
        if (!CEP_PATTERN.matcher(dto.cep()).matches() || cepLimpo.length() != 8) {
            throw new BadRequestException("Formato de CEP inválido. Use XXXXX-XXX ou XXXXXXXX.");
        }

        // Validando a capacidade do abrigo
        if (dto.capacidadeMaxima() == null || dto.capacidadeMaxima() <= 0) {
            throw new BadRequestException("A capacidade máxima é obrigatória e deve ser maior que zero.");
        }

        // Validando o endereço do abrigo
        if (dto.enderecoAbrigo() == null || dto.enderecoAbrigo().isBlank()) {
            throw new BadRequestException("O endereço do abrigo é obrigatório.");
        }

        // Validando a resposta da API do ViaCep
        ViaCep.EnderecoCompleto enderecoInfo;
        try {
            enderecoInfo = viaCepService.obterEnderecoCompletoPorCep(cepLimpo);
        } catch (NotFoundException e) {
            throw new BadRequestException("CEP não encontrado ou inválido: " + cepLimpo);
        } catch (fiap.tds.exceptions.ServiceUnavailableException e) {
            throw new ServiceUnavailableException("Serviço ViaCEP indisponível ao obter dados para o CEP: " + cepLimpo + ". Detalhe: " + e.getMessage());
        }

        if (enderecoInfo == null || enderecoInfo.coordenadas() == null) {
            throw new ServiceUnavailableException("Não foi possível obter informações de coordenadas para o CEP: " + cepLimpo);
        }

        // Validando latitude e longitude
        BigDecimal latitude = enderecoInfo.coordenadas().latitude();
        BigDecimal longitude = enderecoInfo.coordenadas().longitude();

        if (latitude == null || longitude == null) { // Validações de lat/lon
            throw new ServiceUnavailableException("Não foi possível obter coordenadas (latitude/longitude) para o CEP: " + cepLimpo);
        }
        if (latitude.compareTo(new BigDecimal("-90")) < 0 || latitude.compareTo(new BigDecimal("90")) > 0) {
            throw new BadRequestException("Latitude inválida (" + latitude + ") obtida para o CEP. Deve estar entre -90 e 90.");
        }
        if (longitude.compareTo(new BigDecimal("-180")) < 0 || longitude.compareTo(new BigDecimal("180")) > 0) {
            throw new BadRequestException("Longitude inválida (" + longitude + ") obtida para o CEP. Deve estar entre -180 e 180.");
        }

        // Validando o status de funcionamento
        if (dto.statusFuncionamento() == null || !dto.statusFuncionamento().equalsIgnoreCase("NORMAL") && !dto.statusFuncionamento().equalsIgnoreCase("PARCIAL") && !dto.statusFuncionamento().equalsIgnoreCase("INTERDITADO")){
            throw new BadRequestException("O status de funcionamento deve ser NORMAL, PARCIAL ou INTERDITADO");
        }

        // Validando nível de segurança
        if (dto.nivelSegurancaAtual() == null || !dto.nivelSegurancaAtual().equalsIgnoreCase("ALTO") && !dto.nivelSegurancaAtual().equalsIgnoreCase("MÉDIO") && !dto.nivelSegurancaAtual().equalsIgnoreCase("BAIXO")){
            throw new BadRequestException("O nível de segurança deve ser ALTO, MÉDIO ou BAIXO");
        }

        // Validando o telefone de contato
        if (dto.telefoneContato() == null || dto.telefoneContato().isBlank()) {
            // Telefone de contato é opcional? Se sim, remova este throw.
            // Se for obrigatório, mantenha.
            throw new BadRequestException("O telefone de contato é obrigatório.");
        }

        String telefoneLimpo = dto.telefoneContato().replaceAll("[^0-9]", ""); // Remove non-digits

        // Validar se o telefone tem o comprimento correto (10 ou 11 dígitos, DDD + número)
        // E se começa com um DDD válido (2 dígitos, não 0 ou 1)
        // E se o primeiro dígito do número é 9 para celular (após o DDD) ou 2-5 para fixo
        if (!PHONE_NUMBER_PATTERN.matcher(telefoneLimpo).matches()) {
            throw new BadRequestException("Formato de telefone inválido. Use um formato como (XX) 9XXXX-XXXX ou (XX) XXXX-XXXX.");
        }

        // Transformando o DTO em Entidade
        Abrigo abrigo = new Abrigo();
        abrigo.setNomeAbrigo(dto.nomeAbrigo());
        abrigo.setCep(cepLimpo);
        abrigo.setEnderecoAbrigo(dto.enderecoAbrigo());
        abrigo.setCapacidadeMaxima(dto.capacidadeMaxima());
        abrigo.setStatusFuncionamento(dto.statusFuncionamento());
        abrigo.setNivelSegurancaAtual(dto.nivelSegurancaAtual());
        abrigo.setTelefoneContato(dto.telefoneContato());
        abrigo.setLat(latitude);
        abrigo.setLon(longitude);
        abrigo.setDataCriacao(LocalDateTime.now());
        abrigo.setDeleted(false);
        abrigo.setIdCidade(dto.idCidade());

        // Chamando o repository que vai registrar
        abrigoRepository.registrar(abrigo);
    }

    public SearchListDto<ResponseAbrigoDto> buscar(int page, String nomeAbrigo, String cep, String status, String direction) {

        // Validação do tamanho da página
        if (page < 1) {
            throw new BadRequestException("O número da página deve ser maior ou igual a 1.");
        }

        // Chamando o repository e pegando as informações úteis
        var resultadoRepository = abrigoRepository.buscar(nomeAbrigo, cep, status, direction);
        List<Abrigo> todosAbrigos = resultadoRepository.data();
        int totalItems = resultadoRepository.totalItems();

        // Gerando informações de paginação
        List<ResponseAbrigoDto> pageData = todosAbrigos.stream()
                .skip(Math.max((page - 1) * PAGE_SIZE, 0))
                .limit(PAGE_SIZE)
                .map(this::converterAbrigoParaDto)
                .toList();
        return new SearchListDto<>(page, direction, PAGE_SIZE, totalItems, pageData);
    }


    public SearchListDto<ResponseAbrigoDto> buscarAbrigosPorCidade(
            int idCidade,
            int page,
            String nomeAbrigo,
            String cep,
            String statusFuncionamento,
            String direction) {

        // Validação do número da página
        if (page < 1) {
            throw new BadRequestException("O número da página deve ser maior ou igual a 1.");
        }

        // Validação do ID da cidade
        if (idCidade <= 0) {
            throw new BadRequestException("O ID da cidade deve ser um número positivo.");
        }

        final int PAGE_SIZE = 10;

        SearchResult<Abrigo> resultado = abrigoRepository.buscarPorIdCidade(
                idCidade,
                nomeAbrigo,
                cep,
                statusFuncionamento,
                direction
        );

        // Informações para paginação
        int totalItems = resultado.totalItems();
        List<Abrigo> abrigos = resultado.data();

        // Calcula os índices de início e fim para a paginação
        int start = Math.max((page - 1) * PAGE_SIZE, 0);
        int end = Math.min(start + PAGE_SIZE, totalItems);

        List<ResponseAbrigoDto> pageData = List.of(); // Inicializa a lista de dados da página

        // Verifica se há itens para a página atual e se a lista de abrigos não está vazia
        if (start < totalItems && !abrigos.isEmpty()) {
            int effectiveEnd = Math.min(end, abrigos.size()); // Garante que 'end' não ultrapasse o tamanho da lista 'abrigos'
            // Se o início for maior ou igual ao fim efetivo, não há dados para esta página
            if (start >= effectiveEnd) {
                pageData = Collections.emptyList();
            } else {
                // Cria a sublista para a página atual e converte para DTOs
                pageData = abrigos.subList(start, effectiveEnd).stream()
                        .map(this::converterAbrigoParaDto) // Usa o método de conversão para DTO
                        .toList();
            }
        } else {
            // Se não houver itens para a página atual, retorna uma lista vazia
            pageData = Collections.emptyList();
        }

        // Retorna o DTO com os dados da página e informações de paginação
        return new SearchListDto<>(page, direction, PAGE_SIZE, totalItems, pageData);
    }

    public ResponseAbrigoDto buscarPorId(int id) {

        // Validando a existência de um abrigo
        if (id <= 0){
            throw new BadRequestException("O ID do abrigo deve ser maior do que zero.");
        }

        var abrigoExistente = abrigoRepository.buscarPorId(id);

        if (abrigoExistente == null) {
            throw new NotFoundException("Abrigo com o ID " + id + " não encontrado.");
        }

        return converterAbrigoParaDto(abrigoExistente);
    }

    public void atualizar(int id, CreateAbrigoDto dto) throws ServiceUnavailableException {

        // Validando a existência de um abrigo pelo ID fornecido
        if (id <= 0) {
            throw new BadRequestException("O ID do abrigo para atualização é obrigatório e deve ser maior do que zero.");
        }

        var abrigoExistente = abrigoRepository.buscarPorId(id);

        if (abrigoExistente == null) {
            throw new BadRequestException("Abrigo com o ID " + id + " não encontrado para atualização.");
        }

        // Validando a cidade que o abrigo será relacionada (como no registrar)
        if (dto.idCidade() <= 0) {
            throw new BadRequestException("O ID da cidade é obrigatório e deve ser válido para atualização.");
        }

        var cidadeExistente = cidadeService.buscarPorId(dto.idCidade());

        if (cidadeExistente == null){
            throw new BadRequestException("A cidade fornecida para atualização não existe ou foi deletada");
        }

        // Validando o nome do abrigo (como no registrar)
        if (dto.nomeAbrigo() == null || dto.nomeAbrigo().isBlank()) {
            throw new BadRequestException("O nome do abrigo é obrigatório para atualização.");
        }

        // Validando o CEP (como no registrar)
        if (dto.cep() == null || dto.cep().isBlank()) {
            throw new BadRequestException("O CEP é obrigatório para atualização.");
        }

        String cepLimpo = dto.cep().replaceAll("[^0-9]", "");
        if (!CEP_PATTERN.matcher(dto.cep()).matches() || cepLimpo.length() != 8) {
            throw new BadRequestException("Formato de CEP inválido para atualização. Use XXXXX-XXX ou XXXXXXXX.");
        }

        // Validando a capacidade do abrigo (como no registrar)
        if (dto.capacidadeMaxima() == null || dto.capacidadeMaxima() <= 0) {
            throw new BadRequestException("A capacidade máxima é obrigatória e deve ser maior que zero para atualização.");
        }

        // Validando o endereço do abrigo (como no registrar)
        if (dto.enderecoAbrigo() == null || dto.enderecoAbrigo().isBlank()) {
            throw new BadRequestException("O endereço do abrigo é obrigatório para atualização.");
        }

        // Validando a resposta da API do ViaCep (como no registrar)
        ViaCep.EnderecoCompleto enderecoInfo;
        try {
            enderecoInfo = viaCepService.obterEnderecoCompletoPorCep(cepLimpo);
        } catch (NotFoundException e) {
            // Se o CEP não foi encontrado (ViaCEP retornou 404), é um erro do CLIENTE (BadRequest)
            throw new BadRequestException("CEP não encontrado ou inválido: " + cepLimpo);
        } catch (fiap.tds.exceptions.ServiceUnavailableException e) {
            throw new fiap.tds.exceptions.ServiceUnavailableException("Erro ao obter dados para o CEP: " + cepLimpo);
        }

        if (enderecoInfo == null || enderecoInfo.coordenadas() == null) {
            throw new ServiceUnavailableException("Não foi possível obter informações de coordenadas para o CEP de atualização: " + cepLimpo);
        }

        // Validando latitude e longitude (como no registrar)
        BigDecimal latitude = enderecoInfo.coordenadas().latitude();
        BigDecimal longitude = enderecoInfo.coordenadas().longitude();

        if (latitude == null || longitude == null) { // Validações de lat/lon
            throw new ServiceUnavailableException("Não foi possível obter coordenadas (latitude/longitude) para o CEP de atualização: " + cepLimpo);
        }
        if (latitude.compareTo(new BigDecimal("-90")) < 0 || latitude.compareTo(new BigDecimal("90")) > 0) {
            throw new BadRequestException("Latitude inválida (" + latitude + ") obtida para o CEP de atualização. Deve estar entre -90 e 90.");
        }
        if (longitude.compareTo(new BigDecimal("-180")) < 0 || longitude.compareTo(new BigDecimal("180")) > 0) {
            throw new BadRequestException("Longitude inválida (" + longitude + ") obtida para o CEP de atualização. Deve estar entre -180 e 180.");
        }

        // Validando o status de funcionamento (como no registrar)
        if (dto.statusFuncionamento() == null || !dto.statusFuncionamento().equalsIgnoreCase("NORMAL") && !dto.statusFuncionamento().equalsIgnoreCase("PARCIAL") && !dto.statusFuncionamento().equalsIgnoreCase("INTERDITADO")){
            throw new BadRequestException("O status de funcionamento deve ser NORMAL, PARCIAL ou INTERDITADO para atualização");
        }

        // Validando nível de segurança (como no registrar)
        if (dto.nivelSegurancaAtual() == null || !dto.nivelSegurancaAtual().equalsIgnoreCase("ALTO") && !dto.nivelSegurancaAtual().equalsIgnoreCase("MÉDIO") && !dto.nivelSegurancaAtual().equalsIgnoreCase("BAIXO")){
            throw new BadRequestException("O nível de segurança deve ser ALTO, MÉDIO ou BAIXO para atualização");
        }

        // Validando o telefone de contato
        if (dto.telefoneContato() == null || dto.telefoneContato().isBlank()) {
            // Telefone de contato é opcional? Se sim, remova este throw.
            // Se for obrigatório, mantenha.
            throw new BadRequestException("O telefone de contato é obrigatório.");
        }

        String telefoneLimpo = dto.telefoneContato().replaceAll("[^0-9]", ""); // Remove non-digits

        // Validar se o telefone tem o comprimento correto (10 ou 11 dígitos, DDD + número)
        // E se começa com um DDD válido (2 dígitos, não 0 ou 1)
        // E se o primeiro dígito do número é 9 para celular (após o DDD) ou 2-5 para fixo
        if (!PHONE_NUMBER_PATTERN.matcher(telefoneLimpo).matches()) {
            throw new BadRequestException("Formato de telefone inválido. Use um formato como (XX) 9XXXX-XXXX ou (XX) XXXX-XXXX.");
        }

        Abrigo abrigoParaAtualizar = new Abrigo();
        abrigoParaAtualizar.setIdAbrigo(id);
        abrigoParaAtualizar.setNomeAbrigo(dto.nomeAbrigo());
        abrigoParaAtualizar.setCep(cepLimpo);
        abrigoParaAtualizar.setEnderecoAbrigo(dto.enderecoAbrigo());
        abrigoParaAtualizar.setCapacidadeMaxima(dto.capacidadeMaxima());
        abrigoParaAtualizar.setStatusFuncionamento(dto.statusFuncionamento());
        abrigoParaAtualizar.setNivelSegurancaAtual(dto.nivelSegurancaAtual());
        abrigoParaAtualizar.setTelefoneContato(dto.telefoneContato());
        abrigoParaAtualizar.setLat(latitude);
        abrigoParaAtualizar.setLon(longitude);
        abrigoParaAtualizar.setIdCidade(dto.idCidade());

        // Chamando o repository que vai atualizar
        abrigoRepository.atualizar(id, abrigoParaAtualizar);
    }

    public void deletar(int id) {
        buscarPorId(id);
        abrigoRepository.deletar(id);
    }

    private ResponseAbrigoDto converterAbrigoParaDto(Abrigo abrigo) {
        if (abrigo == null) return null;
        return new ResponseAbrigoDto(
                abrigo.getIdAbrigo(),
                abrigo.isDeleted(),
                abrigo.getDataCriacao(), // Espera-se LocalDateTime aqui
                abrigo.getCep(),
                abrigo.getNomeAbrigo(),
                abrigo.getEnderecoAbrigo(),
                abrigo.getCapacidadeMaxima(),
                abrigo.getStatusFuncionamento(),
                abrigo.getNivelSegurancaAtual(),
                abrigo.getTelefoneContato(),
                abrigo.getIdCidade(),
                abrigo.getLat(),
                abrigo.getLon()
        );
    }
}