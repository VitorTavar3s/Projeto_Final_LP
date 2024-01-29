import java.util.ArrayList;
import java.util.List;

public class Contato {

    private Long id;
    private String nome;
    private String sobreNome;
    private List<Telefone> telefones;


    public Contato(Long id, String nome, String sobreNome){
        this.id = id;
        this.nome = nome;
        this.sobreNome = sobreNome;
        this.telefones = new ArrayList<>();
    }

    public void setId(Long id) {
        this.id = id;
    }
    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }
    public void setNome(String nome) {
        this.nome = nome;
    }

    public void setSobreNome(String sobreNome) {
        this.sobreNome = sobreNome;
    }
    public String getSobreNome() {
        return sobreNome;
    }

    public void setTelefones(List<Telefone> telefones) {
        this.telefones = telefones;
    }
    public List<Telefone> getTelefones() {
        return telefones;
    }


}
