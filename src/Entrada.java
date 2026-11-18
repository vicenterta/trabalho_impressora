import java.util.NoSuchElementException;
import java.util.Scanner;

/** Centraliza leitura, validacao e o tratamento do fim da entrada. */
public class Entrada {
    private final Scanner scanner;

    public Entrada(Scanner scanner) {
        this.scanner = scanner;
    }

    public String texto(String pergunta) {
        System.out.print(pergunta);
        if (!scanner.hasNextLine()) {
            throw new NoSuchElementException("Entrada encerrada.");
        }
        return scanner.nextLine();
    }

    public String obrigatorio(String pergunta) {
        while (true) {
            String valor = texto(pergunta).trim();
            if (!valor.isEmpty()) return valor;
            System.out.println("Preencha este campo.");
        }
    }

    public int inteiro(String pergunta, int minimo, int maximo) {
        while (true) {
            try {
                int valor = Integer.parseInt(texto(pergunta).trim());
                if (valor >= minimo && valor <= maximo) return valor;
                System.out.println("Digite um numero entre " + minimo + " e " + maximo + ".");
            } catch (NumberFormatException e) {
                System.out.println("Valor invalido. Digite um numero inteiro.");
            }
        }
    }

    public boolean login(Usuario usuario) {
        for (int tentativa = 1; tentativa <= 3; tentativa++) {
            String nome = texto("Usuario: ");
            String senha = texto("Senha: ");
            if (usuario.autenticar(nome, senha)) {
                System.out.println("Login realizado com sucesso!");
                return true;
            }
            System.out.println("Usuario ou senha incorretos. Tentativas restantes: " + (3 - tentativa));
        }
        System.out.println("Tres tentativas incorretas. Programa encerrado.");
        return false;
    }
}
