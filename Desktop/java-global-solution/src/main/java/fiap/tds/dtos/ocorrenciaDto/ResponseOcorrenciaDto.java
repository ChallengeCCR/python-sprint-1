package fiap.tds.dtos.ocorrenciaDto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ResponseOcorrenciaDto(
        int idOcorrencia,
        boolean deleted,
        LocalDateTime dataCriacao,
        String cep,
        BigDecimal lat,
        BigDecimal lon,
        String tipoOcorrencia,
        String nivelGravidade,
        int idCidade
) {
    public ResponseOcorrenciaDto {
    }
}
