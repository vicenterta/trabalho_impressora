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
                    0  - Sair
                    """);
            int opcao = entrada.inteiro("Opcao: ", 0, 12);
            // Bloqueia ANTES de perguntar texto, arquivo, quantidade ou outros dados.
            if (opcao >= 4 && !impressora.exigirConexao()) continue;
            switch (opcao) {
                case 0 -> { System.out.println("Ate logo!"); return; }
                case 1 -> configurar(entrada, impressora);
                case 2 -> impressora.abrirConexao();
                case 3 -> impressora.fecharConexao();
                case 4 -> impressora.imprimirTexto(entrada.obrigatorio("Texto: "), 1, 1, 0, 0);
                case 5 -> impressora.imprimirQRCode(entrada.obrigatorio("Texto do QR Code: "), 4, 2);
                case 6 -> impressora.imprimirCodigoBarras(8, "{A012345678912", 100, 2, 3);
                case 7 -> impressora.imprimirXml("XMLSAT.xml", false);
                case 8 -> impressora.imprimirXml("CANC_SAT.xml", true);
                case 9 -> impressora.abrirGaveta(1, 5, 10);
                case 10 -> impressora.sinalSonoro(1);
                case 11 -> impressora.avancarPapel(3);
                case 12 -> impressora.verificarPapel();
            }
        }
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
