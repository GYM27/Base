package aor;

/** objeto para fins estéticos */
public class Design {


    // --- ATRIBUTOS

    // cores ANSI
    public static String RESET = "\u001B[0m";
    static String GREEN = "\u001B[32m";
    static String YELLOW = "\u001B[33m";
    public static String CYAN = "\u001B[36m";
    static String RED = "\u001B[31m";
    static String ORANGE = "\u001B[38;5;208m";

    private static final int LARGURA_INTERNA = 36;


    // --- MÉTODOS

    /**
     * imprimir caixa que emoldura títulos
     * @param titulo título a escrever dentro da caixa
     * @param cor cor pretendida para a caixa e texto
     */
    public static void caixaTitulo(String titulo, String cor) {

        int len = titulo.length();
        int totalEspacos = Math.max(0, LARGURA_INTERNA - len);
        int left = totalEspacos / 2;
        int right = totalEspacos - left;
        String tituloFormatado = " ".repeat(left) + titulo + " ".repeat(right);

        System.out.println(cor + "\n╔══════════════════════════════════════╗" + RESET);
        System.out.printf(cor + "║ %s ║%n" + RESET, tituloFormatado);
        System.out.println(cor + "╚══════════════════════════════════════╝\n" + RESET);
    }

    /**
     * imprimir linha para efeitos estéticos e de compreensão no decorrer do programa
     */
    public static void separador() {
        System.out.println(YELLOW + "\n────────────────────────────────────────────────\n" + RESET);
    }

    /**
     * imprimir texto com cor
     * @param mensagem texto a escrever
     * @param cor cor pretendida
     */
    public static void imprimir(String mensagem, String cor) {
        System.out.println(cor + mensagem + "\n" + RESET);
    }

    /**
     * pedir opção de menu/input ao utilizador
     */
    public static void opcao(){
        System.out.print(ORANGE + "Opção: " + RESET);
    }

    /**
     * informar utilizador de que pode digitar '0' para cancelar uma ação
     */
    public static void mensagemCancelar() {
        System.out.println(ORANGE + "Digite '0' a qualquer altura para cancelar a ação.\n" + RESET);
    }

    /**
     * imprimir mensagem de erro
     * @param mensagem texto que explica erro
     */
    public static void erro(String mensagem) {
        System.out.println(RED + mensagem + RESET);
    }

    /**
     * imprimir mensagem de sucesso
     * @param mensagem texto que informa sucesso
     */
    public static void sucesso(String mensagem) {
        System.out.println(GREEN + mensagem + RESET);
    }
}