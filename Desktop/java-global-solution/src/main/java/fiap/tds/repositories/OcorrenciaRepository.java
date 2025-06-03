package fiap.tds.repositories;

import fiap.tds.dtos.SearchResult;
import fiap.tds.entities.Ocorrencia;
import fiap.tds.entities.Usuario;
import fiap.tds.infrastructure.DatabaseConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class OcorrenciaRepository {

    public static final Logger logger = LogManager.getLogger(OcorrenciaRepository.class);

    public void registrar(Ocorrencia ocorrencia) {
        var query = "INSERT INTO T_POSE_OCORRENCIA (DT_CRIACAO, CEP, LAT, LON, TP_OCORRENCIA, NV_GRAVIDADE, ID_CIDADE) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (var conn = DatabaseConfig.getConnection()) {
            var stmt = conn.prepareStatement(query);

            // Definindo os parâmetros
            stmt.setTimestamp(1, Timestamp.valueOf(ocorrencia.getDataCriacao()));
            stmt.setString(2, ocorrencia.getCep());
            stmt.setBigDecimal(3, ocorrencia.getLat());
            stmt.setBigDecimal(4, ocorrencia.getLon());
            stmt.setString(5, ocorrencia.getTipoOcorrencia());
            stmt.setString(6, ocorrencia.getNivelGravidade());
            stmt.setInt(7, ocorrencia.getIdCidade());

            var res = stmt.executeUpdate();

            if (res > 0) {
                logger.info("✅ Ocorrência registrada com sucesso");
            }

        } catch (SQLException e) {
            logger.error("❌ Erro ao registrar ocorrência: {}", e.getMessage(), e);
        }
    }

    public SearchResult<Ocorrencia> buscar(String tipoOcorrencia, String nivelGravidade, String direction) {
        List<Ocorrencia> ocorrenciasBuscadas = new ArrayList<>();
        int totalItems = 0;

        // Usar SELECT * é geralmente desaconselhado. Listar colunas é melhor.
        // Mas seguindo o padrão fornecido:
        var query = new StringBuilder("SELECT * FROM T_POSE_OCORRENCIA WHERE DELETED = 0");

        // Filtros
        if (tipoOcorrencia != null && !tipoOcorrencia.isBlank()) {
            query.append(" AND UPPER(TP_OCORRENCIA) LIKE UPPER(?)");
        }
        if (nivelGravidade != null && !nivelGravidade.isBlank()) {
            query.append(" AND UPPER(NV_GRAVIDADE) LIKE UPPER(?)");
        }

        // Ordenação
        if ("desc".equalsIgnoreCase(direction)) {
            query.append(" ORDER BY ID_OCORRENCIA DESC");
        } else {
            // Padrão é ASC se direction for nulo, vazio, ou "asc"
            query.append(" ORDER BY ID_OCORRENCIA ASC");
        }

        try (var conn = DatabaseConfig.getConnection();
             var stmt = conn.prepareStatement(query.toString())) {

            int paramIndex = 1;
            if (tipoOcorrencia != null && !tipoOcorrencia.isBlank()) {
                stmt.setString(paramIndex++, "%" + tipoOcorrencia + "%");
            }
            if (nivelGravidade != null && !nivelGravidade.isBlank()) {
                stmt.setString(paramIndex++, "%" + nivelGravidade + "%");
            }

            var rs = stmt.executeQuery();

            while (rs.next()) {
                Ocorrencia ocorrencia = new Ocorrencia();
                ocorrencia.setIdOcorrencia(rs.getInt("ID_OCORRENCIA"));
                ocorrencia.setDeleted(rs.getInt("DELETED") == 1); // DELETED é NUMERIC(1)
                ocorrencia.setDataCriacao(rs.getTimestamp("DT_CRIACAO").toLocalDateTime()); // importante não esquecer isso ;)
                ocorrencia.setCep(rs.getString("CEP"));
                ocorrencia.setLat(rs.getBigDecimal("LAT"));
                ocorrencia.setLon(rs.getBigDecimal("LON"));
                ocorrencia.setTipoOcorrencia(rs.getString("TP_OCORRENCIA"));
                ocorrencia.setNivelGravidade(rs.getString("NV_GRAVIDADE"));
                ocorrencia.setIdCidade(rs.getInt("ID_CIDADE"));
                ocorrenciasBuscadas.add(ocorrencia);
            }

            logger.info("✅ Ocorrências buscadas com sucesso");
            totalItems = ocorrenciasBuscadas.size();

        } catch (SQLException e) {
            logger.error("❌ Erro ao buscar ocorrências: {}", e.getMessage(), e);
        }
        return new SearchResult<>(ocorrenciasBuscadas, totalItems);
    }

    public SearchResult<Ocorrencia> buscarPorIdCidade(int idCidade, String tipoOcorrencia, String nivelGravidade, String direction) {
        List<Ocorrencia> ocorrenciasBuscadas = new ArrayList<>();
        int totalItems = 0;

        var query = new StringBuilder("SELECT * FROM T_POSE_OCORRENCIA WHERE DELETED = 0 AND ID_CIDADE = ?");

        // Adiciona filtros opcionais
        if (tipoOcorrencia != null && !tipoOcorrencia.isBlank()) {
            query.append(" AND UPPER(TP_OCORRENCIA) LIKE UPPER(?)");
        }
        if (nivelGravidade != null && !nivelGravidade.isBlank()) {
            query.append(" AND UPPER(NV_GRAVIDADE) LIKE UPPER(?)");
        }

        // Ordenação
        if ("desc".equalsIgnoreCase(direction)) {
            query.append(" ORDER BY ID_OCORRENCIA DESC");
        } else {
            query.append(" ORDER BY ID_OCORRENCIA ASC");
        }

        try (var conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query.toString())) {

            int paramIndex = 1;
            stmt.setInt(paramIndex++, idCidade); // Parâmetro obrigatório idCidade

            // Parâmetros opcionais
            if (tipoOcorrencia != null && !tipoOcorrencia.isBlank()) {
                stmt.setString(paramIndex++, "%" + tipoOcorrencia + "%");
            }
            if (nivelGravidade != null && !nivelGravidade.isBlank()) {
                stmt.setString(paramIndex++, "%" + nivelGravidade + "%");
            }

            var rs = stmt.executeQuery();

            while (rs.next()) {
                Ocorrencia ocorrencia = new Ocorrencia();
                ocorrencia.setIdOcorrencia(rs.getInt("ID_OCORRENCIA"));
                ocorrencia.setDeleted(rs.getInt("DELETED") == 1);
                ocorrencia.setDataCriacao(rs.getTimestamp("DT_CRIACAO").toLocalDateTime());
                ocorrencia.setCep(rs.getString("CEP"));
                ocorrencia.setLat(rs.getBigDecimal("LAT"));
                ocorrencia.setLon(rs.getBigDecimal("LON"));
                ocorrencia.setTipoOcorrencia(rs.getString("TP_OCORRENCIA"));
                ocorrencia.setNivelGravidade(rs.getString("NV_GRAVIDADE"));
                ocorrencia.setIdCidade(rs.getInt("ID_CIDADE"));
                ocorrenciasBuscadas.add(ocorrencia);
            }

            logger.info("✅ Ocorrências buscadas com sucesso para a cidade ID: {}", idCidade);
            totalItems = ocorrenciasBuscadas.size();

        } catch (SQLException e) {
            logger.error("❌ Erro ao buscar ocorrências para a cidade ID {}: {}", idCidade, e.getMessage(), e);
        }
        return new SearchResult<>(ocorrenciasBuscadas, totalItems);
    }

    // Função para buscar por ID
    public Ocorrencia buscarPorId(int id){
        var query = "SELECT * FROM T_POSE_OCORRENCIA WHERE ID_OCORRENCIA = ? AND DELETED = 0";

        try (var conn = DatabaseConfig.getConnection();
             var stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, id);

            var rs = stmt.executeQuery();

            if (rs.next()) {
                Ocorrencia ocorrencia = new Ocorrencia();
                ocorrencia.setIdOcorrencia(rs.getInt("ID_OCORRENCIA"));
                ocorrencia.setDeleted(rs.getBoolean("DELETED"));
                ocorrencia.setDataCriacao(rs.getTimestamp("DT_CRIACAO").toLocalDateTime());
                ocorrencia.setCep(rs.getString("CEP"));
                ocorrencia.setLat(rs.getBigDecimal("LAT"));
                ocorrencia.setLon(rs.getBigDecimal("LON"));
                ocorrencia.setTipoOcorrencia(rs.getString("TP_OCORRENCIA"));
                ocorrencia.setNivelGravidade(rs.getString("NV_GRAVIDADE"));
                ocorrencia.setIdCidade(rs.getInt("ID_CIDADE"));

                logger.info("✅ Ocorrência com ID {} buscada com sucesso.", id);
                return ocorrencia;
            }
        } catch (SQLException e) {
            logger.error("❌ Erro ao buscar ocorrência: {}", e.getMessage());
        }
        return null;
    }

    // Função para atualizar uma ocorrência
    public void atualizar(int id, Ocorrencia ocorrencia) {
        String query = "UPDATE T_POSE_OCORRENCIA SET CEP = ?, LAT = ?, LON = ?, TP_OCORRENCIA = ?, NV_GRAVIDADE = ?, ID_CIDADE = ? " +
                "WHERE ID_OCORRENCIA = ? AND DELETED = 0";

        try (var conn = DatabaseConfig.getConnection();
             var stmt = conn.prepareStatement(query)) {

            stmt.setString(1, ocorrencia.getCep());
            stmt.setBigDecimal(2, ocorrencia.getLat());
            stmt.setBigDecimal(3, ocorrencia.getLon());
            stmt.setString(4, ocorrencia.getTipoOcorrencia());
            stmt.setString(5, ocorrencia.getNivelGravidade());
            stmt.setInt(6, ocorrencia.getIdCidade());
            stmt.setInt(7, id);

            int affectedRows = stmt.executeUpdate();

            if (affectedRows > 0) {
                logger.info("✅ Ocorrência com ID {} atualizada com sucesso.", id);
            }

        } catch (SQLException e) {
            logger.error("❌ Erro ao atualizar ocorrência com ID {} \n{}", id, e.getMessage(), e);
        }
    }

    public void deletar(int id) {
        logger.info("Iniciando deleção lógica da ocorrência por ID {}", id);
        String query = "UPDATE T_POSE_OCORRENCIA SET DELETED = 1 WHERE ID_OCORRENCIA = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, id);
            int affectedRows = stmt.executeUpdate();

            if (affectedRows > 0) {
                logger.info("✅ Ocorrência com ID {} deletada com sucesso.", id);
            }
        } catch (SQLException e) {
            logger.error("❌ Erro ao deletar cidade com ID {}: {}", id, e.getMessage(), e);
        }
    }


}
