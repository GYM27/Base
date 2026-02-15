package aor;

import java.sql.Date;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Scanner;
import java.util.UUID;

/**
 * Classe principal que gere a interface de utilizador via terminal.
 * O nome do ficheiro deve ser exatamente Main.java.
 */
public class Main {

    public static void main(String[] args) {

        Scanner sc = new Scanner(System.in);

        // O fecho da ligação é implícito através da instrução try-with-resources [cite: 139]
        try (App app = new App()) {

            int opcao;

            do {
                System.out.println("\n==== MENU MUSI-APP ====");
                System.out.println("1- Adicionar música");
                System.out.println("2- Atualizar título");
                System.out.println("3- Remover música");
                System.out.println("4- Listar todas as músicas");
                System.out.println("5- Gerar Playlist Aleatória (Extra)");
                System.out.println("0- Sair");
                System.out.print("Opção: ");

                try {
                    opcao = Integer.parseInt(sc.nextLine());
                } catch (NumberFormatException e) {
                    opcao = -1;
                }

                switch (opcao) {

                    case 1:
                        // O enunciado exige um identificador universal (UUID) [cite: 201]
                        UUID musicaId = UUID.randomUUID();
                        System.out.println("ID gerado automaticamente: " + musicaId);

                        System.out.print("Título da música: ");
                        String titulo = sc.nextLine();

                        Date data = null;
                        while (data == null) {
                            try {
                                System.out.print("Data de criação (YYYY-MM-DD): ");
                                data = Date.valueOf(LocalDate.parse(sc.nextLine()));
                            } catch (DateTimeParseException e) {
                                System.out.println("Formato de data inválido. Use YYYY-MM-DD.");
                            }
                        }

                        System.out.print("Nome do Autor: ");
                        String autor = sc.nextLine();

                        System.out.print("ID do Género: ");
                        String generoId = sc.nextLine();

                        System.out.print("Título do Álbum: ");
                        String album = sc.nextLine();

                        int ordem = 0;
                        try {
                            System.out.print("Ordem no álbum (número): ");
                            ordem = Integer.parseInt(sc.nextLine());
                        } catch (NumberFormatException e) {
                            System.out.println("Entrada inválida.");
                        }

                        // Chamada ao método para adicionar uma nova música
                        app.adicionarMusica(musicaId, data, titulo, ordem, album, generoId, autor);
                        break;

                    case 2:
                        try {
                            System.out.print("ID da música (UUID): ");
                            UUID idUpdate = UUID.fromString(sc.nextLine());
                            System.out.print("Novo título: ");
                            String novoTitulo = sc.nextLine();
                            // Corrige o título de uma música indicando o seu identificador [cite: 204]
                            app.atualizarTitulo(idUpdate, novoTitulo);
                        } catch (IllegalArgumentException e) {
                            System.out.println("ID UUID inválido.");
                        }
                        break;

                    case 3:
                        System.out.print("Introduza o nome da música que deseja apagar: ");
                        String nomeBusca = sc.nextLine();

                        // 1. Procurar músicas com esse nome
                        List<String[]> encontradas = app.procurarMusicasParaSelecao(nomeBusca);

                        if (encontradas.isEmpty()) {
                            System.out.println("Nenhuma música encontrada com esse nome.");
                        } else {
                            System.out.println("\n--- Músicas Encontradas ---");
                            for (int i = 0; i < encontradas.size(); i++) {
                                String[] m = encontradas.get(i);
                                // Listagem numerada com Título, Autor e Género para segurança
                                System.out.printf("[%d] %s | Autor: %s | Género: %s%n", i + 1, m[1], m[2], m[3]);
                            }

                            System.out.print("\nEscolha o número da música a remover (ou 0 para cancelar): ");
                            try {
                                int escolha = Integer.parseInt(sc.nextLine());

                                if (escolha > 0 && escolha <= encontradas.size()) {
                                    // Obtém o ID da música selecionada e chama o método de remoção
                                    String idSelecionado = encontradas.get(escolha - 1)[0];
                                    app.removerMusica(UUID.fromString(idSelecionado));
                                } else {
                                    System.out.println("Operação cancelada.");
                                }
                            } catch (NumberFormatException e) {
                                System.out.println("Entrada inválida. Operação abortada.");
                            }
                        }
                        break;


                    case 4:
                        // Consultar todos os detalhes das músicas, incluindo género, álbum e autor [cite: 206]
                        app.listarMusicas();
                        break;



                    case 5:
                        // [Extra] Visualizar uma listagem personalizada (playlist) aleatória [cite: 207, 208]
                        System.out.print("Género musical pretendido: ");
                        String genPlaylist = sc.nextLine();

                        try {
                            System.out.print("Número de músicas: ");
                            int limite = Integer.parseInt(sc.nextLine());
                            app.gerarPlaylist(genPlaylist, limite);
                        } catch (NumberFormatException e) {
                            System.out.println("Número inválido.");
                        }
                        break;

                    case 0:
                        System.out.println("A encerrar a aplicação...");
                        break;

                    default:
                        System.out.println("Opção inválida! Tente novamente.");
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