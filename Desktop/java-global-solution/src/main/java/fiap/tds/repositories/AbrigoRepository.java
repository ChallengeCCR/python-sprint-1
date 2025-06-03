package fiap.tds.repositories;

import fiap.tds.dtos.SearchResult;
import fiap.tds.entities.Abrigo;
import fiap.tds.infrastructure.DatabaseConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AbrigoRepository {

    public static final Logger logger = LogManager.getLogger(AbrigoRepository.class);

    // Função para registrar um abrigo
    public void registrar(Abrigo abrigo) {
        String query = "INSERT INTO T_POSE_ABRIGO (NM_ABRIGO, DT_CRIACAO, CEP, ENDERECO, " +
                "QT_MAXIMA_PESSOAS, ST_FUNCIONAMENTO, NV_SEGURANCA_ATUAL, TELEFONE_CONTATO, LAT, LON, ID_CIDADE) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, abrigo.getNomeAbrigo());
            stmt.setTimestamp(2, Timestamp.valueOf(abrigo.getDataCriacao()));
            stmt.setString(3, abrigo.getCep());
            stmt.setString(4, abrigo.getEnderecoAbrigo());
            stmt.setInt(5, abrigo.getCapacidadeMaxima());
            stmt.setString(6, abrigo.getStatusFuncionamento());
            stmt.setString(7, abrigo.getNivelSegurancaAtual());
            stmt.setString(8, abrigo.getTelefoneContato());
            stmt.setBigDecimal(9, abrigo.getLat());
            stmt.setBigDecimal(10, abrigo.getLon());
            stmt.setInt(11, abrigo.getIdCidade());

            int affectedRows = stmt.executeUpdate();

            if (affectedRows > 0) {
                logger.info("✅ Abrigo registrado com sucesso, ID: {}", abrigo.getIdAbrigo());
            }
        } catch (SQLException e) {
            logger.error("❌ Erro ao registrar abrigo: {}", e.getMessage(), e);
        }
    }

    // Função para buscar abrigos com filtros
    public SearchResult<Abrigo> buscar(String nomeAbrigo, String cep, String statusFuncionamento, String direction) {
        List<Abrigo> abrigosBuscados = new ArrayList<>();
        int totalItems = 0;

        StringBuilder queryBuilder = new StringBuilder(
                "SELECT ID_ABRIGO, DELETED, DT_CRIACAO, NM_ABRIGO, CEP, ENDERECO, " +
                        "QT_MAXIMA_PESSOAS, ST_FUNCIONAMENTO, NV_SEGURANCA_ATUAL, TELEFONE_CONTATO, LAT, LON, ID_CIDADE " +
                        "FROM T_POSE_ABRIGO " +
                        "WHERE DELETED = 0"
        );

        List<Object> params = new ArrayList<>();

        if (nomeAbrigo != null && !nomeAbrigo.isBlank()) {
            queryBuilder.append(" AND UPPER(NM_ABRIGO) LIKE UPPER(?)");
            params.add("%" + nomeAbrigo + "%");
        }
        if (cep != null && !cep.isBlank()) {
            queryBuilder.append(" AND CEP = ?");
            params.add(cep);
        }
        if (statusFuncionamento != null && !statusFuncionamento.isBlank()) {
            queryBuilder.append(" AND UPPER(ST_FUNCIONAMENTO) LIKE UPPER(?)");
            params.add("%" + statusFuncionamento + "%");
        }

        if ("desc".equalsIgnoreCase(direction)) {
            queryBuilder.append(" ORDER BY NM_ABRIGO DESC, ID_ABRIGO DESC");
        } else {
            queryBuilder.append(" ORDER BY NM_ABRIGO ASC, ID_ABRIGO ASC");
        }

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(queryBuilder.toString())) {

            int paramIndex = 1;
            for (Object param : params) {
                stmt.setObject(paramIndex++, param);
            }

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Abrigo abrigo = new Abrigo();
                abrigo.setIdAbrigo(rs.getInt("ID_ABRIGO"));
                abrigo.setDeleted(rs.getBoolean("DELETED"));
                abrigo.setDataCriacao(rs.getTimestamp("DT_CRIACAO").toLocalDateTime());
                abrigo.setNomeAbrigo(rs.getString("NM_ABRIGO"));
                abrigo.setCep(rs.getString("CEP"));
                abrigo.setEnderecoAbrigo(rs.getString("ENDERECO"));
                abrigo.setCapacidadeMaxima(rs.getInt("QT_MAXIMA_PESSOAS"));
                abrigo.setStatusFuncionamento(rs.getString("ST_FUNCIONAMENTO"));
                abrigo.setNivelSegurancaAtual(rs.getString("NV_SEGURANCA_ATUAL"));
                abrigo.setTelefoneContato(rs.getString("TELEFONE_CONTATO"));
                abrigo.setLat(rs.getBigDecimal("LAT"));
                abrigo.setLon(rs.getBigDecimal("LON"));
                abrigo.setIdCidade(rs.getInt("ID_CIDADE"));
                abrigosBuscados.add(abrigo);
            }

            totalItems = abrigosBuscados.size();
            logger.info("✅ Abrigos buscados. {} resultados encontrados.", totalItems);

        } catch (SQLException e) {
            logger.error("❌ Erro ao buscar abrigos: {}", e.getMessage(), e);
        }
        return new SearchResult<>(abrigosBuscados, totalItems);
    }

    // Função para buscar abrigos por ID da cidade
    public SearchResult<Abrigo> buscarPorIdCidade(int idCidade, String nomeAbrigo, String cep, String statusFuncionamento, String direction) {
        List<Abrigo> abrigosBuscados = new ArrayList<>();
        int totalItems = 0;

        StringBuilder queryBuilder = new StringBuilder(
                "SELECT * FROM T_POSE_ABRIGO WHERE DELETED = 0 AND ID_CIDADE = ?"
        );

        List<Object> params = new ArrayList<>();
        params.add(idCidade); // Adiciona o idCidade como o primeiro parâmetro

        // Adiciona filtros opcionais
        if (nomeAbrigo != null && !nomeAbrigo.isBlank()) {
            queryBuilder.append(" AND UPPER(NM_ABRIGO) LIKE UPPER(?)");
            params.add("%" + nomeAbrigo + "%");
        }
        if (cep != null && !cep.isBlank()) {
            queryBuilder.append(" AND CEP = ?");
            params.add(cep);
        }
        if (statusFuncionamento != null && !statusFuncionamento.isBlank()) {
            queryBuilder.append(" AND UPPER(ST_FUNCIONAMENTO) LIKE UPPER(?)");
            params.add("%" + statusFuncionamento + "%");
        }

        // Ordenação
        if ("desc".equalsIgnoreCase(direction)) {
            queryBuilder.append(" ORDER BY NM_ABRIGO DESC, ID_ABRIGO DESC");
        } else {
            queryBuilder.append(" ORDER BY NM_ABRIGO ASC, ID_ABRIGO ASC");
        }

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(queryBuilder.toString())) {

            // Define os parâmetros na PreparedStatement
            int paramIndex = 1;
            for (Object param : params) {
                stmt.setObject(paramIndex++, param);
            }

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Abrigo abrigo = new Abrigo();
                abrigo.setIdAbrigo(rs.getInt("ID_ABRIGO"));
                abrigo.setDeleted(rs.getBoolean("DELETED")); // Considerando que DELETED é um booleano no banco ou pode ser convertido
                abrigo.setDataCriacao(rs.getTimestamp("DT_CRIACAO").toLocalDateTime());
                abrigo.setNomeAbrigo(rs.getString("NM_ABRIGO"));
                abrigo.setCep(rs.getString("CEP"));
                abrigo.setEnderecoAbrigo(rs.getString("ENDERECO"));
                abrigo.setCapacidadeMaxima(rs.getInt("QT_MAXIMA_PESSOAS"));
                abrigo.setStatusFuncionamento(rs.getString("ST_FUNCIONAMENTO"));
                abrigo.setNivelSegurancaAtual(rs.getString("NV_SEGURANCA_ATUAL"));
                abrigo.setTelefoneContato(rs.getString("TELEFONE_CONTATO"));
                abrigo.setLat(rs.getBigDecimal("LAT"));
                abrigo.setLon(rs.getBigDecimal("LON"));
                abrigo.setIdCidade(rs.getInt("ID_CIDADE"));
                abrigosBuscados.add(abrigo);
            }

            totalItems = abrigosBuscados.size();
            logger.info("✅ Abrigos buscados para a cidade ID: {}. {} resultados encontrados.", idCidade, totalItems);

        } catch (SQLException e) {
            logger.error("❌ Erro ao buscar abrigos para a cidade ID {}: {}", idCidade, e.getMessage(), e);
        }
        return new SearchResult<>(abrigosBuscados, totalItems);
    }

    // Função para buscar um abrigo por ID
    public Abrigo buscarPorId(int id) {
        String query = "SELECT ID_ABRIGO, DELETED, DT_CRIACAO, NM_ABRIGO, CEP, ENDERECO, " +
                "QT_MAXIMA_PESSOAS, ST_FUNCIONAMENTO, NV_SEGURANCA_ATUAL, TELEFONE_CONTATO, LAT, LON, ID_CIDADE " +
                "FROM T_POSE_ABRIGO " +
                "WHERE ID_ABRIGO = ? AND DELETED = 0";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Abrigo abrigo = new Abrigo();
                abrigo.setIdAbrigo(rs.getInt("ID_ABRIGO"));
                abrigo.setDeleted(rs.getInt("DELETED") == 1); // DDL é NUMERIC(1)
                abrigo.setDataCriacao(rs.getTimestamp("DT_CRIACAO").toLocalDateTime());
                abrigo.setNomeAbrigo(rs.getString("NM_ABRIGO"));
                abrigo.setCep(rs.getString("CEP"));
                abrigo.setEnderecoAbrigo(rs.getString("ENDERECO"));
                abrigo.setCapacidadeMaxima(rs.getInt("QT_MAXIMA_PESSOAS")); // DDL é QT_MAXIMA_PESSOAS
                abrigo.setStatusFuncionamento(rs.getString("ST_FUNCIONAMENTO"));
                abrigo.setNivelSegurancaAtual(rs.getString("NV_SEGURANCA_ATUAL"));
                abrigo.setTelefoneContato(rs.getString("TELEFONE_CONTATO"));
                abrigo.setLat(rs.getBigDecimal("LAT"));
                abrigo.setLon(rs.getBigDecimal("LON"));
                abrigo.setIdCidade(rs.getInt("ID_CIDADE")); // Novo campo ID_CIDADE
                logger.info("✅ Abrigo com ID {} buscado com sucesso.", id);
                return abrigo;
            }
        } catch (SQLException e) {
            logger.error("❌ Erro ao buscar abrigo por ID {}: {}", id, e.getMessage(), e);
        }
        return null;
    }

    // Função para atualizar o abrigo
    public void atualizar(int id, Abrigo abrigo) {
        String query = "UPDATE T_POSE_ABRIGO SET NM_ABRIGO = ?, CEP = ?, ENDERECO = ?, " +
                "QT_MAXIMA_PESSOAS = ?, ST_FUNCIONAMENTO = ?, NV_SEGURANCA_ATUAL = ?, " +
                "TELEFONE_CONTATO = ?, LAT = ?, LON = ?, ID_CIDADE = ? " +
                "WHERE ID_ABRIGO = ? AND DELETED = 0";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, abrigo.getNomeAbrigo());
            stmt.setString(2, abrigo.getCep());
            stmt.setString(3, abrigo.getEnderecoAbrigo());
            stmt.setInt(4, abrigo.getCapacidadeMaxima()); // Mapeia para QT_MAXIMA_PESSOAS
            stmt.setString(5, abrigo.getStatusFuncionamento());
            stmt.setString(6, abrigo.getNivelSegurancaAtual());
            stmt.setString(7, abrigo.getTelefoneContato());
            stmt.setBigDecimal(8, abrigo.getLat());
            stmt.setBigDecimal(9, abrigo.getLon());
            stmt.setInt(10, abrigo.getIdCidade()); // Novo campo ID_CIDADE
            stmt.setInt(11, id); // ID do abrigo a ser atualizado (WHERE clause)

            int affectedRows = stmt.executeUpdate();

            if (affectedRows > 0) {
                logger.info("✅ Abrigo com ID {} atualizado com sucesso.", id);
            }
        } catch (SQLException e) {
            logger.error("❌ Erro ao atualizar abrigo com ID {}: {}", id, e.getMessage(), e);
        }
    }

    // Função para deletar (logicamente) um abrigo
    public void deletar(int id) {
        logger.info("Iniciando deleção lógica do abrigo por ID {}", id);
        String query = "UPDATE T_POSE_ABRIGO SET DELETED = 1 WHERE ID_ABRIGO = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, id);
            int affectedRows = stmt.executeUpdate();

            if (affectedRows > 0) {
                logger.info("✅ Abrigo com ID {} marcado como deletado com sucesso.", id);
            }
        } catch (SQLException e) {
            logger.error("❌ Erro ao marcar abrigo com ID {} como deletado: {}", id, e.getMessage(), e);
        }
    }
}