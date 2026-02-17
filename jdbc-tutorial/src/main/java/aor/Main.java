package aor;

import java.awt.*;
import java.sql.SQLException;
import java.util.Scanner;




/**
 * Classe principal que gere a interface de utilizador via terminal.
 * O nome do ficheiro deve ser exatamente Main.java.
 */
public class Main {

    public static void main(String[] args) {

        Scanner sc = new Scanner(System.in);

        // O fecho da ligação é implícito através da instrução try-with-resources
        try (App app = new App()) {

            int opcao;

            do {
                Menu.appMenu();

                try {
                    opcao = Integer.parseInt(sc.nextLine());
                } catch (NumberFormatException e) {
                    opcao = -1;
                }

                switch (opcao) {

                    case 1:Menu.adicionarMusica();break;
                    case 2:Menu.atualizarMusica();break;
                    case 3:Menu.removerMusica();break;
                    case 4:app.listarMusicas();break;
                    case 5:Menu.gerarPlaylist();break;
                    case 0:System.out.println("A encerrar a aplicação...");break;
                    default:Design.erro("Opção inválida! Tente novamente.");
                }

            } while (opcao != 0);

        } catch (SQLException e) {
            System.err.println("Erro na base de dados: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Erro inesperado: " + e.getMessage());
        } finally {
            sc.close();
        }
    }
}