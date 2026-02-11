package aor;

import java.sql.SQLException;

public static void main(String[] args) {

    try (App app = new App() ) {
        app.adicionarMusica(id, titulo, data_criacao, ordem_album);
    } catch (SQLException e) {
        e.printStackTrace();
    }
}
