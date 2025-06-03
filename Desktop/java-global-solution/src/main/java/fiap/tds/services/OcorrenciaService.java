package fiap.tds.services;

import fiap.tds.dtos.SearchListDto;
import fiap.tds.dtos.SearchResult;
import fiap.tds.dtos.ocorrenciaDto.CreateOcorrenciaDto;
import fiap.tds.dtos.ocorrenciaDto.ResponseOcorrenciaDto;
import fiap.tds.entities.Ocorrencia;
import fiap.tds.exceptions.BadRequestException;
import fiap.tds.exceptions.NotFoundException;
import fiap.tds.exceptions.ServiceUnavailableException;
import fiap.tds.infrastructure.ViaCep;
import fiap.tds.repositories.CidadeRepository;
import fiap.tds.repositories.OcorrenciaRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

public class OcorrenciaService {

    private final OcorrenciaRepository ocorrenciaRepository = new OcorrenciaRepository();
    private final CidadeRepository cidadeRepository = new CidadeRepository();
    private final ViaCep viaCepService = new ViaCep();
    private static final Pattern CEP_PATTERN = Pattern.compile("^\\d{5}-?\\d{3}$");

    private static final List<String> TIPOS_OCORRENCIA_VALIDOS = Arrays.asList(
            "DESLIZAMENTO", "ENCHENTE", "QUEIMADA"
    );

    private static final List<String> NIVEIS_GRAVIDADE_VALIDOS = Arrays.asList(
            "BAIXO", "MÉDIO", "ALTO"
    );

    public void registrar(CreateOcorrenciaDto dto) throws ServiceUnavailableException {

        // Validando o CEP
        if (dto.cep() == null || dto.cep().isBlank()) {
            throw new BadRequestException("O CEP é obrigatório para registrar a ocorrência.");
        }
        if (dto.tipoOcorrencia() == null || dto.tipoOcorrencia().isBlank() ||
                dto.nivelGravidade() == null || dto.nivelGravidade().isBlank()) {
            throw new BadRequestException("Campos CEP, tipoOcorrencia e nivelGravidade são obrigatórios.");
        }

        String cepLimpo = dto.cep().replaceAll("[^0-9]", "");
        if (!CEP_PATTERN.matcher(dto.cep()).matches() || cepLimpo.length() != 8) {
            throw new BadRequestException("Formato de CEP inválido. Use XXXXX-XXX ou XXXXXXXX.");
        }

        BigDecimal latParaRegistrar;
        BigDecimal lonParaRegistrar;

        ViaCep.EnderecoCompleto enderecoInfo;
        try {
            enderecoInfo = viaCepService.obterEnderecoCompletoPorCep(cepLimpo);
        } catch (NotFoundException e) {
            throw new BadRequestException("CEP não encontrado ou inválido: " + cepLimpo);
        } catch (ServiceUnavailableException e) {
            throw new ServiceUnavailableException("Erro ao obter dados do ViaCEP para o CEP: " + cepLimpo + ". Detalhe: " + e.getMessage());
        }

        if (enderecoInfo == null || enderecoInfo.coordenadas() == null ||
                enderecoInfo.coordenadas().latitude() == null || enderecoInfo.coordenadas().longitude() == null) {
            throw new ServiceUnavailableException("Não foi possível obter coordenadas (latitude/longitude) completas para o CEP: " + cepLimpo);
        }
        latParaRegistrar = enderecoInfo.coordenadas().latitude();
        lonParaRegistrar = enderecoInfo.coordenadas().longitude();

        // Validações de range para latitude e longitude
        if (latParaRegistrar.compareTo(new BigDecimal("-90")) < 0 || latParaRegistrar.compareTo(new BigDecimal("90")) > 0) {
            throw new BadRequestException("Latitude inválida ("+ latParaRegistrar +") obtida para o CEP. Deve estar entre -90 e 90.");
        }
        if (lonParaRegistrar.compareTo(new BigDecimal("-180")) < 0 || lonParaRegistrar.compareTo(new BigDecimal("180")) > 0) {
            throw new BadRequestException("Longitude inválida ("+ lonParaRegistrar +") obtida para o CEP. Deve estar entre -180 e 180.");
        }

        String tipoOcorrenciaUpper = dto.tipoOcorrencia().toUpperCase();
        if (!TIPOS_OCORRENCIA_VALIDOS.contains(tipoOcorrenciaUpper)) {
            throw new BadRequestException("Tipo de ocorrência inválido: '" + dto.tipoOcorrencia() + "'. Valores permitidos: " + TIPOS_OCORRENCIA_VALIDOS);
        }

        String nivelGravidadeUpper = dto.nivelGravidade().toUpperCase();
        if (!NIVEIS_GRAVIDADE_VALIDOS.contains(nivelGravidadeUpper)) {
            throw new BadRequestException("Nível de gravidade inválido: '" + dto.nivelGravidade() + "'. Valores permitidos: " + NIVEIS_GRAVIDADE_VALIDOS);
        }

        if (cidadeRepository.buscarPorId(dto.idCidade()) == null) {
            throw new NotFoundException("Cidade com ID " + dto.idCidade() + " não encontrada. Não é possível registrar a ocorrência.");
        }

        Ocorrencia ocorrencia = new Ocorrencia();
        ocorrencia.setCep(cepLimpo);
        ocorrencia.setLat(latParaRegistrar);
        ocorrencia.setLon(lonParaRegistrar);
        ocorrencia.setTipoOcorrencia(tipoOcorrenciaUpper);
        ocorrencia.setNivelGravidade(nivelGravidadeUpper);
        ocorrencia.setIdCidade(dto.idCidade());
        ocorrencia.setDataCriacao(LocalDateTime.now());
        ocorrencia.setDeleted(false);

        ocorrenciaRepository.registrar(ocorrencia);
    }

