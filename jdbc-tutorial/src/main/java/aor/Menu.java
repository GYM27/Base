package aor;

import java.sql.Date;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Scanner;
import java.util.UUID;

import static aor.Design.CYAN;

public class Menu {

    static void appMenu() {

        Design.caixaTitulo("==== MENU MUSI-APP ====", Design.CYAN);

                System.out.println("1- Adicionar música");
                System.out.println("2- Atualizar título");
                System.out.println("3- Remover música");
                System.out.println("4- Listar todas as músicas");
                System.out.println("5- Gerar Playlist Aleatória (Extra)");
                System.out.println("0- Sair");
                Design.opcao();
    }

    static void adicionarMusica() throws SQLException {
        Scanner sc = new Scanner(System.in);
        App app = new App();

        Design.separador();
        Design.imprimir("--- Adicionar Nova Música ---", CYAN);


        // 1. Título (Obrigatório)
        String titulo;
        do {
            System.out.print("Título da música: ");
            titulo = sc.nextLine().trim();
            if (titulo.isEmpty()) System.out.println("Erro: O título não pode estar vazio.");
        } while (titulo.isEmpty());


        // 2. Data (Validação de Formato)
        Date data = null;
        while (data == null) {
            System.out.print("Data de criação (YYYY-MM-DD): ");
            String input = sc.nextLine().trim();
            try {
                data = Date.valueOf(LocalDate.parse(input));
            } catch (DateTimeParseException e) {
                System.out.println("Erro: Formato inválido. Tente novamente.");
            }
        }

        // 3. Autor (Obrigatório)
        String autor;
        do {
            System.out.print("Nome do Autor: ");
            autor = sc.nextLine().trim();
            if (autor.isEmpty()) System.out.println("Erro: O autor é obrigatório.");
        } while (autor.isEmpty());

        // 4. Género
        String genero;
        do {
            System.out.print("Género musical: ");
            genero = sc.nextLine().trim();
            if (genero.isEmpty()) System.out.println("Erro: Indique um género.");
        } while (genero.isEmpty());

        // 5. Álbum e Ordem (Agora tudo como String)
        System.out.print("Título do Álbum (Pressione Enter para Single): ");
        String album = sc.nextLine().trim();

        String ordem = null; // Declarado como String

        if (!album.isEmpty()) {
            boolean ordemValida = false;
            while (!ordemValida) {
                System.out.print("Ordem da música no álbum: ");
                ordem = sc.nextLine().trim();

                if (!ordem.isEmpty()) {
                    // Validamos se é um número apenas para garantir dados limpos
                    if (ordem.matches("\\d+")) {
                        ordemValida = true;
                    } else {
                        System.out.println("Erro: A ordem deve ser um número.");
                    }
                } else {
                    System.out.println("Erro: Já que indicou um álbum, a ordem é obrigatória.");
                }
            }
        } else {
            album = null; // Garante NULL na BD se for Single
            ordem = null;
        }

        // Envia a 'ordem' como String para o método da App
        app.adicionarMusica(data, titulo, ordem, album, genero, autor);
        Design.sucesso("Música adicionada com sucesso!");
    }

    static void atualizarMusica() throws SQLException {
        Scanner sc = new Scanner(System.in);

        // Usamos o try-with-resources para garantir que a ligação fecha
        try (App app = new App()) {
            Design.separador();
            Design.imprimir("   ATUALIZAR TÍTULO", CYAN);
            Design.separador();

            // 1. Pedir o ID de forma robusta
            int idUpdate = -1;
            while (idUpdate < 0) {
                System.out.print("Introduza o ID numérico da música: ");
                try {
                    idUpdate = Integer.parseInt(sc.nextLine());
                } catch (NumberFormatException e) {
                    System.out.println("Erro: O ID deve ser um número inteiro.");
                }
            }

            // 2. Pedir o novo título (Garantindo que não está vazio)
            String novoTitulo = "";
            while (novoTitulo.isEmpty()) {
                System.out.print("Novo título para a música: ");
                novoTitulo = sc.nextLine().trim();
                if (novoTitulo.isEmpty()) {
                    System.out.println("Erro: O título não pode estar vazio.");
                }
            }

            // 3. Chamar o método da App (agora com int e String)
            app.atualizarTitulo(idUpdate, novoTitulo);

        } catch (Exception e) {
            System.out.println("Erro ao atualizar música: " + e.getMessage());
        }
    }

    static void removerMusica() throws SQLException {
        Scanner sc = new Scanner(System.in);

        try (App app = new App()) {
            Design.separador();
            Design.imprimir("--- Remoção com Validação ---", CYAN);

            System.out.print("Introduza o ID da música que deseja apagar: ");
            String input = sc.nextLine().trim();

            if (input.isEmpty()) return;

            try {
                int id = Integer.parseInt(input);

                // 1. Validar se a música existe e obter o nome
                String[] dados = app.buscarMusicaPorId(id);

                if (dados == null) {
                    Design.erro("Não existe nenhuma música com o ID " + id);
                } else {
                    // 2. Apresentar o nome e pedir confirmação
                    System.out.printf("%n  CONFIRMAÇÃO DE REMOÇÃO%n");
                    System.out.printf("Música: %s%n", dados[0]);
                    System.out.printf("Autor:  %s%n", dados[1]);
                    System.out.print("\nTem a certeza que deseja apagar? (S/N): ");

                    String confirmacao = sc.nextLine().trim().toUpperCase();

                    if (confirmacao.equals("S")) {
                        app.removerMusica(id);
                    } else {
                        System.out.println("Remoção cancelada pelo utilizador.");
                    }
                }
            } catch (NumberFormatException e) {
                Design.erro("ID inválido. Introduza um número inteiro.");
            }
        }
    }

    static void gerarPlaylist() throws SQLException {
        Scanner sc = new Scanner(System.in);

        // 1. Usar try-with-resources para gestão eficiente da ligação
        try (App app = new App()) {
            Design.separador();
            Design.imprimir("   GERAR NOVA PLAYLIST", CYAN);
            Design.separador();

            // 2. Validar o Género (Não pode ser vazio)
            String genPlaylist = "";
            while (genPlaylist.isEmpty()) {
                System.out.print("Género musical pretendido (ex: Rock, Fado): ");
                genPlaylist = sc.nextLine().trim();
                if (genPlaylist.isEmpty()) {
                    System.out.println("Erro: Deve indicar um género para a pesquisa.");
                }
            }

            // 3. Validar o Limite (Deve ser um número positivo)
            int limite = 0;
            while (limite <= 0) {
                System.out.print("Número de músicas desejado: ");
                try {
                    limite = Integer.parseInt(sc.nextLine());
                    if (limite <= 0) {
                        System.out.println("Erro: O número deve ser maior que zero.");
                    }
                } catch (NumberFormatException e) {
                    System.out.println("Erro: Por favor, introduza um número inteiro válido.");
                }
            }

            // 4. Chamar a lógica de negócio
            app.gerarPlaylist(genPlaylist, limite);

        } catch (Exception e) {
            System.out.println("Erro ao gerar a playlist: " + e.getMessage());
        }
    }
}
