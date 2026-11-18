import java.util.function.IntSupplier;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;


public class Impressora {
    private int tipo = 1;
    private String modelo = "i9";
    private String conexao = "USB";
    private int parametro = 0;
    private boolean conexaoAberta;
    private final DispositivoImpressora dispositivo;
    private static final String ASS_QRCODE_CANCELAMENTO = "Q5DLkpdRijIRGY6YSSNsTWK1TztHL1vD0V1Jc4spo/CEUqICEb9SFy82ym8EhBRZjbh3btsZhF+sjHqEMR159i4agru9x6KsepK/q0E2e5xlU5cv3m1woYfgHyOkWDNcSdMsS6bBh2Bpq6s89yJ9Q6qh/J8YHi306ce9Tqb/drKvN2XdE5noRSS32TAWuaQEVd7u+TrvXlOQsE3fHR1D5f1saUwQLPSdIv01NF6Ny7jZwjCwv1uNDgGZONJdlTJ6p0ccqnZvuE70aHOI09elpjEO6Cd+orI7XHHrFCwhFhAcbalc+ZfO5b/+vkyAHS6CYVFCDtYR9Hi5qgdk31v23w==";

    public Impressora() { this(new AdaptadorElgin()); }

    public Impressora(DispositivoImpressora dispositivo) {
        this.dispositivo = dispositivo;
    }

    public boolean estaConectada() { return conexaoAberta; }

    public boolean exigirConexao() {
        if (!conexaoAberta) System.out.println("Conexao fechada. Abra a conexao antes.");
        return conexaoAberta;
    }

    public String configuracaoAtual() {
        return "Tipo: " + tipo + " | Modelo: " + modelo + " | Conexao: " + conexao
                + " | Parametro: " + parametro + " | " + (conexaoAberta ? "ABERTA" : "FECHADA");
    }

    public void configurar(int tipo, String modelo, String conexao, int parametro) {
        if (conexaoAberta) {
            System.out.println("Feche a conexao antes de alterar a configuracao.");
            return;
        }
        if (tipo < 1 || tipo > 4 || !(modelo.equalsIgnoreCase("i8") || modelo.equalsIgnoreCase("i9"))
                || conexao.isBlank() || parametro < 0) {
            System.out.println("Configuracao invalida.");
            return;
        }
        this.tipo = tipo;
        this.modelo = modelo.toLowerCase(java.util.Locale.ROOT);
        this.conexao = conexao;
        this.parametro = parametro;
        System.out.println("Configuracao salva.");
    }

    public void abrirConexao() {
        if (conexaoAberta) {
            System.out.println("A conexao ja esta aberta.");
            return;
        }
        conexaoAberta = executar("Abrir conexao", () -> dispositivo.abrir(tipo, modelo, conexao, parametro), false);
    }

    public void fecharConexao() {
        if (!conexaoAberta) {
            System.out.println("A conexao ja esta fechada.");
            return;
        }
        if (executar("Fechar conexao", dispositivo::fechar, false)) conexaoAberta = false;
    }

    public void imprimirTexto(String texto, int copias, int posicao, int estilo, int tamanho) {
        if (!exigirConexao()) return;
        for (int copia = 1; copia <= copias; copia++) {
            if (!executar("Imprimir copia " + copia, () -> dispositivo.texto(texto + "\n", posicao, estilo, tamanho), true)) return;
        }
        avancarPapel(3);
    }

    public void imprimirQRCode(String dados, int tamanho, int correcao) {
        if (!exigirConexao()) return;
        if (executar("Imprimir QR Code", () -> dispositivo.qrCode(dados, tamanho, correcao), false)) avancarPapel(3);
    }

    public void imprimirCodigoBarras(int tipoCodigo, String dados, int altura, int largura, int hri) {
        if (!exigirConexao()) return;
        // A DLL exige a selecao do conjunto de caracteres no CODE 128.
        String conteudo = tipoCodigo == 8 && !dados.startsWith("{") ? "{A" + dados : dados;
        if (executar("Imprimir codigo de barras", () -> dispositivo.codigoBarras(tipoCodigo, conteudo, altura, largura, hri), false)) avancarPapel(3);
    }

    public void imprimirXml(String arquivo, boolean cancelamento) {
        if (!exigirConexao()) return;
        try {
            String xml = Files.readString(Path.of(arquivo));
            if (xml.isBlank()) {
                System.out.println("Arquivo XML vazio.");
                return;
            }
            if (cancelamento) {
                executar("Imprimir cancelamento SAT", () -> dispositivo.xmlCancelamento(xml, ASS_QRCODE_CANCELAMENTO), false);
            } else {
                executar("Imprimir XML SAT", () -> dispositivo.xmlVenda(xml), false);
            }
        } catch (IOException | InvalidPathException | SecurityException e) {
            System.out.println("Nao foi possivel ler o arquivo XML: " + e.getMessage());
        }
    }

    public void abrirGaveta(int pino, int tempoInicial, int tempoFinal) {
        if (exigirConexao()) executar("Abrir gaveta", () -> dispositivo.gaveta(pino, tempoInicial, tempoFinal), false);
    }

    public void sinalSonoro(int quantidade) {
        if (exigirConexao()) executar("Sinal sonoro", () -> dispositivo.bip(quantidade, 50, 5), false);
    }

    public void avancarPapel(int linhas) {
        if (exigirConexao()) executar("Avancar papel", () -> dispositivo.avancar(linhas), false);
    }

    public void cortarPapel(int avanco) {
        if (exigirConexao()) executar("Cortar papel", () -> dispositivo.cortar(avanco), false);
    }

    public void verificarPapel() {
        if (!exigirConexao()) return;
        // Status devolve estados, por isso nao utiliza a regra de sucesso zero.
        executar("Consulta de papel", () -> {
            int status = dispositivo.statusPapel();
            switch (status) {
                case 5 -> System.out.println("Papel OK.");
                case 6 -> System.out.println("Papel acabando.");
                case 7 -> System.out.println("Sem papel.");
                default -> {
                    System.out.println("Status nao reconhecido ou sensor indisponivel. Codigo: " + status);
                    return status == 0 ? -1 : status;
                }
            }
            return 0;
        }, false);
    }


    private boolean executar(String nome, IntSupplier comando, boolean retornoTexto) {
        try {
            int retorno = comando.getAsInt();
            boolean sucesso = retornoTexto ? retorno >= 0 : retorno == 0;
            System.out.println(sucesso ? nome + ": sucesso."
                    : "Erro em " + nome + ". Codigo: " + retorno);
            return sucesso;
        } catch (LinkageError e) {
            System.out.println("Nao foi possivel carregar ou acessar a DLL. Confira as pastas x64/x86, "
                    + "a arquitetura do Java e as dependencias da Elgin. Detalhe: " + e.getMessage());
        } catch (RuntimeException e) {
            System.out.println("Falha em " + nome + ": " + e.getMessage());
        }
        return false;
    }
}
