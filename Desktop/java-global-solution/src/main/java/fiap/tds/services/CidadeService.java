package fiap.tds.services;

import fiap.tds.dtos.SearchListDto;
import fiap.tds.dtos.cidadeDto.CreateCidadeDto;
import fiap.tds.dtos.cidadeDto.ResponseCidadeDto; // Usando o DTO definido acima
import fiap.tds.entities.Cidade;
import fiap.tds.exceptions.BadRequestException;
import fiap.tds.exceptions.NotFoundException;
import fiap.tds.exceptions.ServiceUnavailableException;
import fiap.tds.infrastructure.ViaCep;
import fiap.tds.repositories.CidadeRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.regex.Pattern;

public class CidadeService {

    private final CidadeRepository cidadeRepository = new CidadeRepository();
    private final ViaCep viaCepService = new ViaCep();
    private static final int PAGE_SIZE = 10;
    private static final Pattern CEP_PATTERN = Pattern.compile("^\\d{5}-?\\d{3}$");

    public void registrar(CreateCidadeDto dto) throws ServiceUnavailableException {

        // Validando o cep da cidade
        if (dto.cep() == null || dto.cep().isBlank()) {
            throw new BadRequestException("O CEP é obrigatório.");
        }
        String cepLimpo = dto.cep().replaceAll("[^0-9]", "");
        if (!CEP_PATTERN.matcher(dto.cep()).matches() || cepLimpo.length() != 8) {
            throw new BadRequestException("Formato de CEP inválido. Use XXXXX-XXX ou XXXXXXXX.");
        }

        // Validando a resposta da API do ViaCep
        ViaCep.EnderecoCompleto enderecoInfo;
        try {
            enderecoInfo = viaCepService.obterEnderecoCompletoPorCep(cepLimpo);
        } catch (NotFoundException e) {
            // Se o CEP não foi encontrado (ViaCEP retornou 404), é um erro do CLIENTE (BadRequest)
            throw new BadRequestException("CEP não encontrado ou inválido: " + cepLimpo);
        } catch (ServiceUnavailableException e) {
            throw new ServiceUnavailableException("Erro ao obter dados para o CEP: " + cepLimpo);
        }

        if (enderecoInfo == null || enderecoInfo.nomeCidade() == null || enderecoInfo.coordenadas() == null) {
            throw new ServiceUnavailableException("Não foi possível obter informações completas do endereço para o CEP: " + cepLimpo);
        }

        String nomeCidadeFinal = (dto.nomeCidade() != null && !dto.nomeCidade().isBlank()) ? dto.nomeCidade() : enderecoInfo.nomeCidade();
        BigDecimal latitude = enderecoInfo.coordenadas().latitude();
        BigDecimal longitude = enderecoInfo.coordenadas().longitude();

        if (latitude == null || longitude == null) {
            throw new ServiceUnavailableException("Não foi possível obter coordenadas (latitude/longitude) para o CEP: " + cepLimpo);
        }
        if (latitude.compareTo(new BigDecimal("-90")) < 0 || latitude.compareTo(new BigDecimal("90")) > 0) {
            throw new BadRequestException("Latitude inválida ("+ latitude +") obtida para o CEP. Deve estar entre -90 e 90.");
        }
        if (longitude.compareTo(new BigDecimal("-180")) < 0 || longitude.compareTo(new BigDecimal("180")) > 0) {
            throw new BadRequestException("Longitude inválida ("+ longitude +") obtida para o CEP. Deve estar entre -180 e 180.");
        }

        Cidade cidade = new Cidade();
        cidade.setCepCidade(cepLimpo); // <<< ADICIONADO: Setar o CEP na entidade
        cidade.setNomeCidade(nomeCidadeFinal);
        cidade.setLat(latitude);
        cidade.setLon(longitude);
        cidade.setDataCriacao(LocalDateTime.now());
        cidade.setDeleted(false);
        // Inicializar contagens, pois são NOT NULL na tabela
        cidade.setQuantidadeAbrigos(0);    // <<< ADICIONADO
        cidade.setQuantidadeOcorrencias(0); // <<< ADICIONADO

        cidadeRepository.registrar(cidade); // O ID será setado no objeto 'cidade' pelo repository

    }

    public SearchListDto<ResponseCidadeDto> buscar(int page, String nome, String direction) {
        if (page < 1) {
            throw new BadRequestException("O número da página deve ser maior ou igual a 1.");
        }

        var resultadoRepository = cidadeRepository.buscar(nome, direction); // Supondo que buscar não pagina internamente

        List<Cidade> todasCidades = resultadoRepository.data();
        int totalItems = resultadoRepository.totalItems();

        int start = Math.max((page - 1) * PAGE_SIZE, 0);
        List<ResponseCidadeDto> pageData = todasCidades.stream()
                .skip(start)
                .limit(PAGE_SIZE)
                .map(this::converterCidadeParaDto)
                .toList();

        return new SearchListDto<>(page, direction, PAGE_SIZE, totalItems, pageData);
    }

