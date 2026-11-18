import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Proxy;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/** Testes de comportamento com dispositivo falso; nao comandam a impressora real. */
public class Testes {
    private static int verificacoes;

    static class HardwareFalso {
        int retorno;
        int retornoTexto = 42;
        int status = 5;
        boolean falharDll;
        String ultimoMetodo;
        Object[] ultimosArgumentos;
        final List<String> chamadas = new ArrayList<>();

        DispositivoImpressora dispositivo() {
            return (DispositivoImpressora) Proxy.newProxyInstance(
                    DispositivoImpressora.class.getClassLoader(), new Class<?>[]{DispositivoImpressora.class},
                    (objeto, metodo, argumentos) -> {
                        if (falharDll) throw new UnsatisfiedLinkError("DLL indisponivel no teste");
                        ultimoMetodo = metodo.getName();
                        ultimosArgumentos = argumentos;
                        chamadas.add(ultimoMetodo);
                        return switch (ultimoMetodo) {
                            case "texto" -> retornoTexto;
                            case "statusPapel" -> status;
                            default -> retorno;
                        };
                    });
        }
    }

    private static void conferir(boolean condicao, String descricao) {
        verificacoes++;
        if (!condicao) throw new AssertionError(descricao);
    }

    private static String capturar(Runnable acao) {
        PrintStream original = System.out;
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        try (PrintStream saida = new PrintStream(buffer, true, StandardCharsets.UTF_8)) {
            System.setOut(saida);
            acao.run();
        } finally {
            System.setOut(original);
        }
        return buffer.toString(StandardCharsets.UTF_8);
    }

    private static Entrada entrada(String dados) { return new Entrada(new Scanner(dados)); }

