package aor;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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
    public void adicionarMusica(UUID id, Date data, String titulo, int ordem, String album, String genero, String autor) throws SQLException {

        // 1. Garantir que o Autor existe
        String sqlAutor = "INSERT INTO autor (nome) VALUES (?) ON CONFLICT (nome) DO NOTHING";
        try (PreparedStatement stm = conn.prepareStatement(sqlAutor)) {
            stm.setString(1, autor);
            stm.executeUpdate();
        }

        // 2. Garantir que o Género existe (Ajustado para usar apenas 'nome' como PK)
        String sqlGenero = "INSERT INTO genero (nome) VALUES (?) ON CONFLICT (nome) DO NOTHING";
        try (PreparedStatement stm = conn.prepareStatement(sqlGenero)) {
            stm.setString(1, genero);
            stm.executeUpdate();
        }

        // 3. Garantir que o Álbum existe (se for fornecido)
        if (album != null && !album.trim().isEmpty()) {
            String sqlAlbum = "INSERT INTO album (titulo) VALUES (?) ON CONFLICT (titulo) DO NOTHING";
            try (PreparedStatement stm = conn.prepareStatement(sqlAlbum)) {
                stm.setString(1, album);
                stm.executeUpdate();
            }
        }

        // 4. Inserir a Música seguindo a ordem física da BD
        String sqlMusica = "INSERT INTO musica (id, data_criacao, titulo, ordem_album, album_titulo, genero_nome, autor_nome) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stm = conn.prepareStatement(sqlMusica)) {
            stm.setString(1, id.toString()); // UUID como varchar(36)
            stm.setDate(2, data);
            stm.setString(3, titulo);
            // Uso de setObject para permitir NULL se o álbum for vazio
            stm.setObject(4, (album != null && !album.trim().isEmpty()) ? ordem : null);
            stm.setObject(5, (album != null && !album.trim().isEmpty()) ? album : null);
            stm.setString(6, genero);
            stm.setString(7, autor);

            stm.executeUpdate();
            System.out.println("Música e dependências processadas com sucesso!");
        }
    }

    /**
     * Remove uma música e elimina o álbum se este ficar vazio.
     */
    public void removerMusica(UUID id) throws SQLException {
        // 1. Identificar o álbum antes de remover a música
        String selectAlbum = "SELECT album_titulo FROM musica WHERE id = ?";
        String albumTitulo = null;

        try (PreparedStatement stm = conn.prepareStatement(selectAlbum)) {
            stm.setString(1, id.toString());
            try (ResultSet rs = stm.executeQuery()) {
                if (rs.next()) {
                    albumTitulo = rs.getString("album_titulo");
                }
            }
        }

        // 2. Remover a música
        String deleteMusica = "DELETE FROM musica WHERE id = ?";
        try (PreparedStatement stm = conn.prepareStatement(deleteMusica)) {
            stm.setString(1, id.toString());
            stm.executeUpdate();
            System.out.println("Música removida.");
        }

        // 3. Verificar e remover álbum se ficar vazio
        if (albumTitulo != null) {
            String countMusicas = "SELECT COUNT(*) FROM musica WHERE album_titulo = ?";
            try (PreparedStatement stm = conn.prepareStatement(countMusicas)) {
                stm.setString(1, albumTitulo);
                try (ResultSet rs = stm.executeQuery()) {
                    if (rs.next() && rs.getInt(1) == 0) {
                        try (PreparedStatement delAlbum = conn.prepareStatement("DELETE FROM album WHERE titulo = ?")) {
                            delAlbum.setString(1, albumTitulo);
                            delAlbum.executeUpdate();
                            System.out.println("O álbum '" + albumTitulo + "' ficou vazio e foi removido.");
                        }
                    }
                }
            }
        }
    }

    public void atualizarTitulo(UUID id, String novoTitulo) throws SQLException {
        String sql = "UPDATE musica SET titulo = ? WHERE id = ?";
        try (PreparedStatement stm = conn.prepareStatement(sql)) {
            stm.setString(1, novoTitulo);
            stm.setString(2, id.toString());
            if (stm.executeUpdate() > 0) System.out.println("Título atualizado.");
            else System.out.println("Música não encontrada.");
        }
    }

    public void listarMusicas() throws SQLException {
        // SQL ajustado para os nomes reais das suas colunas
        String sql = "SELECT m.titulo, m.data_criacao, m.autor_nome, m.genero_nome, m.album_titulo " +
                "FROM musica m";

        try (Statement stm = conn.createStatement();
             ResultSet rs = stm.executeQuery(sql)) {
            System.out.println("\n--- Listagem de Músicas ---");
            while (rs.next()) {
                System.out.printf("Título: %-25s | Autor: %-25s | Género: %-15s | Álbum: %-30s (Data: %s)%n",
                        rs.getString("titulo"),
                        rs.getString("autor_nome"),
                        rs.getString("genero_nome"),
                        rs.getString("album_titulo") == null ? "N/A" : rs.getString("album_titulo"),
                        rs.getDate("data_criacao"));
            }
        }
    }

    public void gerarPlaylist(String genero, int limite) throws SQLException {
        String sql = "SELECT titulo, autor_nome FROM musica WHERE genero_nome = ? ORDER BY RANDOM() LIMIT ?";
        try (PreparedStatement stm = conn.prepareStatement(sql)) {
            stm.setString(1, genero);
            stm.setInt(2, limite);
            try (ResultSet rs = stm.executeQuery()) {
                System.out.println("\n--- Playlist Aleatória [" + genero + "] ---");
                while (rs.next())
                    System.out.printf("- %s (%s)%n", rs.getString("titulo"), rs.getString("autor_nome"));
            }
        }
    }

    public List<String[]> procurarMusicasParaSelecao(String tituloBusca) throws SQLException {
        List<String[]> resultados = new ArrayList<>();
        // SQL que junta a informação necessária para o utilizador distinguir as músicas
        String sql = "SELECT id, titulo, autor_nome, genero_nome FROM musica WHERE titulo ILIKE ?";

        try (PreparedStatement stm = conn.prepareStatement(sql)) {
            stm.setString(1, "%" + tituloBusca + "%");
            try (ResultSet rs = stm.executeQuery()) {
                while (rs.next()) {
                    resultados.add(new String[]{
                            rs.getString("id"),          // [0]
                            rs.getString("titulo"),      // [1]
                            rs.getString("autor_nome"),  // [2]
                            rs.getString("genero_nome")    // [3]
                    });
                }
            }
        }
        return resultados;
    }

    @Override
    public void close() throws SQLException {
        if (this.conn != null) this.conn.close();
    }
}