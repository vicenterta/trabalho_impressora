import java.util.NoSuchElementException;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Entrada entrada = new Entrada(new Scanner(System.in));
        Impressora impressora = new Impressora();
        try {
            if (entrada.login(new Usuario("admin", "1234"))) {
                executarMenu(entrada, impressora);
            }
        } catch (NoSuchElementException e) {
            System.out.println("Entrada encerrada. Saindo.");
        } finally {
            if (impressora.estaConectada()) impressora.fecharConexao();
        }
    }

    static void executarMenu(Entrada entrada, Impressora impressora) {
        while (true) {
            System.out.println("\n=== CONTROLE ELGIN ===\n" + impressora.configuracaoAtual());
            System.out.println("""
                    1  - Configurar conexao
                    2  - Abrir conexao
                    3  - Fechar conexao
                    4  - Imprimir texto
                    5  - Imprimir QR Code
                    6  - Imprimir codigo de barras
                    7  - Imprimir XML SAT
                    8  - Imprimir cancelamento SAT
                    9  - Abrir gaveta
                    10 - Sinal sonoro
                    11 - Avancar papel
                    12 - Consultar papel
                    13 - Cortar papel
                    0  - Sair
                    """);
            int opcao = entrada.inteiro("Opcao: ", 0, 13);

            if (opcao >= 4 && !impressora.exigirConexao()) continue;
            switch (opcao) {
                case 0 -> { System.out.println("Ate logo!"); return; }
                case 1 -> configurar(entrada, impressora);
                case 2 -> impressora.abrirConexao();
                case 3 -> impressora.fecharConexao();
                case 4 -> {
                    String texto = entrada.obrigatorio("Texto: ");
                    int copias = entrada.inteiro("Copias (1 a 20): ", 1, 20);
                    int posicao = entrada.inteiro("Alinhamento (0=esquerda, 1=centro, 2=direita): ", 0, 2);
                    int estilo = entrada.inteiro("Estilo (0=normal, 1=fonte B, 2=sublinhado, 4=invertido, 8=negrito; somaveis): ", 0, 15);
                    int tamanho = entrada.inteiro("Tamanho (0=normal, 1 a 7=ampliado): ", 0, 7);
                    impressora.imprimirTexto(texto, copias, posicao, estilo, tamanho);
                }
                case 5 -> {
                    String dados = entrada.obrigatorio("Conteudo do QR Code: ");
                    int tamanho = entrada.inteiro("Tamanho (1 a 6): ", 1, 6);
                    int correcao = entrada.inteiro("Nivel de correcao (1 a 4): ", 1, 4);
                    impressora.imprimirQRCode(dados, tamanho, correcao);
                }
                case 6 -> codigoBarras(entrada, impressora);
                case 7, 8 -> {
                    String padrao = opcao == 7 ? "XMLSAT.xml" : "CANC_SAT.xml";
                    String arquivo = entrada.texto("Caminho do XML (Enter = " + padrao + "): ").trim();
                    impressora.imprimirXml(arquivo.isEmpty() ? padrao : arquivo, opcao == 8);
                }
                case 9 -> {
                    int pino = entrada.inteiro("Pino da gaveta (0 ou 1): ", 0, 1);
                    int ti = entrada.inteiro("Tempo inicial (1 a 255): ", 1, 255);
                    int tf = entrada.inteiro("Tempo final (1 a 255): ", 1, 255);
                    impressora.abrirGaveta(pino, ti, tf);
                }
                case 10 -> impressora.sinalSonoro(entrada.inteiro("Quantidade de bips (1 a 63): ", 1, 63));
                case 11 -> impressora.avancarPapel(entrada.inteiro("Linhas (1 a 100): ", 1, 100));
                case 12 -> impressora.verificarPapel();
                case 13 -> impressora.cortarPapel(entrada.inteiro("Avanco antes do corte (0 a 20): ", 0, 20));
            }
        }
    }

    private static void codigoBarras(Entrada entrada, Impressora impressora) {
        int tipo;
        do {
            tipo = entrada.inteiro("Tipo (0=UPC-A, 2=EAN13, 3=EAN8, 4=CODE39, 8=CODE128): ", 0, 8);
            if (tipo != 0 && tipo != 2 && tipo != 3 && tipo != 4 && tipo != 8)
                System.out.println("Escolha um dos tipos listados.");
        } while (tipo != 0 && tipo != 2 && tipo != 3 && tipo != 4 && tipo != 8);
        String dados = entrada.obrigatorio("Dados (exemplo CODE128: 012345678912): ");
        int altura = entrada.inteiro("Altura (1 a 255): ", 1, 255);
        int largura = entrada.inteiro("Largura (1 a 6): ", 1, 6);
        int hri = entrada.inteiro("Legenda (1=acima, 2=abaixo, 3=ambos, 4=oculta): ", 1, 4);
        impressora.imprimirCodigoBarras(tipo, dados, altura, largura, hri);
    }

    private static void configurar(Entrada entrada, Impressora impressora) {
        if (impressora.estaConectada()) {
            System.out.println("Feche a conexao antes de alterar a configuracao.");
            return;
        }
        int tipo = entrada.inteiro("Tipo (1=USB, 2=Serial, 3=Rede, 4=Bluetooth): ", 1, 4);
        String modelo;
        do {
            modelo = entrada.obrigatorio("Modelo (i8 ou i9): ");
            if (!modelo.equalsIgnoreCase("i8") && !modelo.equalsIgnoreCase("i9"))
                System.out.println("Informe i8 ou i9.");
        } while (!modelo.equalsIgnoreCase("i8") && !modelo.equalsIgnoreCase("i9"));
        String porta = entrada.obrigatorio("Conexao (USB, COM2 ou endereco IP): ");
        int parametro = tipo == 1 ? 0 : entrada.inteiro("Parametro (velocidade serial ou porta de rede): ", 1,
                tipo == 3 ? 65535 : 4000000);
        impressora.configurar(tipo, modelo, porta, parametro);
    }
}
