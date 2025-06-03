package fiap.tds.repositories;

import fiap.tds.dtos.SearchResult;
import fiap.tds.entities.AutenticaUsuario;
import fiap.tds.entities.Usuario;
import fiap.tds.infrastructure.DatabaseConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class UsuarioRepository {

    public static final Logger logger = LogManager.getLogger(UsuarioRepository.class);

    // Chamando o repository de AutenticaUsuario para usar as funcionalidades dele aqui e conseguir separar as tabelas
    AutenticaUsuarioRepository autenticaUsuarioRepository = new AutenticaUsuarioRepository();

    // Função para registrar usuário
    public void registrar(Usuario usuario){
        var query = "INSERT INTO T_POSE_USUARIO (DT_CRIACAO, NM_USUARIO, TP_USUARIO, ID_CIDADE, TELEFONE_CONTATO) VALUES (?, ?, ?, ?, ?) ";

        try (var conn = DatabaseConfig.getConnection()) {

            var stmt = conn.prepareStatement(query, new String[] { "ID_USUARIO"}); // Retorna o ID gerado pelo banco

            // Definindo os parâmetros
            stmt.setTimestamp(1, Timestamp.valueOf(usuario.getDataCriacao()));
            stmt.setString(2, usuario.getNomeUsuario());
            stmt.setString(3, usuario.getTipoUsuario());
            stmt.setInt(4, usuario.getIdCidade());
            stmt.setString(5, usuario.getTelefoneContato());

            var res = stmt.executeUpdate();

            if (res > 0){

                // Recuperando o ID gerado pelo banco de dados
                try (var generatedKeys = stmt.getGeneratedKeys()){
                    if (generatedKeys.next()){
                        var idGerado = generatedKeys.getInt(1); // Isso busca o ID gerado pelo banco
                        logger.info("✅ Usuário registrado com sucesso, ID: {}", idGerado);

                        // Chamar a função do AutenticaUsuario aqui!
                        AutenticaUsuario autenticaUsuario = usuario.getAutenticaUsuario();
                        autenticaUsuario.setIdUsuario(idGerado);
                        autenticaUsuarioRepository.registrar(autenticaUsuario);
                    }
                } catch (Exception e) {
                    logger.info("❌ Erro ao obter ID gerado pelo banco");
                }
            }
        } catch (SQLException e) {
            logger.error("❌ Erro ao registrar usuário: {}", e.getMessage());
        }
    }

    // Função para buscar usuários
    public SearchResult<Usuario> buscar(String nome, String tipo, String direction) {
        List<Usuario> usuariosBuscados = new ArrayList<>();
        int totalItems = 0;

        var query = new StringBuilder("SELECT * FROM T_POSE_USUARIO WHERE DELETED = 0");

        // Filtros
        if (nome != null && !nome.isBlank()) {
            query.append(" AND UPPER(NM_USUARIO) LIKE UPPER(?)");
        }
        if (tipo != null && !tipo.isBlank()) {
            query.append(" AND UPPER(TP_USUARIO) LIKE UPPER(?)");
        }

        // Ordenação
        if ("desc".equalsIgnoreCase(direction)) {
            query.append(" ORDER BY ID_USUARIO DESC");
        } else {
            query.append(" ORDER BY ID_USUARIO ASC");
        }

        try (var conn = DatabaseConfig.getConnection();
             var stmt = conn.prepareStatement(query.toString())) {

            int paramIndex = 1;
            if (nome != null && !nome.isBlank()) {
                stmt.setString(paramIndex++, "%" + nome + "%");
            }
            if (tipo != null && !tipo.isBlank()) {
                stmt.setString(paramIndex++, "%" + tipo + "%");
            }

            var res = stmt.executeQuery();

            while (res.next()) {
                Usuario usuario = new Usuario();
                usuario.setIdUsuario(res.getInt("ID_USUARIO"));
                usuario.setDeleted(res.getBoolean("DELETED"));
                usuario.setDataCriacao(res.getTimestamp("DT_CRIACAO").toLocalDateTime()); // tomar cuidado pra não esquecer isso ;)
                usuario.setNomeUsuario(res.getString("NM_USUARIO"));
                usuario.setTipoUsuario(res.getString("TP_USUARIO"));
                usuario.setTelefoneContato(res.getString("TELEFONE_CONTATO"));
                usuario.setIdCidade(res.getInt("ID_CIDADE"));
                usuariosBuscados.add(usuario);
            }

            logger.info("✅ Usuários buscados com sucesso");
            totalItems = usuariosBuscados.size();

        } catch (SQLException e) {
            logger.error("❌ Erro ao buscar usuários: {}", e.getMessage());
        }
        return new SearchResult<>(usuariosBuscados, totalItems);
    }

    // Função para pegar o ID de um usuário por token de sessão
    public Integer buscarIdColaboradorPorToken(String token) {
        String query = "SELECT ID_USUARIO FROM T_POSE_SESSAO_USUARIO WHERE TOKEN_SESSAO = ?";

        try (var conn = DatabaseConfig.getConnection();
             var stmt = conn.prepareStatement(query)) {

            stmt.setString(1, token);

            var res = stmt.executeQuery();

            if (res.next()) {
                return res.getInt("ID_USUARIO");
            }
        } catch (SQLException e) {
            logger.error("❌ Erro ao buscar colaborador pelo token \n{}", e.getMessage());
        }
        return null;
    }

    public Usuario buscarPorId(int id) {
        String queryUsuario = "SELECT * FROM T_POSE_USUARIO WHERE ID_USUARIO = ? AND DELETED = 0";
        // Assumindo que a tabela T_POSE_AUTENTICA_USUARIO usa ID_USUARIO como chave estrangeira
        // e que não possui uma coluna 'DELETED' própria relevante para esta busca específica.
        // Se T_POSE_AUTENTICA_USUARIO tiver uma coluna 'DELETED', adicione AND DELETED = 0 se apropriado.
        String queryAutentica = "SELECT EMAIL_USUARIO FROM T_POSE_AUTENTICA_USUARIO WHERE ID_USUARIO = ?";

        Usuario usuario = null;

        try (var conn = DatabaseConfig.getConnection();
             var stmtUsuario = conn.prepareStatement(queryUsuario)) {

            stmtUsuario.setInt(1, id);

            try (var rsUsuario = stmtUsuario.executeQuery()) {
                if (rsUsuario.next()) {
                    usuario = new Usuario();
                    usuario.setIdUsuario(rsUsuario.getInt("ID_USUARIO"));
                    usuario.setDeleted(rsUsuario.getBoolean("DELETED"));

                    Timestamp dataCriacaoTimestamp = rsUsuario.getTimestamp("DT_CRIACAO");
                    if (dataCriacaoTimestamp != null) {
                        usuario.setDataCriacao(dataCriacaoTimestamp.toLocalDateTime());
                    } else {
                        // logger.warn("⚠️ Data de criação nula para usuário ID {}", id); // Opcional
                    }

                    usuario.setNomeUsuario(rsUsuario.getString("NM_USUARIO"));
                    usuario.setTipoUsuario(rsUsuario.getString("TP_USUARIO"));
                    usuario.setTelefoneContato(rsUsuario.getString("TELEFONE_CONTATO"));
                    usuario.setIdCidade(rsUsuario.getInt("ID_CIDADE"));

                    // Agora, buscar os dados de autenticação (e-mail)
                    try (var stmtAutentica = conn.prepareStatement(queryAutentica)) {
                        stmtAutentica.setInt(1, id); // Usar o mesmo ID de usuário

                        try (var rsAutentica = stmtAutentica.executeQuery()) {
                            if (rsAutentica.next()) {
                                AutenticaUsuario autenticacao = new AutenticaUsuario();
                                autenticacao.setEmailUsuario(rsAutentica.getString("EMAIL_USUARIO"));
                                usuario.setAutenticaUsuario(autenticacao);
                                logger.info("✅ E-mail buscado para o usuário ID {}", id);
                            } else {
                                logger.warn("⚠️ E-mail não encontrado na tabela de autenticação para o usuário ID {}. O campo 'autenticaUsuario' no objeto Usuario ficará nulo ou com e-mail nulo.", id);
                                // Você pode optar por instanciar AutenticaUsuario com email null
                                // usuario.setAutenticaUsuario(new AutenticaUsuario(null));
                                // ou deixar o campo autenticaUsuario do objeto usuario como null.
                                // A entidade Usuario já instancia autenticaUsuario no construtor default se você não o fizer aqui.
                            }
                        }
                    } catch (SQLException eAuth) {
                        logger.error("❌ Erro ao buscar e-mail para o usuário ID {}: {}. O usuário será retornado sem e-mail.", id, eAuth.getMessage());
                        // O objeto usuario principal ainda será retornado, mas sem os dados de autenticação.
                    }
                    logger.info("✅ Usuário ID {} buscado da tabela principal.", id);
                } else {
                    logger.warn("⚠️ Usuário com ID {} não encontrado ou marcado como deletado na tabela principal.", id);
                }
            }
        } catch (SQLException e) {
            logger.error("❌ Erro crítico ao buscar usuário com ID {}: {}", id, e.getMessage());
            return null; // Retorna null se houver erro na busca principal do usuário
        }
        return usuario; // Retorna o objeto Usuario
    }


    public void atualizarCidadeUsuario(int id, Usuario usuario){
        String query = "UPDATE T_POSE_USUARIO SET ID_CIDADE = ? WHERE ID_USUARIO = ? AND DELETED = 0" ;

        try (var conn = DatabaseConfig.getConnection();
             var stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, usuario.getIdCidade());
            stmt.setInt(2, id);

            int res = stmt.executeUpdate();

            if (res > 0) {
                logger.info("✅ Cidade do usuario atualizada com sucesso");
            }

        } catch (SQLException e) {
            logger.error("❌ Erro ao atualizar usuario \n{}", e.getMessage());
        }
    }

    public void atualizarEmailUsuario(int idUsuario, String novoEmail) {
        String query = "UPDATE T_POSE_AUTENTICA_USUARIO SET EMAIL_USUARIO = ? WHERE ID_USUARIO = ?";

        try (var conn = DatabaseConfig.getConnection();
             var stmt = conn.prepareStatement(query)) {

            stmt.setString(1, novoEmail);
            stmt.setInt(2, idUsuario);

            int res = stmt.executeUpdate();

            if (res > 0) {
                logger.info("✅ Email do usuário ID {} atualizado com sucesso.", idUsuario);
            } else {
                logger.warn("⚠️ Nenhum email de usuário foi atualizado para o ID {}. Verifique se o ID do usuário existe.", idUsuario);
            }

        } catch (SQLException e) {
            logger.error("❌ Erro ao atualizar email do usuário ID {} \n{}", idUsuario, e.getMessage());
        }
    }

    public void atualizarSenhaUsuario(int idUsuario, String novaSenha) {
        String query = "UPDATE T_POSE_AUTENTICA_USUARIO SET SENHA_USUARIO = ? WHERE ID_USUARIO = ?";


        try (var conn = DatabaseConfig.getConnection();
             var stmt = conn.prepareStatement(query)) {

            stmt.setString(1, novaSenha);
            stmt.setInt(2, idUsuario);

            int res = stmt.executeUpdate();

            if (res > 0) {
                logger.info("✅ Senha do usuário ID {} atualizada com sucesso.", idUsuario);
            } else {
                logger.warn("⚠️ Nenhuma senha de usuário foi atualizada para o ID {}. Verifique se o ID do usuário existe.", idUsuario);
            }

        } catch (SQLException e) {
            logger.error("❌ Erro ao atualizar senha do usuário ID {} \n{}", idUsuario, e.getMessage());
        }
    }

    public void atualizarTelefoneUsuario(int idUsuario, String novoTelefone) {
        String query = "UPDATE T_POSE_USUARIO SET TELEFONE_CONTATO = ? WHERE ID_USUARIO = ? AND DELETED = 0";

        try (var conn = DatabaseConfig.getConnection();
             var stmt = conn.prepareStatement(query)) {

            stmt.setString(1, novoTelefone);
            stmt.setInt(2, idUsuario);

            int res = stmt.executeUpdate();

            if (res > 0) {
                logger.info("✅ Telefone de contato do usuário ID {} atualizado com sucesso.", idUsuario);
            } else {
                logger.warn("⚠️ Nenhum telefone de contato de usuário foi atualizado para o ID {}. Verifique se o ID existe e não está marcado como 'deleted'.", idUsuario);
            }

        } catch (SQLException e) {
            logger.error("❌ Erro ao atualizar telefone de contato do usuário ID {} \n{}", idUsuario, e.getMessage());
        }
    }

    public void atualizar(int id, Usuario usuario) {

        String query = "UPDATE T_POSE_USUARIO SET NM_USUARIO = ?, TELEFONE_CONTATO = ?, ID_CIDADE = ? WHERE ID_USUARIO = ? AND DELETED = 0";

        try (var conn = DatabaseConfig.getConnection();
             var stmt = conn.prepareStatement(query)) {

            stmt.setString(1, usuario.getNomeUsuario());
            stmt.setString(2, usuario.getTelefoneContato());
            stmt.setInt(3, usuario.getIdCidade());
            stmt.setInt(4, id);

            int res = stmt.executeUpdate();

            if (res > 0) {
                logger.info("✅ Usuario atualizado com sucesso");
            }

        } catch (SQLException e) {
            logger.error("❌ Erro ao atualizar usuario \n{}", e.getMessage());
        }
    }

    public void deletar(int id) {
        logger.info("Iniciando deleção do usuário por ID {}", id);

        String query = "UPDATE T_POSE_USUARIO SET DELETED = 1 WHERE ID_USUARIO = ?";

        try (var conn = DatabaseConfig.getConnection();
             var stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, id);

            int res = stmt.executeUpdate();

            if (res > 0) {
                logger.info("✅ Usuário deletado com sucesso");
            }

        } catch (SQLException e) {
            logger.error("❌ Erro ao excluir usuário \n{}", e.getMessage());
        }
    }

}
