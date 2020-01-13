package br.com.meuprimeiroprojetojsf;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.event.AjaxBehaviorEvent;
import javax.servlet.http.HttpServletRequest;

import com.google.gson.Gson;

import br.com.dao.DaoGeneric;
import br.com.entidades.Pessoa;
import br.com.repository.IDaoPessoa;
import br.com.repository.IDaoPessoaImpl;

@ViewScoped
@ManagedBean(name = "pessoaBean") // Controla alguma pagina xhtml
public class PessoaBean {

	private Pessoa pessoa = new Pessoa();
	private DaoGeneric<Pessoa> daoGeneric = new DaoGeneric<Pessoa>();
	private List<Pessoa> pessoas = new ArrayList<Pessoa>();
	private IDaoPessoa iDaoPessoa = new IDaoPessoaImpl();

	public String salvar() {
		pessoa = daoGeneric.merge(pessoa);

		listarPessoas();

		mostrarMsg("Salvo com sucesso");

		return "";
	}

	private void mostrarMsg(String msg) {

		FacesContext context = FacesContext.getCurrentInstance();

		FacesMessage message = new FacesMessage(msg);

		context.addMessage(null, message);
	}

	public String novo() {
		pessoa = new Pessoa();
		return "";
	}

	public String limpar() {
		pessoa = new Pessoa();
		return "";
	}

	public String delete() {
		daoGeneric.deletePorId(pessoa);
		pessoa = new Pessoa();
		listarPessoas();
		mostrarMsg("Removido com sucesso");
		return "";
	}

	public void pesquisaCep(AjaxBehaviorEvent event) {

		try {

			URL url = new URL("https://viacep.com.br/ws/" + pessoa.getCep() + "/json/");

			URLConnection connection = url.openConnection();

			InputStream is = connection.getInputStream();

			BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8"));

			String cep = "";

			StringBuilder json = new StringBuilder();

			while ((cep = br.readLine()) != null) {
				json.append(cep);
			}

			Pessoa gson = new Gson().fromJson(json.toString(), Pessoa.class);

			pessoa.setCep(gson.getCep());
			pessoa.setLogradouro(gson.getLogradouro());
			pessoa.setComplemento(gson.getComplemento());
			pessoa.setBairro(gson.getBairro());
			pessoa.setLocalidade(gson.getLocalidade());
			pessoa.setUf(gson.getUf());
			pessoa.setUnidade(gson.getUnidade());
			pessoa.setIbge(gson.getIbge());
			pessoa.setGia(gson.getGia());

		} catch (Exception e) {
			e.printStackTrace();
			mostrarMsg("Erro ao consultar CEP");
		}
	}
	
	public String deslogar() {
		
		//invalidate do jsf
		//FacesContext.getCurrentInstance().getExternalContext().invalidateSession();
		
		FacesContext context = FacesContext.getCurrentInstance();
		
		ExternalContext externalContext = context.getExternalContext();
		externalContext.getSessionMap().remove("usuarioLogado");
		
		HttpServletRequest httpServletRequest = (HttpServletRequest)
				context.getCurrentInstance().
		getExternalContext().getRequest();
		
		httpServletRequest.getSession().invalidate();
		
		return "index.jsf";
	}

	@PostConstruct
	public void listarPessoas() {
		pessoas = daoGeneric.listEntity(Pessoa.class);
	}

	public String logar() {
		Pessoa pessoaUser = iDaoPessoa.consultarPessoa(pessoa.getLogin(), pessoa.getSenha());

		if (pessoaUser != null) {

			// adicionar o usuario na sessao "usuarioLogado"
			FacesContext facesContext = FacesContext.getCurrentInstance(); // FacesContext contem informações do
																			// ambiente de execução 'JSF"
			ExternalContext externalContext = facesContext.getExternalContext();
			externalContext.getSessionMap().put("usuarioLogado", pessoaUser);
			return "primeirapagina.jsf";
		}

		return "index.jsf";
	}

	public boolean permiteAcesso(String acesso) {

		FacesContext facesContext = FacesContext.getCurrentInstance(); // FacesContext contem informações do ambiente de
																		// execução 'JSF"
		ExternalContext externalContext = facesContext.getExternalContext();
		Pessoa pessoaUser = (Pessoa) externalContext.getSessionMap().get("usuarioLogado");

		return pessoaUser.getPerfilUser().equals(acesso);

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