    public ResponseCidadeDto buscarPorId(int id) {
        Cidade cidade = cidadeRepository.buscarPorId(id);
        if (cidade == null) {
            throw new NotFoundException("Cidade com o ID " + id + " não encontrada.");
        }
        return converterCidadeParaDto(cidade);
    }

    public void atualizar(int id, CreateCidadeDto dto) throws ServiceUnavailableException {
        ResponseCidadeDto cidadeExistenteDto = buscarPorId(id); // Isso já lança NotFoundException se não existir

        String cepAtualizado = cidadeExistenteDto.cepCidade(); // Mantém o CEP existente por padrão
        String nomeCidadeAtualizado = dto.nomeCidade() != null && !dto.nomeCidade().isBlank() ? dto.nomeCidade() : cidadeExistenteDto.nomeCidade();
        BigDecimal latAtualizada = cidadeExistenteDto.lat();
        BigDecimal lonAtualizada = cidadeExistenteDto.lon();

        if (dto.cep() != null && !dto.cep().isBlank()) {
            String cepLimpoNovo = dto.cep().replaceAll("[^0-9]", "");
            if (!CEP_PATTERN.matcher(dto.cep()).matches() || cepLimpoNovo.length() != 8) {
                throw new BadRequestException("Formato de CEP inválido para atualização. Use XXXXX-XXX ou XXXXXXXX.");
            }
            if (!cepLimpoNovo.equals(cidadeExistenteDto.cepCidade())) { // Só busca no ViaCEP se o CEP realmente mudou
                ViaCep.EnderecoCompleto enderecoInfo;
                try {
                    enderecoInfo = viaCepService.obterEnderecoCompletoPorCep(cepLimpoNovo);
                } catch (NotFoundException e) {
                    throw new BadRequestException("Novo CEP " + cepLimpoNovo + " não encontrado para atualização.");
                } catch (ServiceUnavailableException e) {
                    throw new ServiceUnavailableException("Serviço ViaCEP indisponível ao atualizar dados para o CEP: " + cepLimpoNovo + ". Detalhe: " + e.getMessage());
                }

                if (enderecoInfo == null || enderecoInfo.coordenadas() == null) {
                    throw new ServiceUnavailableException("Não foi possível obter informações completas do endereço para o CEP de atualização: " + cepLimpoNovo);
                }
                latAtualizada = enderecoInfo.coordenadas().latitude();
                lonAtualizada = enderecoInfo.coordenadas().longitude();
                if (dto.nomeCidade() == null || dto.nomeCidade().isBlank()) {
                    nomeCidadeAtualizado = enderecoInfo.nomeCidade();
                }
                cepAtualizado = cepLimpoNovo;
            }
        }

        if (nomeCidadeAtualizado == null || nomeCidadeAtualizado.isBlank()) {
            throw new BadRequestException("O nome da cidade é obrigatório para atualização.");
        }
        if (latAtualizada == null || lonAtualizada == null) {
            throw new ServiceUnavailableException("Não foi possível obter coordenadas (latitude/longitude) para a atualização.");
        }
        if (latAtualizada.compareTo(new BigDecimal("-90")) < 0 || latAtualizada.compareTo(new BigDecimal("90")) > 0) {
            throw new BadRequestException("Latitude inválida ("+ latAtualizada +") para atualização. Deve estar entre -90 e 90.");
        }
        if (lonAtualizada.compareTo(new BigDecimal("-180")) < 0 || lonAtualizada.compareTo(new BigDecimal("180")) > 0) {
            throw new BadRequestException("Longitude inválida ("+ lonAtualizada +") para atualização. Deve estar entre -180 e 180.");
        }

        Cidade cidadeParaAtualizar = new Cidade();
        cidadeParaAtualizar.setIdCidade(id);
        cidadeParaAtualizar.setCepCidade(cepAtualizado);
        cidadeParaAtualizar.setNomeCidade(nomeCidadeAtualizado);
        cidadeParaAtualizar.setLat(latAtualizada);
        cidadeParaAtualizar.setLon(lonAtualizada);

        cidadeRepository.atualizar(id, cidadeParaAtualizar);
    }

    public void deletar(int id) {
        buscarPorId(id); // Verifica se a cidade existe antes de tentar deletar
        cidadeRepository.deletar(id);
    }

    private ResponseCidadeDto converterCidadeParaDto(Cidade cidade) {
        if (cidade == null) return null;
        return new ResponseCidadeDto(
                cidade.getIdCidade(),
                cidade.isDeleted(),
                cidade.getDataCriacao(),
                cidade.getCepCidade(),      // Ordem conforme ResponseCidadeDto
                cidade.getNomeCidade(),
                cidade.getQuantidadeOcorrencias(),
                cidade.getQuantidadeAbrigos(),
                cidade.getLat(),
                cidade.getLon()
        );
    }
}