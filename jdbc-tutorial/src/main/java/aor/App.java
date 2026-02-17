package aor;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static aor.Design.CYAN;

public class App implements AutoCloseable {
    private final static String URL = "jdbc:postgresql://localhost:5432/MiniProjecto";
    private final static String USER = "postgres";
    private final static String PASSWORD = "postgres";
    private Connection conn;

    public App() throws SQLException {
        this.conn = DriverManager.getConnection(App.URL, App.USER, App.PASSWORD);
    }

    /**
     * Adiciona uma nova música ao sistema com inserção automática de dependências.
     */
    public void adicionarMusica(Date data, String titulo, String ordem, String album, String genero, String autor) throws SQLException {

        // 1. Garantir que o Autor existe
        String sqlAutor = "INSERT INTO autor (nome) VALUES (?) ON CONFLICT (nome) DO NOTHING";
        try (PreparedStatement stm = conn.prepareStatement(sqlAutor)) {
            stm.setString(1, autor);
            stm.executeUpdate();
        }

        // 2. Garantir que o Género existe
        String sqlGenero = "INSERT INTO genero (nome) VALUES (?) ON CONFLICT (nome) DO NOTHING";
        try (PreparedStatement stm = conn.prepareStatement(sqlGenero)) {
            stm.setString(1, genero);
            stm.executeUpdate();
        }

        // 3. Garantir que o Álbum existe
        if (album != null && !album.trim().isEmpty()) {
            String sqlAlbum = "INSERT INTO album (titulo) VALUES (?) ON CONFLICT (titulo) DO NOTHING";
            try (PreparedStatement stm = conn.prepareStatement(sqlAlbum)) {
                stm.setString(1, album);
                stm.executeUpdate();
            }
        }

        // 4. Inserir a Música seguindo a ordem física da BD
        String sqlMusica = "INSERT INTO musica (data_criacao, titulo, ordem_album, album_titulo, genero_nome, autor_nome) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stm = conn.prepareStatement(sqlMusica)) {

            stm.setDate(1, data);
            stm.setString(2, titulo);
            // Tratamento da String para a coluna numérica da BD
            if (ordem == null || ordem.isEmpty()) {
                stm.setNull(3, Types.INTEGER);
            } else {
                // Converte a String para Long apenas no momento do INSERT
                stm.setInt(3, Integer.parseInt(ordem));
            }
            stm.setObject(4, (album != null && !album.trim().isEmpty()) ? album : null);
            stm.setString(5, genero);
            stm.setString(6, autor);

            stm.executeUpdate();
            System.out.println("Música e dependências processadas com sucesso!");
        }
    }

    /**
     * Remove uma música e elimina o álbum se este ficar vazio.
     */
    public void removerMusica(int id) throws SQLException {
        // 1. Identificar o álbum antes de remover a música
        String selectAlbum = "SELECT album_titulo FROM musica WHERE id = ?";
        String albumTitulo = null;

        try (PreparedStatement stm = conn.prepareStatement(selectAlbum)) {
            stm.setInt(1, id);
            try (ResultSet rs = stm.executeQuery()) {
                if (rs.next()) {
                    albumTitulo = rs.getString("album_titulo");
                }
            }
        }

        // 2. Remover a música
        String deleteMusica = "DELETE FROM musica WHERE id = ?";
        try (PreparedStatement stm = conn.prepareStatement(deleteMusica)) {
            stm.setInt(1, id); // Já não usamos .toString(), passamos o int diretamente
            int rows = stm.executeUpdate();
            if (rows > 0) {
                System.out.println("Música com ID " + id + " removida.");
            } else {
                System.out.println("Nenhuma música encontrada com o ID " + id);
                return; // Se não removeu nada, não vale a pena verificar o álbum
            }
        }

        // 3. Verificar e remover álbum se ficar vazio
        if (albumTitulo != null) {
            String countMusicas = "SELECT COUNT(*) FROM musica WHERE album_titulo ILIKE ?";
            try (PreparedStatement stm = conn.prepareStatement(countMusicas)) {
                stm.setString(1, albumTitulo);
                try (ResultSet rs = stm.executeQuery()) {
                    if (rs.next() && rs.getInt(1) == 0) {
                        try (PreparedStatement delAlbum = conn.prepareStatement("DELETE FROM album WHERE titulo ILIKE ?")) {
                            delAlbum.setString(1, albumTitulo);
                            delAlbum.executeUpdate();
                            System.out.println("O álbum '" + albumTitulo + "' ficou vazio e foi removido.");
                        }
                    }
                }
            }
        }
    }

    public void atualizarTitulo(int id, String novoTitulo) throws SQLException {
        String sql = "UPDATE musica SET titulo = ? WHERE id = ?";
        try (PreparedStatement stm = conn.prepareStatement(sql)) {
            stm.setString(1, novoTitulo);
            stm.setInt(2, id);
            if (stm.executeUpdate() > 0) System.out.println("Título atualizado.");
            else System.out.println("Música não encontrada.");
        }
    }

    public void listarMusicas() throws SQLException {
        // Adicionamos m.id à consulta para o utilizador saber o que selecionar depois
        String sql = "SELECT m.id, m.titulo, m.data_criacao, m.autor_nome, m.genero_nome, m.album_titulo " +
                "FROM musica m ORDER BY m.id ASC";

        try (Statement stm = conn.createStatement();
             ResultSet rs = stm.executeQuery(sql)) {

            Design.caixaTitulo("--- Listagem de Músicas ---", CYAN);

            // Cabeçalho para facilitar a leitura das colunas
            System.out.printf("%-4s | %-20s | %-20s | %-12s | %-20s | %s%n",
                    "ID", "Título", "Autor", "Género", "Álbum", "Data");
            Design.separador();

            while (rs.next()) {
                System.out.printf("%-4d | %-20.20s | %-20.20s | %-12.12s | %-20.20s | %s%n",
                        rs.getInt("id"),
                        rs.getString("titulo"),
                        rs.getString("autor_nome"),
                        rs.getString("genero_nome"),
                        rs.getString("album_titulo") == null ? "N/A" : rs.getString("album_titulo"),
                        rs.getDate("data_criacao"));
            }
            Design.separador();
        }
    }

    public void gerarPlaylist(String genero, int limite) throws SQLException {
        String sql = "SELECT titulo, autor_nome FROM musica WHERE genero_nome ILIKE ? ORDER BY RANDOM() LIMIT ?";
        try (PreparedStatement stm = conn.prepareStatement(sql)) {
            stm.setString(1, genero);
            stm.setInt(2, limite);

            try (ResultSet rs = stm.executeQuery()) {
                Design.separador();
                System.out.printf("   PLAYLIST ALEATÓRIA: %s (Máx: %d)%n\n", genero.toUpperCase(), limite);


                boolean temMusicas = false;
                int contador = 1;

                while (rs.next()) {
                    temMusicas = true;
                    System.out.printf("%d. %-25s | %s%n",
                            contador++,
                            rs.getString("titulo"),
                            rs.getString("autor_nome"));
                }

                if (!temMusicas) {
                    System.out.println("Nenhuma música encontrada para o género: " + genero);
                }
                Design.separador();
            }
        }
    }

    public String[] buscarMusicaPorId(int id) throws SQLException {
        String sql = "SELECT titulo, autor_nome FROM musica WHERE id = ?";
        try (PreparedStatement stm = conn.prepareStatement(sql)) {
            stm.setInt(1, id);
            try (ResultSet rs = stm.executeQuery()) {
                if (rs.next()) {
                    return new String[]{rs.getString("titulo"), rs.getString("autor_nome")};
                }
            }
        }
        return null; // Retorna null se o ID não existir
    }

    @Override
    public void close() throws SQLException {
        if (this.conn != null) this.conn.close();
    }
}