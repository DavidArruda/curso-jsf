package br.com.filter;

import java.io.IOException;

import javax.inject.Inject;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import br.com.entidades.Pessoa;
import br.com.jpautil.JPAUtil;

@WebFilter(urlPatterns = "/*")
public class FilterAutenticacao implements Filter {
	
	@Inject
	private JPAUtil jpaUtil;
	
	@Override
	public void destroy() {

	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {

		HttpServletRequest req = (HttpServletRequest) request;
		HttpSession session = req.getSession();

		Pessoa userLogado = (Pessoa) session.getAttribute("usuarioLogado");
		
		String urlParaAutenticar = req.getServletPath();

		// Retorna null caso não esteja logado
		if (!urlParaAutenticar.equalsIgnoreCase("index.jsf") && userLogado == null) {// Usuário não logado
			RequestDispatcher dispatcher = request.getRequestDispatcher("/index.jsf");
			dispatcher.forward(request, response);
			return;
			
		} else {
			/*
			 * Sempre fazer essa chamada. Executa as ações do resquest e response
			 */
			chain.doFilter(request, response);
		}
	}

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		jpaUtil.getEntityManager();

	}

}
