import java.lang.reflect.Proxy;
import java.util.*;
public class Testes {
    public static void main(String[] args) {
        List<String> chamadas = new ArrayList<>();
        List<Object[]> parametros = new ArrayList<>();
        DispositivoImpressora falso = (DispositivoImpressora) Proxy.newProxyInstance(DispositivoImpressora.class.getClassLoader(), new Class<?>[]{DispositivoImpressora.class}, (o,m,a)->{chamadas.add(m.getName());parametros.add(a);return m.getName().equals("texto")?10:m.getName().equals("statusPapel")?5:0;});
        Impressora i = new Impressora(falso);
        Main.executarMenu(new Entrada(new Scanner("4\n5\n6\n7\n8\n9\n10\n0\n")), i);
        if(!chamadas.isEmpty()) throw new AssertionError("Bloqueio desconectado");
        i.abrirConexao(); chamadas.clear(); parametros.clear();
        Main.executarMenu(new Entrada(new Scanner("4\nOla\n5\nQR teste\n6\n7\n8\n9\n10\n0\n")), i);
        if(!chamadas.equals(List.of("texto","cortar","qrCode","cortar","codigoBarras","cortar","xmlVenda","cortar","xmlCancelamento","cortar","gaveta","bip"))) throw new AssertionError(chamadas);
        if(!parametros.get(4)[0].equals(8)) throw new AssertionError("CODE128 deve ser 8");
        if(!parametros.get(11)[1].equals(10)) throw new AssertionError("Bip curto");
        for(int j:new int[]{1,3,5,7,9}) if(!parametros.get(j)[0].equals(5)) throw new AssertionError("Corte fixo");
        chamadas.clear();
        i.imprimirXml("inexistente-teste.xml",false);
        if(!chamadas.isEmpty()) throw new AssertionError("Arquivo ausente nao imprime nem corta");
        System.out.println("OK: menu simplificado, parametros fixos, cinco cortes, bloqueio e arquivo ausente verificados.");
    }
}