    public static void main(String[] args) throws Exception {
        Usuario usuario = new Usuario("admin", "1234");
        conferir(usuario.autenticar("admin", "1234"), "Login correto");
        conferir(!usuario.autenticar("admin", "errada"), "Senha incorreta");
        String loginErrado = capturar(() -> conferir(!entrada("x\nx\nx\nx\nx\nx\n").login(usuario), "Tres falhas encerram"));
        conferir(loginErrado.contains("restantes: 2") && loginErrado.contains("restantes: 1")
                && loginErrado.contains("restantes: 0"), "Contagem de tentativas");
        capturar(() -> conferir(entrada("x\nx\nadmin\n1234\n").login(usuario), "Login na segunda tentativa"));
        capturar(() -> conferir(entrada("abc\n999999999999\n-1\n14\n5\n").inteiro("Numero: ", 0, 13) == 5,
                "Letras, overflow e limites sao rejeitados"));

        HardwareFalso hw = new HardwareFalso();
        Impressora impressora = new Impressora(hw.dispositivo());
        conferir(!impressora.estaConectada(), "Inicia desconectada");
        String bloqueio = capturar(() -> Main.executarMenu(entrada("4\n5\n6\n7\n8\n9\n10\n11\n12\n13\n0\n"), impressora));
        conferir(bloqueio.contains("Conexao fechada"), "Aviso de conexao");
        conferir(!bloqueio.contains("Texto: ") && !bloqueio.contains("Caminho do XML")
                && !bloqueio.contains("Quantidade de bips"), "Menu nao solicita dados desconectado");
        conferir(hw.chamadas.isEmpty(), "Menu desconectado nao toca no hardware");
        capturar(() -> {
            impressora.imprimirTexto("Teste", 1, 0, 0, 0);
            impressora.imprimirQRCode("Teste", 2, 2);
            impressora.imprimirCodigoBarras(8, "123", 100, 2, 2);
            impressora.imprimirXml("nao-existe.xml", false);
            impressora.abrirGaveta(0, 5, 10);
            impressora.sinalSonoro(1);
            impressora.avancarPapel(1);
            impressora.cortarPapel(1);
            impressora.verificarPapel();
        });
        conferir(hw.chamadas.isEmpty(), "Metodos tambem bloqueiam hardware desconectado");

        hw.retorno = -21;
        capturar(impressora::abrirConexao);
        conferir(!impressora.estaConectada(), "Falha de abertura nao conecta");
        hw.falharDll = true;
        conferir(capturar(impressora::abrirConexao).contains("DLL"), "Erro da DLL e tratado");
        conferir(!impressora.estaConectada(), "DLL ausente nao conecta");
        hw.falharDll = false;
        hw.retorno = 0;
        capturar(impressora::abrirConexao);
        conferir(impressora.estaConectada(), "Abertura com zero conecta");
        int quantidade = hw.chamadas.size();
        capturar(impressora::abrirConexao);
        conferir(hw.chamadas.size() == quantidade, "Nao reabre conexao aberta");
        String configuracao = impressora.configuracaoAtual();
        capturar(() -> impressora.configurar(3, "i8", "192.168.0.20", 9100));
        conferir(configuracao.equals(impressora.configuracaoAtual()), "Nao reconfigura conectada");

        hw.chamadas.clear();
        capturar(() -> impressora.imprimirTexto("Teste", 3, 1, 8, 0));
        conferir(hw.chamadas.equals(List.of("texto", "texto", "texto", "avancar")), "Texto positivo e tres copias com avanco");
        hw.retornoTexto = -3;
        hw.chamadas.clear();
        String falhaTexto = capturar(() -> impressora.imprimirTexto("Teste", 3, 0, 0, 0));
        conferir(hw.chamadas.equals(List.of("texto")) && falhaTexto.contains("Codigo: -3"), "Erro de texto interrompe copias e avanco");
        hw.retornoTexto = 0;
        conferir(capturar(() -> impressora.imprimirTexto("Teste", 1, 0, 0, 0)).contains("sucesso"), "Texto zero tambem e sucesso");
        hw.retorno = -1;
        hw.chamadas.clear();
        capturar(() -> impressora.imprimirQRCode("teste", 3, 2));
        conferir(hw.chamadas.equals(List.of("qrCode")), "QR falho nao avanca papel");
        hw.retorno = 0;
        capturar(() -> impressora.imprimirCodigoBarras(8, "12345", 100, 2, 2));
        // Consulta especifica dos argumentos do codigo antes do avanco automatico.
        List<Object[]> codigo = new ArrayList<>();
        DispositivoImpressora monitor = (DispositivoImpressora) Proxy.newProxyInstance(
                DispositivoImpressora.class.getClassLoader(), new Class<?>[]{DispositivoImpressora.class},
                (obj, metodo, params) -> { if (metodo.getName().equals("codigoBarras")) codigo.add(params); return 0; });
        Impressora codigoPrinter = new Impressora(monitor);
        capturar(() -> { codigoPrinter.abrirConexao(); codigoPrinter.imprimirCodigoBarras(8, "123", 100, 2, 2); });
        conferir(codigo.getFirst()[1].equals("{A123"), "Prefixo CODE128");

        Path xml = Files.createTempFile("elgin-teste-", ".xml");
        try {
            Files.writeString(xml, "<CFe>teste</CFe>");
            capturar(() -> impressora.imprimirXml(xml.toString(), false));
            conferir(hw.ultimoMetodo.equals("xmlVenda"), "XML venda encaminhado");
            capturar(() -> impressora.imprimirXml(xml.toString(), true));
            conferir(hw.ultimoMetodo.equals("xmlCancelamento"), "XML cancelamento encaminhado");
            String assinaturaFornecida = Files.readString(Path.of("testes/assinatura-esperada.txt")).trim();
            conferir(hw.ultimosArgumentos[1].equals(assinaturaFornecida), "Assinatura identica ao material fornecido");
            Files.writeString(xml, "");
            conferir(capturar(() -> impressora.imprimirXml(xml.toString(), false)).contains("vazio"), "XML vazio tratado");
        } finally { Files.deleteIfExists(xml); }
        quantidade = hw.chamadas.size();
        conferir(capturar(() -> impressora.imprimirXml("arquivo-inexistente-para-teste.xml", false)).contains("Nao foi possivel ler"), "Arquivo inexistente tratado");
        conferir(hw.chamadas.size() == quantidade, "Arquivo inexistente nao chega a DLL");
        conferir(capturar(() -> impressora.imprimirXml("\u0000", false)).contains("Nao foi possivel ler"), "Caminho invalido tratado");
        for (int status : new int[]{5, 6, 7, -1}) {
            hw.status = status;
            String saida = capturar(impressora::verificarPapel);
            conferir(saida.contains(switch (status) { case 5 -> "Papel OK"; case 6 -> "acabando"; case 7 -> "Sem papel"; default -> "Codigo: -1"; }), "Status " + status);
        }
        capturar(() -> { impressora.abrirGaveta(1, 5, 10); impressora.sinalSonoro(3); impressora.avancarPapel(2); impressora.cortarPapel(5); });
        conferir(hw.chamadas.containsAll(List.of("gaveta", "bip", "avancar", "cortar")), "Operacoes auxiliares encaminhadas");
        hw.retorno = -1;
        capturar(impressora::fecharConexao);
        conferir(impressora.estaConectada(), "Falha de fechamento preserva estado");
        hw.retorno = 0;
        capturar(impressora::fecharConexao);
        conferir(!impressora.estaConectada(), "Fechamento bem sucedido");
        capturar(() -> impressora.configurar(3, "i8", "192.168.0.20", 9100));
        conferir(impressora.configuracaoAtual().contains("9100"), "Configuracao apos fechamento");
        java.io.InputStream entradaOriginal = System.in;
        try {
            System.setIn(new java.io.ByteArrayInputStream(new byte[0]));
            conferir(capturar(() -> Main.main(new String[0])).contains("Entrada encerrada"), "EOF encerra sem travar");
        } finally { System.setIn(entradaOriginal); }
        System.out.println("OK: " + verificacoes + " verificacoes passaram. Hardware real nao foi acionado.");
    }
}
