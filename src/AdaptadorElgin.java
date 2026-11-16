/** Carrega a DLL apenas quando o usuario solicita uma operacao real. */
public class AdaptadorElgin implements DispositivoImpressora {
    public int abrir(int tipo, String modelo, String conexao, int parametro) {
        return ImpressoraDLL.INSTANCE.AbreConexaoImpressora(tipo, modelo, conexao, parametro);
    }
    public int fechar() { return ImpressoraDLL.INSTANCE.FechaConexaoImpressora(); }
    public int texto(String dados, int posicao, int estilo, int tamanho) {
        return ImpressoraDLL.INSTANCE.ImpressaoTexto(dados, posicao, estilo, tamanho);
    }
    public int qrCode(String dados, int tamanho, int correcao) {
        return ImpressoraDLL.INSTANCE.ImpressaoQRCode(dados, tamanho, correcao);
    }
    public int codigoBarras(int tipo, String dados, int altura, int largura, int hri) {
        return ImpressoraDLL.INSTANCE.ImpressaoCodigoBarras(tipo, dados, altura, largura, hri);
    }
    public int xmlVenda(String xml) { return ImpressoraDLL.INSTANCE.ImprimeXMLSAT(xml, 0); }
    public int xmlCancelamento(String xml, String assinatura) {
        return ImpressoraDLL.INSTANCE.ImprimeXMLCancelamentoSAT(xml, assinatura, 0);
    }
    public int gaveta(int pino, int tempoInicial, int tempoFinal) {
        return ImpressoraDLL.INSTANCE.AbreGaveta(pino, tempoInicial, tempoFinal);
    }
    public int bip(int quantidade, int duracao, int intervalo) {
        return ImpressoraDLL.INSTANCE.SinalSonoro(quantidade, duracao, intervalo);
    }
    public int avancar(int linhas) { return ImpressoraDLL.INSTANCE.AvancaPapel(linhas); }
    public int statusPapel() { return ImpressoraDLL.INSTANCE.StatusImpressora(3); }
    public int cortar(int avanco) { return ImpressoraDLL.INSTANCE.Corte(avanco); }
}
