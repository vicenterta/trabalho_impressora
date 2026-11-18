/** Credenciais de demonstracao para o trabalho de POO. */
public class Usuario {
    private final String login;
    private final String senha;

    public Usuario(String login, String senha) {
        this.login = login;
        this.senha = senha;
    }

    public boolean autenticar(String loginInformado, String senhaInformada) {
        return login.equals(loginInformado) && senha.equals(senhaInformada);
    }
}
