package fiap.tds.dtos.abrigoDto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

// Campos retornados ao consultar um abrigo
public record ResponseAbrigoDto(
        int idAbrigo,
        boolean deleted,
        LocalDateTime dataCriacao,
        String cep,
        String nomeAbrigo,
        String enderecoAbrigo,
        int capacidadeMaxima,
        String statusFuncionamento,
        String nivelSegurancaAtual,
        String telefoneContato,
        int idCidade,
        BigDecimal lat,
        BigDecimal lon
) {
    public ResponseAbrigoDto {
    }
}