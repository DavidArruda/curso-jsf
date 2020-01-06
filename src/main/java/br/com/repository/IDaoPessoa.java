package br.com.repository;

import br.com.entidades.Pessoa;

public interface IDaoPessoa {

	Pessoa consultarPessoa(String login, String senha);
}
