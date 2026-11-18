/** Contrato para isolar o hardware e permitir testes sem impressora. */
public interface DispositivoImpressora {
    int abrir(int tipo, String modelo, String conexao, int parametro);
    int fechar();
    int texto(String dados, int posicao, int estilo, int tamanho);
    int qrCode(String dados, int tamanho, int correcao);
    int codigoBarras(int tipo, String dados, int altura, int largura, int hri);
    int xmlVenda(String xml);
    int xmlCancelamento(String xml, String assinatura);
    int gaveta(int pino, int tempoInicial, int tempoFinal);
    int bip(int quantidade, int duracao, int intervalo);
    int avancar(int linhas);
    int statusPapel();
    int cortar(int avanco);
}
