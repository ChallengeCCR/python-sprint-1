package fiap.tds.repositories;

import fiap.tds.dtos.SearchResult;
import fiap.tds.entities.Cidade;
import fiap.tds.infrastructure.DatabaseConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CidadeRepository {

    public static final Logger logger = LogManager.getLogger(CidadeRepository.class);

    // Função para registrar uma cidade
    public void registrar(Cidade cidade) {
        String query = "INSERT INTO T_POSE_CIDADE (NM_CIDADE, DT_CRIACAO, CEP, LAT, LON, QT_ABRIGOS, QT_OCORRENCIAS, DELETED) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConfig.getConnection()){
             PreparedStatement stmt = conn.prepareStatement(query);

            stmt.setString(1, cidade.getNomeCidade());
            stmt.setTimestamp(2, Timestamp.valueOf(cidade.getDataCriacao()));
            stmt.setString(3, cidade.getCepCidade());
            stmt.setBigDecimal(4, cidade.getLat());
            stmt.setBigDecimal(5, cidade.getLon());
            stmt.setInt(6, cidade.getQuantidadeAbrigos());    // Valor da entidade (ex: 0)
            stmt.setInt(7, cidade.getQuantidadeOcorrencias()); // Valor da entidade (ex: 0)
            stmt.setInt(8, cidade.isDeleted() ? 1 : 0);

            int affectedRows = stmt.executeUpdate();

            if (affectedRows > 0) {
                logger.info("✅ Cidade registrada com sucesso, ID: {}", cidade.getIdCidade());
            }

        } catch (SQLException e) {
            logger.error("❌ Erro ao registrar cidade: {}", e.getMessage());
        }
    }

    // Função para buscar cidades com filtros
    public SearchResult<Cidade> buscar(String nomeCidade, String direction) {
        List<Cidade> cidadesBuscadas = new ArrayList<>();
        int totalItems = 0;

        // Query para buscar cidades, calculando QT_ABRIGOS e QT_OCORRENCIAS com subqueries
        StringBuilder queryBuilder = new StringBuilder(
                "SELECT c.ID_CIDADE, c.DELETED, c.DT_CRIACAO, c.NM_CIDADE, c.CEP, c.LAT, c.LON, " +
                        " (SELECT COUNT(*) FROM T_POSE_ABRIGO a WHERE a.ID_CIDADE = c.ID_CIDADE AND a.DELETED = 0) as QT_ABRIGOS_CALC, " + // Considerando abrigos não deletados
                        " (SELECT COUNT(*) FROM T_POSE_OCORRENCIA o WHERE o.ID_CIDADE = c.ID_CIDADE AND o.DELETED = 0) as QT_OCORRENCIAS_CALC " + // Considerando ocorrências não deletadas
                        "FROM T_POSE_CIDADE c " +
                        "WHERE c.DELETED = 0" // Apenas cidades não deletadas
        );

        if (nomeCidade != null && !nomeCidade.isBlank()) {
            queryBuilder.append(" AND UPPER(c.NM_CIDADE) LIKE UPPER(?)");
        }

        if ("desc".equalsIgnoreCase(direction)) {
            queryBuilder.append(" ORDER BY c.NM_CIDADE DESC, c.ID_CIDADE DESC");
        } else {
            queryBuilder.append(" ORDER BY c.NM_CIDADE ASC, c.ID_CIDADE ASC");
        }

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(queryBuilder.toString())) {

            int paramIndex = 1;
            if (nomeCidade != null && !nomeCidade.isBlank()) {
                stmt.setString(paramIndex++, "%" + nomeCidade + "%");
            }

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Cidade cidade = new Cidade();
                cidade.setIdCidade(rs.getInt("ID_CIDADE"));
                cidade.setDeleted(rs.getInt("DELETED") == 1);
                cidade.setDataCriacao(rs.getTimestamp("DT_CRIACAO").toLocalDateTime());
                cidade.setCepCidade(rs.getString("CEP"));
                cidade.setNomeCidade(rs.getString("NM_CIDADE"));
                cidade.setLat(rs.getBigDecimal("LAT"));
                cidade.setLon(rs.getBigDecimal("LON"));

                // Usar os valores calculados das subqueries
                cidade.setQuantidadeAbrigos(rs.getInt("QT_ABRIGOS_CALC"));
                cidade.setQuantidadeOcorrencias(rs.getInt("QT_OCORRENCIAS_CALC"));
                cidadesBuscadas.add(cidade);
            }
            totalItems = cidadesBuscadas.size();
            logger.info("✅ Cidades buscadas. {} resultados encontrados.", totalItems);

        } catch (SQLException e) {
            logger.error("❌ Erro ao buscar cidades: {}", e.getMessage(), e);
        }
        return new SearchResult<>(cidadesBuscadas, totalItems);
    }

    // Função para buscar uma cidade por ID
    public Cidade buscarPorId(int id) {
        // Query para buscar cidade por ID, calculando QT_ABRIGOS e QT_OCORRENCIAS com subqueries
        String query = "SELECT c.ID_CIDADE, c.DELETED, c.DT_CRIACAO, c.NM_CIDADE, c.CEP, c.LAT, c.LON, " +
                " (SELECT COUNT(*) FROM T_POSE_ABRIGO a WHERE a.ID_CIDADE = c.ID_CIDADE AND a.DELETED = 0) as QT_ABRIGOS_CALC, " +
                " (SELECT COUNT(*) FROM T_POSE_OCORRENCIA o WHERE o.ID_CIDADE = c.ID_CIDADE AND o.DELETED = 0) as QT_OCORRENCIAS_CALC " +
                "FROM T_POSE_CIDADE c " +
                "WHERE c.ID_CIDADE = ? AND c.DELETED = 0";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Cidade cidade = new Cidade();
                cidade.setIdCidade(rs.getInt("ID_CIDADE"));
                cidade.setDeleted(rs.getBoolean("DELETED"));
                cidade.setDataCriacao(rs.getTimestamp("DT_CRIACAO").toLocalDateTime());
                cidade.setCepCidade(rs.getString("CEP"));
                cidade.setNomeCidade(rs.getString("NM_CIDADE"));
                cidade.setLat(rs.getBigDecimal("LAT"));
                cidade.setLon(rs.getBigDecimal("LON"));

                // Usar os valores calculados das subqueries
                cidade.setQuantidadeAbrigos(rs.getInt("QT_ABRIGOS_CALC"));
                cidade.setQuantidadeOcorrencias(rs.getInt("QT_OCORRENCIAS_CALC"));
                logger.info("✅ Cidade com ID {} buscada com sucesso.", id);
                return cidade;
            }
        } catch (SQLException e) {
            logger.error("❌ Erro ao buscar cidade por ID {}: {}", id, e.getMessage(), e);
        }
        return null;
    }

    // Função para atualizar a cidade
    public void atualizar(int id, Cidade cidade) {
        String query = "UPDATE T_POSE_CIDADE SET NM_CIDADE = ?, CEP = ?, LAT = ?, LON = ? " +
                "WHERE ID_CIDADE = ? AND DELETED = 0";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, cidade.getNomeCidade());
            stmt.setString(2, cidade.getCepCidade());
            stmt.setBigDecimal(3, cidade.getLat());
            stmt.setBigDecimal(4, cidade.getLon());
            stmt.setInt(5, id);

            int affectedRows = stmt.executeUpdate();

            if (affectedRows > 0) {
                logger.info("✅ Cidade com ID {} atualizada com sucesso.", id);
            } else {
                logger.warn("⚠️ Nenhuma cidade ativa encontrada com ID {} para atualizar ou dados não foram alterados.", id);
            }
        } catch (SQLException e) {
            logger.error("❌ Erro ao atualizar cidade com ID {}: {}", id, e.getMessage(), e);
        }
    }

    // Função para deletar uma cidade
    public void deletar(int id) {
        logger.info("Iniciando deleção lógica da cidade por ID {}", id);
        String query = "UPDATE T_POSE_CIDADE SET DELETED = 1 WHERE ID_CIDADE = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, id);
            int affectedRows = stmt.executeUpdate();

            if (affectedRows > 0) {
                logger.info("✅ Cidade com ID {} marcada como deletada com sucesso.", id);
            }
        } catch (SQLException e) {
            logger.error("❌ Erro ao marcar cidade com ID {} como deletada: {}", id, e.getMessage(), e);
        }
    }
}