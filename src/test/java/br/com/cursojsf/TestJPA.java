package br.com.cursojsf;


import javax.persistence.Persistence;


public class TestJPA {
	
	public static void main(String[] args) {
		Persistence.createEntityManagerFactory("meuprimeiroprojetojsf");
		//HibernatePersistenceProvider  provider = new HibernatePersistenceProvider();
		//provider.createEntityManagerFactory("meuprimeiroprojetojsf", null);
	}

}
