package br.com.dao;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import br.com.jpautil.JPAUtil;

public class DaoGeneric<E> {

	public void salvar(E entidade) {
		EntityManager entityManager = JPAUtil.getEntityManager();

		EntityTransaction transaction = entityManager.getTransaction();
		transaction.begin();

		entityManager.persist(entidade);

		transaction.commit();

		entityManager.close();
	}

	public E merge(E entidade) {
		EntityManager entityManager = JPAUtil.getEntityManager();

		EntityTransaction transaction = entityManager.getTransaction();
		transaction.begin();

		E retorno = entityManager.merge(entidade);

		transaction.commit();

		entityManager.close();

		return retorno;
	}
	
	public void delete(E entidade) {
		EntityManager entityManager = JPAUtil.getEntityManager();
		
		EntityTransaction transaction = entityManager.getTransaction();
		transaction.begin();
		
		entityManager.remove(entidade);
		
		transaction.commit();
		
		entityManager.close();
	}
	
	public void deletePorId(E entidade) {
		EntityManager entityManager = JPAUtil.getEntityManager();
		
		EntityTransaction transaction = entityManager.getTransaction();
		transaction.begin();
		
		Object id = JPAUtil.getPrimaryKey(entidade);
		
		entityManager.createQuery("delete from " + entidade.getClass().getName() + " where id = " + id).executeUpdate();
		
		transaction.commit();
		
		entityManager.close();
	}
	
	@SuppressWarnings("unchecked")
	public List<E> listEntity(Class<E> entidade) {
		EntityManager entityManager = JPAUtil.getEntityManager();
		
		EntityTransaction transaction = entityManager.getTransaction();
		transaction.begin();
		
		List<E> retorno = entityManager.createQuery("from " + entidade.getSimpleName()).getResultList();
		
		transaction.commit();
		
		entityManager.close();
		
		return retorno;
	}
	
}
