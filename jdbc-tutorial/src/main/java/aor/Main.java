package aor;

import java.sql.Date;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Scanner;
import java.util.UUID;

/**
 * Classe responsável pela interface de utilizador (menu).
 * Interage com o utilizador e chama os métodos da classe App.
 */
public class Main {

    public static void main(String[] args) {

        Scanner sc = new Scanner(System.in);

        try (App app = new App()) {

            int opcao;

            do {
                System.out.println("\n==== MENU ====");
                System.out.println("1- Adicionar música");
                System.out.println("2- Atualizar título");
                System.out.println("3- Remover música");
                System.out.println("4- Listar músicas");
                System.out.println("5 - Gerar playlist por género");
                System.out.println("0- Sair");
                System.out.print("Opção: ");

                opcao = Integer.parseInt(sc.nextLine());

                switch (opcao) {

                    case 1:
                        //Geração automática de UUID
                        UUID id = UUID.randomUUID();

                        System.out.print("Título: ");
                        String titulo = sc.nextLine();

                        System.out.print("Data de criação (YYYY-MM-DD): ");
                        Date data = Date.valueOf(LocalDate.parse(sc.nextLine()));

                        /*
                        Deixei comentado porque não sei se faz sentido ou não ter aqui
                        System.out.print("Autor: ");
                        String autor = sc.nextLine();

                        System.out.print("Género: ");
                        String genero = sc.nextLine();

                        System.out.print("Álbum: ");
                        String album = sc.nextLine();
                         */

                        System.out.print("Ordem no álbum): ");
                        String ordemAlbum = sc.nextLine();

                        /*
                        Se o utilizador não introduzir nada, guarda null.
                        Caso contrário, converte a String para inteiro.
                        Usamos Integer (e não int), porque a ordem pode ser opcional
                         */
                        Integer ordem = ordemAlbum.isBlank() ? null : Integer.parseInt(ordemAlbum);

                        app.adicionarMusica(id, titulo, data,ordem);
                        System.out.println("Id gerado: " + id);
                        break;

                    case 2:
                        System.out.print("ID da música (UUID): ");
                        UUID idUpdate = UUID.fromString(sc.nextLine());

                        System.out.print("Novo título: ");
                        String novoTitulo = sc.nextLine();

                        app.atualizarTitulo(idUpdate, novoTitulo);
                        break;

                    case 3:
                        System.out.print("Id da música (UUID): ");
                        UUID idDelete = UUID.fromString(sc.nextLine());

                        app.eliminarMusica(idDelete);
                        break;

                    case 4:
                        app.listarMusicas();
                        break;

                    case 5:
                        System.out.print("Género: ");
                        String genero = sc.nextLine();

                        System.out.print("Número de músicas: ");
                        int limite = Integer.parseInt(sc.nextLine());

                        app.playlistAleatoria(genero, limite);
                        break;

                    case 0:
                        System.out.println("A sair...");
                        break;

                    default:
                        System.out.println("Opção inválida!");
                }

            } while (opcao != 0);

        } catch (SQLException e) {
            System.out.println("Erro na base de dados:");
            e.printStackTrace();
        } catch (Exception e) {
            System.out.println("Erro de input:");
            e.printStackTrace();
        }

        sc.close();
    }
}

