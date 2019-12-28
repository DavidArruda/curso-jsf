package meuprimeiroprojetojsf;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import br.com.dao.DaoGeneric;
import br.com.entidades.Pessoa;

@ViewScoped
@ManagedBean(name = "pessoaBean") // Controla alguma pagina xhtml
public class PessoaBean {

	private Pessoa pessoa = new Pessoa();
	private DaoGeneric<Pessoa> daoGeneric = new DaoGeneric<Pessoa>();
	private List<Pessoa> pessoas = new ArrayList<Pessoa>();

	public String salvar() {
		pessoa = daoGeneric.merge(pessoa);
		listarPessoas();
		return "";
	}
	
	public String novo() {
		pessoa = new Pessoa();
		return "";	
	}
	
	public String delete() {
		daoGeneric.deletePorId(pessoa);
		pessoa = new Pessoa();
		listarPessoas();
		return "";
	}
	
	@PostConstruct
	public void listarPessoas() {
		pessoas = daoGeneric.listEntity(Pessoa.class);
	}
	
	public List<Pessoa> getPessoas() {
		return pessoas;
	}

	public Pessoa getPessoa() {
		return pessoa;
	}

	public void setPessoa(Pessoa pessoa) {
		this.pessoa = pessoa;
	}

	public DaoGeneric<Pessoa> getDaoGeneric() {
		return daoGeneric;
	}

	public void setDaoGeneric(DaoGeneric<Pessoa> daoGeneric) {
		this.daoGeneric = daoGeneric;
	}

}