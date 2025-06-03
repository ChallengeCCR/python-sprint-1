package fiap.tds.dtos.abrigoDto;

public record CreateAbrigoDto(
        String nomeAbrigo,
        String cep,
        String enderecoAbrigo,
        Integer capacidadeMaxima,
        String statusFuncionamento,
        String nivelSegurancaAtual,
        String telefoneContato,
        int idCidade
) {
    public CreateAbrigoDto {
    }
}