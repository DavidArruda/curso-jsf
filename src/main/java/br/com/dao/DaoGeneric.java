package br.com.dao;

import java.io.Serializable;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import br.com.jpautil.JPAUtil;

@Named
public class DaoGeneric<E> implements Serializable{

	private static final long serialVersionUID = 1L;
	@Inject
	private EntityManager entityManager;
	@Inject
	private JPAUtil jpaUtil;

	public void salvar(E entidade) {

		EntityTransaction transaction = entityManager.getTransaction();
		transaction.begin();

		entityManager.persist(entidade);

		transaction.commit();

	}

	public E merge(E entidade) {

		EntityTransaction transaction = entityManager.getTransaction();
		transaction.begin();

		E retorno = entityManager.merge(entidade);

		transaction.commit();

		return retorno;
	}
	
	public void delete(E entidade) {
		
		EntityTransaction transaction = entityManager.getTransaction();
		transaction.begin();
		
		entityManager.remove(entidade);
		
		transaction.commit();
	}
	
	public void deletePorId(E entidade) {
		EntityTransaction transaction = entityManager.getTransaction();
		transaction.begin();
		
		Object id = jpaUtil.getPrimaryKey(entidade);
		
		entityManager.createQuery("delete from " + entidade.getClass().getName() + " where id = " + id).executeUpdate();
		
		transaction.commit();
	}
	
	@SuppressWarnings("unchecked")
	public List<E> listEntity(Class<E> entidade) {
		EntityTransaction transaction = entityManager.getTransaction();
		transaction.begin();
		
		List<E> retorno = entityManager.createQuery("from " + entidade.getSimpleName()).getResultList();
		
		transaction.commit();
		
		return retorno;
	}
	
	public E consultar(Class<E> entidade, String codigo) {
		EntityTransaction transaction = entityManager.getTransaction();
		transaction.begin();
		
		E objeto = (E) entityManager.find(entidade, Long.parseLong(codigo));
		
		transaction.commit();
		
		return objeto;
	}
	
}
