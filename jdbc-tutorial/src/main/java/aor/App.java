package aor;

import java.sql.*;
import java.util.UUID;


/**
 * Classe responsável pela ligação à base de dados
 * e implementação das operações CRUD sobre músicas.
 * Implementa AutoCloseable para permitir uso em try-with-resources.
 */
public class App implements AutoCloseable {
    private final static String URL = "\"jdbc:postgresql://localhost:5432/MiniProjecto\"";
    //Não será antes: "jdbc:postgresql://localhost:5432/MiniProjecto"?
    private final static String USER = "postgres";
    private final static String PASSWORD = "postgres";
    private Connection conn;

    /**
     * Construtor que estabelece ligação à base de dados.
     */
    public App() throws SQLException {
        this.conn = DriverManager.getConnection(App.URL, App.USER, App.PASSWORD);
    }

    /**
     * Adiciona uma nova música à base de dados.
     *
     * @param id Identificador único (UUID)
     * @param titulo Título da música
     * @param data_criacao Data de criação
     * @param ordem_album Número da ordem no álbum (opcional)
     */
    public void adicionarMusica(UUID id, String titulo, Date data_criacao, Integer ordem_album) throws SQLException {

        String sql = "INSERT INTO musica(id, titulo, data_criacao, ordem_album) VALUES (?,?,?,?)";

        try (PreparedStatement stm = conn.prepareStatement(sql)) {

            // UUID é enviado diretamente com setObject
            stm.setObject(1, id);
            stm.setString(2, titulo);
            stm.setDate(3, data_criacao);

            // Se ordemAlbum for null, envia NULL para a BD
            if (ordem_album != null){
                stm.setInt(4, ordem_album);
            } else{
                stm.setNull(4, Types.INTEGER);
            }

            int rows = stm.executeUpdate();

            if (rows == 1) {
                System.out.println("Música inserida com sucesso: " + rows);
            } else {
                System.out.println("Erro ao inserir música.");
            }
        }
    }

    /**
     * Atualiza o título de uma música existente.
     *
     * @param id UUID da música
     * @param novoTitulo Novo título
     */
    public void atualizarTitulo(UUID id, String novoTitulo) throws SQLException {

        String sql = "UPDATE musica SET titulo = ? WHERE id = ?";

        try (PreparedStatement stm = conn.prepareStatement(sql)) {
            stm.setString(1, novoTitulo);
            stm.setObject(2, id);

            int rows = stm.executeUpdate();

            if (rows == 0){
                System.out.println("Música não encontrada.");
            } else {
                System.out.println("Título atualizado.");
            }
        }
    }

    /*
    //Método sem a parte do "Deve ser removido o álbum, no caso de ficar vazio após a
    //remoção, que está no enunciado".
    public void eliminarMusica(UUID id) throws SQLException{

        String sql= "DELETE FROM musica WHERE id = ?";

        try (PreparedStatement stm = conn.prepareStatement(sql)){
            stm.setObject(1, id);

            int rows = stm.executeUpdate();

            if (rows == 0){
                System.out.println("Música não encontrada.");
            } else {
                System.out.println("Música removida com sucesso.");
            }
        }
    }*/

    /**
     * Remove uma música.
     * Se o álbum associado ficar sem músicas,
     * é removido automaticamente.
     *
     * @param id UUID da música
     */
    public void eliminarMusica(UUID id) throws SQLException {

        UUID albumId = null;

        //Descobrir o álbum da música
        String sqlSelect = "SELECT album_id FROM musica WHERE id = ?";

        try (PreparedStatement stm = conn.prepareStatement(sqlSelect)) {
            stm.setObject(1, id);

            try (ResultSet rs = stm.executeQuery()) {
                if (rs.next()) {
                    albumId = (UUID) rs.getObject("album_id");
                }
            }
        }

        //Apagar música
        String sqlDelete = "DELETE FROM musica WHERE id = ?";

        try (PreparedStatement stm = conn.prepareStatement(sqlDelete)) {
            stm.setObject(1, id);
            int rows= stm.executeUpdate();

            if (rows == 0) {
                System.out.println("Música não encontrada.");
                return;
            }
        }

        //Apagar álbum se ficou vazio
        if (albumId != null) {

            String sqlDeleteAlbum = "DELETE FROM album WHERE id = ? AND NOT EXISTS (SELECT 1 FROM musica WHERE album_id = ?)";

            try (PreparedStatement stm = conn.prepareStatement(sqlDeleteAlbum)) {
                stm.setObject(1, albumId);
                stm.setObject(2, albumId);
                stm.executeUpdate();
            }
        }

        System.out.println("Música removida com sucesso.");
    }


    /**
     * Lista todas as músicas existentes na base de dados.
     */
    public void listarMusicas() throws SQLException {

        String sql = " SELECT id, titulo, data_criacao, ordem_album FROM musica ORDER BY titulo";

        try (PreparedStatement stm = conn.prepareStatement(sql);
             ResultSet rs = stm.executeQuery()) {

            while (rs.next()) {
                System.out.println("Id: " + rs.getObject("id"));
                System.out.println("Título: " + rs.getString("titulo"));
                System.out.println("Data: " + rs.getDate("data_criacao"));
                System.out.println("Ordem no álbum: " + rs.getObject("ordem_album"));
                System.out.println("----------------------");
            }
        }
    }

    /**
     * Gera uma playlist aleatória por género.
     *
     * @param genero Nome do género
     * @param limite Número máximo de músicas
     */
    public void playlistAleatoria(String genero, int limite) throws SQLException {

        String sql = " SELECT m.id, m.titulo " +
                "FROM musica m " +
                "JOIN musica_genero mg ON m.id = mg.musica_id" +
                " JOIN genero g ON mg.genero_id = g.id " +
                "WHERE g.nome = ? " +
                "ORDER BY RANDOM() " +
                "LIMIT ? ";

        try (PreparedStatement stm = conn.prepareStatement(sql)) {

            stm.setString(1, genero);
            stm.setInt(2, limite);

            try (ResultSet rs = stm.executeQuery()) {

                System.out.println("\n PLAYLIST:");
                while (rs.next()) {
                    System.out.println(rs.getString("titulo"));
                }
            }
        }
    }


    /**
     * Fecha a ligação à base de dados.
     */
    @Override
    public void close() throws SQLException {
        if (conn != null){
            conn.close();
        }
    }

    /*
    public static void main(String[] args) {
        try (App app = new App()) {
            app.adicionarMusica(id, titulo, data_criacao, ordem_album);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }*/
}
