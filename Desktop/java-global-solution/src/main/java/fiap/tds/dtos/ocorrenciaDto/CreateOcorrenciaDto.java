package fiap.tds.dtos.ocorrenciaDto;

import java.math.BigDecimal;

public record CreateOcorrenciaDto(
        String cep,
        String tipoOcorrencia,
        String nivelGravidade,
        int idCidade
) {
    public CreateOcorrenciaDto {
    }
}
