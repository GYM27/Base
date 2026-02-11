package aor;

import java.sql.*;
import java.time.LocalDate;

public class App implements AutoCloseable {
    private final static String URL = "\"jdbc:postgresql://localhost:5432/MiniProjecto\"";
    private final static String USER = "postgres";
    private final static String PASSWORD = "postgres";
    private Connection conn;

    public App() throws SQLException {
        this.conn = DriverManager.getConnection(App.URL, App.USER, App.PASSWORD);
    }

    public void adicionarMusica(String id, String titulo, Date data_criacao, String ordem_album) throws SQLException {

        String sql = "INSERT INTO musica(id, titulo, data_criacao, ordem_album) VALUES (?,?,?,?)";

        try (PreparedStatement stm = conn.prepareStatement(sql)) {
            stm.setString(1, id);
            stm.setString(2, titulo);
            stm.setDate(3, data_criacao);
            stm.setString(4, ordem_album);


            int rows = stm.executeUpdate();
            System.out.println("Linhas inseridas com sucesso: " + rows);
        }
    }

    // 2. Corrigir o título de uma música
    public void atualizarTitulo(int id, String novoTitulo) throws SQLException {
        String sql = "UPDATE musica SET titulo = ? WHERE id = ?";
        try (PreparedStatement stm = conn.prepareStatement(sql)) {
            stm.setString(1, novoTitulo);
            stm.setInt(2, id);
            stm.executeUpdate();
        }
    }

    public void eliminarMusica(int id) throws SQLException{

        String sql= "SELECT id_album FROM musica";
    }

    @Override
    public void close() throws Exception {

    }

    public static void main(String[] args) {
        try (App app = new App()) {
            app.adicionarMusica(id, titulo, data_criacao, ordem_album);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