    // buscar, buscarPorCidade, buscarPorId methods remain the same

    public SearchListDto<ResponseOcorrenciaDto> buscar(int page, String tipoOcorrencia, String nivelGravidade, String direction) {
        if (page < 1) {
            throw new BadRequestException("O número da página deve ser maior ou igual a 1.");
        }
        final int PAGE_SIZE = 9;

        SearchResult<Ocorrencia> resultado = ocorrenciaRepository.buscar(tipoOcorrencia, nivelGravidade, direction);

        int totalItems = resultado.totalItems();
        List<Ocorrencia> ocorrencias = resultado.data();
        int start = Math.max((page - 1) * PAGE_SIZE, 0);
        int end = Math.min(start + PAGE_SIZE, totalItems);

        List<ResponseOcorrenciaDto> pageData = List.of();

        if (start < totalItems && !ocorrencias.isEmpty()) {
            pageData = ocorrencias.subList(start, end).stream()
                    .map(this::converterParaResponseDto)
                    .toList();
        }

        return new SearchListDto<>(page, direction, PAGE_SIZE, totalItems, pageData);
    }

    public SearchListDto<ResponseOcorrenciaDto> buscarPorCidade(int idCidade, int page, String tipoOcorrencia, String nivelGravidade, String direction) {
        if (page < 1) {
            throw new BadRequestException("O número da página deve ser maior ou igual a 1.");
        }

        if (idCidade <= 0) {
            throw new BadRequestException("O ID da cidade deve ser um número positivo.");
        }

        final int PAGE_SIZE = 10;

        SearchResult<Ocorrencia> resultado = ocorrenciaRepository.buscarPorIdCidade(
                idCidade,
                tipoOcorrencia,
                nivelGravidade,
                direction
        );

        int totalItems = resultado.totalItems();
        List<Ocorrencia> ocorrencias = resultado.data();

        int start = Math.max((page - 1) * PAGE_SIZE, 0);
        int end = Math.min(start + PAGE_SIZE, totalItems);

        List<ResponseOcorrenciaDto> pageData = List.of();

        if (start < totalItems && !ocorrencias.isEmpty()) {
            int effectiveEnd = Math.min(end, ocorrencias.size());
            if (start >= effectiveEnd) {
                pageData = Collections.emptyList();
            } else {
                pageData = ocorrencias.subList(start, effectiveEnd).stream()
                        .map(this::converterParaResponseDto)
                        .toList();
            }
        } else {
            pageData = Collections.emptyList();
        }

        return new SearchListDto<>(page, direction, PAGE_SIZE, totalItems, pageData);
    }


    public ResponseOcorrenciaDto buscarPorId(int id) {
        var ocorrenciaExistente = ocorrenciaRepository.buscarPorId(id);
        if (ocorrenciaExistente == null) {
            throw new NotFoundException("Ocorrência com o ID " + id + " não encontrado");
        }
        return converterParaResponseDto(ocorrenciaExistente);
    }


