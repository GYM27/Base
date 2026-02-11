package aor;

import java.sql.Date;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Scanner;
import java.util.UUID;

public class main {

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
                System.out.println("0- Sair");
                System.out.print("Opção: ");

                opcao = Integer.parseInt(sc.nextLine());

                switch (opcao) {

                    case 1:
                        System.out.println("Id da música: ");
                        String musicaId = Integer.parseInt(sc.nextLine());

                        System.out.print("Título: ");
                        String titulo = sc.nextLine();

                        System.out.print("Data de criação (YYYY-MM-DD): ");
                        Date data = Date.valueOf(LocalDate.parse(sc.nextLine()));

                        System.out.print("Ordem no álbum): ");
                        String ordemAlbum = sc.nextLine();

                        app.adicionarMusica(musicaId, titulo, data,ordemAlbum);
                        break;

                    case 2:
                        System.out.print("ID da música (UUID): ");
                        UUID idUpdate = UUID.fromString(sc.nextLine());

                        System.out.print("Novo título: ");
                        String novoTitulo = sc.nextLine();

                        app.atualizarTitulo(idUpdate, novoTitulo);
                        break;

                    case 3:
                        System.out.print("ID da música (UUID): ");
                        UUID idDelete = UUID.fromString(sc.nextLine());

                        app.removerMusica(idDelete);
                        break;

                    case 4:
                        app.listarMusicas();
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

