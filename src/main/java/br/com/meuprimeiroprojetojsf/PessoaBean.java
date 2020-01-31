package br.com.meuprimeiroprojetojsf;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.component.html.HtmlSelectOneMenu;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.event.AjaxBehaviorEvent;
import javax.faces.model.SelectItem;
import javax.imageio.ImageIO;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import javax.xml.bind.DatatypeConverter;

import com.google.gson.Gson;

import br.com.dao.DaoGeneric;
import br.com.entidades.Cidades;
import br.com.entidades.Estados;
import br.com.entidades.Pessoa;
import br.com.jpautil.JPAUtil;
import br.com.repository.IDaoPessoa;

@javax.faces.view.ViewScoped
@Named(value = "pessoaBean") // Controla alguma pagina xhtml
public class PessoaBean implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private Pessoa pessoa = new Pessoa();
	@Inject
	private DaoGeneric<Pessoa> daoGeneric;
	private List<Pessoa> pessoas = new ArrayList<Pessoa>();
	@Inject
	private IDaoPessoa iDaoPessoa;
	@Inject
	private JPAUtil jpaUtil;
	
	private List<SelectItem> estados;
	private List<SelectItem> cidades;
	private Part arquivoFoto;

	public String salvar() throws IOException {

		if (arquivoFoto != null) {

			byte[] imagemByte = getByte(arquivoFoto.getInputStream());
			pessoa.setFotoIconBase64Original(imagemByte); /* Salva foto original */

			/* Transformar em buffer image */
			BufferedImage bufferedImage = ImageIO.read(new ByteArrayInputStream(imagemByte));

			/* Pega o tipo da imagem */
			int type = bufferedImage.getType() == 0 ? BufferedImage.TYPE_INT_ARGB : bufferedImage.getType();

			int altura = 200;
			int largura = 200;

			/* Criar a miniatura */
			BufferedImage resizedImage = new BufferedImage(largura, altura, type);
			Graphics2D g = resizedImage.createGraphics();
			g.drawImage(bufferedImage, 0, 0, largura, altura, null);
			g.dispose();

			/* Escrever a imagem em tamanho menor */
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			String extensao = arquivoFoto.getContentType().split("\\/")[1];
			ImageIO.write(resizedImage, extensao, baos);

			String miniImagem = "data:" + arquivoFoto.getContentType() + ";base64,"
					+ DatatypeConverter.printBase64Binary(baos.toByteArray());

			/* Processar Imagem */
			pessoa.setFotoIconBase64(miniImagem);
			pessoa.setExtensao(extensao);

		}

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

	public void novo() {
		pessoa = new Pessoa();
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

		// invalidate do jsf
		// FacesContext.getCurrentInstance().getExternalContext().invalidateSession();

		FacesContext context = FacesContext.getCurrentInstance();

		ExternalContext externalContext = context.getExternalContext();
		externalContext.getSessionMap().remove("usuarioLogado");

		@SuppressWarnings("static-access")
		HttpServletRequest httpServletRequest = (HttpServletRequest) context.getCurrentInstance().getExternalContext()
				.getRequest();

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

	public void carregaCidades(AjaxBehaviorEvent event) {

		Estados estado = (Estados) ((HtmlSelectOneMenu) event.getSource()).getValue();

		if (estado != null) {
			pessoa.setEstados(estado);
			@SuppressWarnings("unchecked")
			List<Cidades> cidades = jpaUtil.getEntityManager()
					.createQuery("from Cidades where estados.id = " + estado.getId()).getResultList();

			List<SelectItem> selectItensCidades = new ArrayList<SelectItem>();

			for (Cidades cidade : cidades) {
				selectItensCidades.add(new SelectItem(cidade, cidade.getNome()));
			}

			setCidades(selectItensCidades);
		}
	}

	public String editar() {

		if (pessoa.getCidades() != null) {
			Estados estado = pessoa.getCidades().getEstados();
			pessoa.setEstados(estado);

			if (estado != null) {
				pessoa.setEstados(estado);
				@SuppressWarnings("unchecked")
				List<Cidades> cidades = jpaUtil.getEntityManager()
						.createQuery("from Cidades where estados.id = " + estado.getId()).getResultList();

				List<SelectItem> selectItensCidades = new ArrayList<SelectItem>();

				for (Cidades cidade : cidades) {
					selectItensCidades.add(new SelectItem(cidade, cidade.getNome()));
				}

				setCidades(selectItensCidades);
			}

		}

		return "";
	}

	public void download() throws IOException {
		Map<String, String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
		String fileDonwloadId = params.get("fileDownloadId");

		Pessoa pessoa = daoGeneric.consultar(Pessoa.class, fileDonwloadId);

		HttpServletResponse response = (HttpServletResponse) FacesContext.getCurrentInstance().getExternalContext()
				.getResponse();
		response.addHeader("Content-Disposition", "attachment; filename=download." + pessoa.getExtensao());
		response.setContentType("applicatin/octet-stream");
		response.setContentLength(pessoa.getFotoIconBase64Original().length);
		response.getOutputStream().write(pessoa.getFotoIconBase64Original());
		response.getOutputStream().flush(); // confirmar resposta de dados

		FacesContext.getCurrentInstance().responseComplete();
	}

	public List<SelectItem> getCidades() {
		return cidades;
	}

	public void setCidades(List<SelectItem> cidades) {
		this.cidades = cidades;
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

	public List<SelectItem> getEstados() {

		estados = iDaoPessoa.listaEstados();
		return estados;
	}

	public Part getArquivoFoto() {
		return arquivoFoto;
	}

	public void setArquivoFoto(Part arquivoFoto) {
		this.arquivoFoto = arquivoFoto;
	}

	/* Método para converter inputstream em um array de bytes */
	private byte[] getByte(InputStream is) throws IOException {

		int len;
		int size = 1024;
		byte[] buf = null;

		if (is instanceof ByteArrayInputStream) {
			size = is.available();
			buf = new byte[size];
			len = is.read(buf, 0, size);

		} else {
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			buf = new byte[1024];

			while ((len = is.read(buf, 0, size)) != -1) {
				bos.write(buf, 0, len);
			}

			buf = bos.toByteArray();
		}

		return buf;
	}

}