    public void atualizar(int id, CreateOcorrenciaDto dto) throws ServiceUnavailableException {
        Ocorrencia ocorrenciaExistente = ocorrenciaRepository.buscarPorId(id);
        if (ocorrenciaExistente == null) {
            throw new NotFoundException("Ocorrência com o ID " + id + " não encontrada para atualização.");
        }

        // Validando o CEP (agora obrigatório para atualização, como em registrar)
        if (dto.cep() == null || dto.cep().isBlank()) {
            throw new BadRequestException("O CEP é obrigatório para atualizar a ocorrência.");
        }
        // Validação dos outros campos obrigatórios (como em registrar)
        if (dto.tipoOcorrencia() == null || dto.tipoOcorrencia().isBlank() ||
                dto.nivelGravidade() == null || dto.nivelGravidade().isBlank()) {
            throw new BadRequestException("Campos CEP, tipoOcorrencia e nivelGravidade são obrigatórios para atualização.");
        }

        String cepLimpo = dto.cep().replaceAll("[^0-9]", "");
        if (!CEP_PATTERN.matcher(dto.cep()).matches() || cepLimpo.length() != 8) {
            throw new BadRequestException("Formato de CEP inválido para atualização. Use XXXXX-XXX ou XXXXXXXX.");
        }

        BigDecimal latParaAtualizar;
        BigDecimal lonParaAtualizar;

        ViaCep.EnderecoCompleto enderecoInfo;
        try {
            enderecoInfo = viaCepService.obterEnderecoCompletoPorCep(cepLimpo);
        } catch (NotFoundException e) {
            throw new BadRequestException("CEP não encontrado ou inválido para atualização: " + cepLimpo);
        } catch (ServiceUnavailableException e) {
            throw new ServiceUnavailableException("Erro ao obter dados do ViaCEP para o CEP de atualização: " + cepLimpo + ". Detalhe: " + e.getMessage());
        }

        if (enderecoInfo == null || enderecoInfo.coordenadas() == null ||
                enderecoInfo.coordenadas().latitude() == null || enderecoInfo.coordenadas().longitude() == null) {
            throw new ServiceUnavailableException("Não foi possível obter coordenadas (latitude/longitude) completas para o CEP de atualização: " + cepLimpo);
        }
        latParaAtualizar = enderecoInfo.coordenadas().latitude();
        lonParaAtualizar = enderecoInfo.coordenadas().longitude();

        // Validações de range para latitude e longitude (como em registrar)
        if (latParaAtualizar.compareTo(new BigDecimal("-90")) < 0 || latParaAtualizar.compareTo(new BigDecimal("90")) > 0) {
            throw new BadRequestException("Latitude inválida ("+ latParaAtualizar +") obtida para o CEP de atualização. Deve estar entre -90 e 90.");
        }
        if (lonParaAtualizar.compareTo(new BigDecimal("-180")) < 0 || lonParaAtualizar.compareTo(new BigDecimal("180")) > 0) {
            throw new BadRequestException("Longitude inválida ("+ lonParaAtualizar +") obtida para o CEP de atualização. Deve estar entre -180 e 180.");
        }

        String tipoOcorrenciaUpper = dto.tipoOcorrencia().toUpperCase();
        if (!TIPOS_OCORRENCIA_VALIDOS.contains(tipoOcorrenciaUpper)) {
            throw new BadRequestException("Tipo de ocorrência inválido para atualização: '" + dto.tipoOcorrencia() + "'. Valores permitidos: " + TIPOS_OCORRENCIA_VALIDOS);
        }

        String nivelGravidadeUpper = dto.nivelGravidade().toUpperCase();
        if (!NIVEIS_GRAVIDADE_VALIDOS.contains(nivelGravidadeUpper)) {
            throw new BadRequestException("Nível de gravidade inválido para atualização: '" + dto.nivelGravidade() + "'. Valores permitidos: " + NIVEIS_GRAVIDADE_VALIDOS);
        }

        if (cidadeRepository.buscarPorId(dto.idCidade()) == null) {
            throw new NotFoundException("Cidade com ID " + dto.idCidade() + " não encontrada. Não é possível atualizar a ocorrência.");
        }

        Ocorrencia ocorrenciaParaAtualizar = new Ocorrencia();
        ocorrenciaParaAtualizar.setIdOcorrencia(id); // Mantém o ID da ocorrência existente
        ocorrenciaParaAtualizar.setCep(cepLimpo);
        ocorrenciaParaAtualizar.setLat(latParaAtualizar);
        ocorrenciaParaAtualizar.setLon(lonParaAtualizar);
        ocorrenciaParaAtualizar.setTipoOcorrencia(tipoOcorrenciaUpper);
        ocorrenciaParaAtualizar.setNivelGravidade(nivelGravidadeUpper);
        ocorrenciaParaAtualizar.setIdCidade(dto.idCidade());


        ocorrenciaRepository.atualizar(id, ocorrenciaParaAtualizar);
    }

    public void deletar(int id) {
        var ocorrenciaExistente = ocorrenciaRepository.buscarPorId(id);

        if (ocorrenciaExistente == null) {
            throw new NotFoundException("Nenhuma ocorrência com ID " + id +" encontrada.");
        }
        ocorrenciaRepository.deletar(id);
    }

    private ResponseOcorrenciaDto converterParaResponseDto(Ocorrencia ocorrencia) {
        if (ocorrencia == null) return null;
        return new ResponseOcorrenciaDto(
                ocorrencia.getIdOcorrencia(),
                ocorrencia.isDeleted(),
                ocorrencia.getDataCriacao(),
                ocorrencia.getCep(),
                ocorrencia.getLat(),
                ocorrencia.getLon(),
                ocorrencia.getTipoOcorrencia(),
                ocorrencia.getNivelGravidade(),
                ocorrencia.getIdCidade()
        );
    }
}