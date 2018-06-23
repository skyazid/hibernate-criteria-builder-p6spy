package com.demo.dao.impl;

import com.demo.conf.IBeanAsSearchCriteres;
import com.demo.dao.IGenericDao;
import com.demo.conf.exception.BeanTechnicalException;
import com.demo.conf.exception.DataAccessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.*;
import javax.persistence.criteria.*;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 *
 * @param <ENTITY>
 *            l'entité du type du DAO
 * @param <PK>
 *            la clé primaire de l'entite
 */
public abstract class AbstractGenericDaoImpl<ENTITY, PK extends Serializable> implements IGenericDao<ENTITY, PK> {

	/**
	 * le caractere wildcard pour JPA.
	 */
	protected static final String JPA_WILDCARD = "%";

	/**
	 * Le logger.
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(AbstractGenericDaoImpl.class.getName());

	/**
	 * La session factory d'hibernate.
	 */
	private EntityManager em;

	/**
	 * la class sur laquelle sont effectuée les requétes.
	 */
	private Class<ENTITY> entityClass;

	/**
	 * Constructeur.
	 *
	 * @param entityClass
	 *            : la classe représentante
	 */
	public AbstractGenericDaoImpl(final Class<ENTITY> entityClass) {
		this.entityClass = entityClass;
	}

	/**
	 * Constructeur par défaut.
	 *
	 */

	public AbstractGenericDaoImpl() {
		this.entityClass = null;
	}

	@Override
	public final void create(final ENTITY newInstance) {
		try {
			nullifyEmptyStrings(newInstance);
			this.em.persist(newInstance);
		} catch (final DataAccessException | BeanTechnicalException ex) {
			LOGGER.error(ex.getMessage(), ex.fillInStackTrace());
			throw ex;
		}
	}

	@Override
	public final ENTITY update(final ENTITY transientObject) {
		try {
			nullifyEmptyStrings(transientObject);
			return this.em.merge(transientObject);
		} catch (final DataAccessException | BeanTechnicalException ex) {
			LOGGER.error(ex.getMessage(), ex.fillInStackTrace());
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug(ex.getMessage(), ex);
			}
			throw ex;
		}

	}

	@Override
	public final void delete(final ENTITY persistentObject) {
		try {
			this.em.remove(this.em.contains(persistentObject) ? persistentObject : this.em.merge(persistentObject));
		} catch (final DataAccessException ex) {
			LOGGER.error(ex.getMessage(), ex.fillInStackTrace());
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug(ex.getMessage(), ex);
			}
			throw ex;
		}
	}

	@Override
	public final void deleteById(final PK identifiant) {
		try {
			final ENTITY obj = this.read(identifiant);
			this.delete(obj);
		} catch (final DataAccessException ex) {
			LOGGER.error(ex.getMessage(), ex.fillInStackTrace());
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug(ex.getMessage(), ex);
			}
			throw ex;
		}
	}

	@Override
	public final ENTITY read(final PK id) {
		if (id == null) {
			throw new IllegalArgumentException("L'identifiant de PK est NULL pour le read");
		}
		try {
			return this.em.find(this.entityClass, id);
		} catch (final DataAccessException ex) {
			LOGGER.error(ex.getMessage(), ex.fillInStackTrace());
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug(ex.getMessage(), ex);
			}
			throw ex;
		}
	}

	@Override
	public final long count() {
		final CriteriaBuilder cb = this.em.getCriteriaBuilder();
		final CriteriaQuery<Long> cq = cb.createQuery(Long.class);
		final Root<ENTITY> c = cq.from(this.entityClass);
		cq.select(cb.count(c));
		return this.em.createQuery(cq).getSingleResult();
	}

	@Override
	public List<ENTITY> readAll() {
		final CriteriaBuilder cb = this.em.getCriteriaBuilder();
		final CriteriaQuery<ENTITY> cq = cb.createQuery(this.entityClass);
		final Root<ENTITY> c = cq.from(this.entityClass);
		cq.select(c);
		return this.findAllByCriteriaQuery(cq, null, null);
	}

	@Override
	public int deleteAll() {
		final CriteriaBuilder cb = this.em.getCriteriaBuilder();
		final CriteriaDelete<ENTITY> cd = cb.createCriteriaDelete(this.entityClass);
		@SuppressWarnings("unused")
		final Root<ENTITY> root = cd.from(this.entityClass);
		return this.deleteByCriteria(cd);
	}

	@Override
	public final List<ENTITY> findAllByAttributes(final Map<String, Object> attributes) {
		// set up the Criteria query
		final CriteriaBuilder cb = this.em.getCriteriaBuilder();
		final CriteriaQuery<ENTITY> cq = cb.createQuery(this.getEntityClass());
		final Root<ENTITY> root = cq.from(this.getEntityClass());

		final List<Predicate> predicates = new ArrayList<>();
		for (final String s : attributes.keySet()) {
			if (root.get(s) != null) {
				predicates.add(cb.equal(root.get(s), attributes.get(s)));
			}
		}
		cq.where(predicates.toArray(new Predicate[] {}));

		return this.findAllByCriteriaQuery(cq);
	}

	@Override
	public final List<ENTITY> findAllByBeanCritere(final IBeanAsSearchCriteres<ENTITY> example) {
		final CriteriaBuilder cb = this.getCriteriaBuilder();
		final CriteriaQuery<ENTITY> cq = cb.createQuery(this.entityClass);
		final Root<ENTITY> root = cq.from(this.entityClass);
		cq.where(example.toPredicate(cb, root));
		return this.findAllByCriteriaQuery(cq);
	}

	/**
	 * retourne la liste des entites en fonction de la criteriaQuery.
	 *
	 * @param <C>
	 *            : la classe retour
	 * @param cq
	 *            : la criteriaQuery
	 * @param offset
	 *            : le debut de la page
	 * @param limit
	 *            : le max result par page
	 * @return <code>List</code>
	 */
	protected <C> List<C> findAllByCriteriaQuery(final CriteriaQuery<C> cq, final Integer offset, final Integer limit) {
		final TypedQuery<C> query = this.createQuery(cq);
		if (offset != null && offset >= 0) {
			query.setFirstResult(offset);
		}
		if (limit != null && limit > 0) {
			query.setMaxResults(limit);
		}
		return query.getResultList();
	}

	/**
	 * @param <C>
	 *            : class
	 * @param cq
	 *            : criteria query
	 * @return query
	 *
	 */
	private <C> TypedQuery<C> createQuery(final CriteriaQuery<C> cq) {
		final TypedQuery<C> query = this.em.createQuery(cq);
		return query;
	}

	/**
	 * @param <C>
	 *            : la classe
	 * @param query
	 *            : la query
	 */
	/*
	 * private <C> void addQueryCacheHints(final TypedQuery<C> query) {
	 * query.setHint(QueryHints.CACHEABLE, true);
	 * query.setHint(QueryHints.CACHE_REGION, this.entityClass.getName()); }
	 */

	/**
	 * retourne la liste des entites en fonction de la criteriaQuery.
	 *
	 * @param <C>
	 *            : la classe retour
	 * @param cq
	 *            : la criteriaQuery
	 * @return <code>List</code>
	 */
	protected final <C> List<C> findAllByCriteriaQuery(final CriteriaQuery<C> cq) {
		return this.findAllByCriteriaQuery(cq, null, null);
	}

	/**
	 * retourne une entité unique a partir du critére.
	 *
	 * @param cq
	 *            : les criteres.
	 * @return <code>T</code>
	 */
	protected final ENTITY findUniqueByCriteriaQuery(final CriteriaQuery<ENTITY> cq) {
		try {
			final TypedQuery<ENTITY> query = this.createQuery(cq);
			return query.getSingleResult();
		} catch (final NoResultException ex) {
			LOGGER.debug("Pas d'entité trouvé. Retourne NULL");
			return null;
		}
	}

	/**
	 * retourne une entité unique a partir du critére.
	 *
	 * @param <C>
	 *            c
	 * @param cq
	 *            CriteriaQuery
	 * @return <code>C</code>
	 */
	protected final <C> C findUniqueGenericTypeByCriteriaQuery(final CriteriaQuery<C> cq) {
		try {
			return this.em.createQuery(cq).getSingleResult();
		} catch (final NoResultException e) {
			LOGGER.debug("Pas d'entité trouvé. Retourne NULL");
			return null;
		}
	}

	/**
	 * retourne le criteria builder.
	 *
	 * @return <code>criteriaBuilder</code>
	 */
	protected final CriteriaBuilder getCriteriaBuilder() {
		return this.em.getCriteriaBuilder();
	}



	/**
	 * retourne la named query demandée.
	 *
	 * @param name
	 *            : nom de la requete
	 * @return <code>Query</code>
	 */
	protected final TypedQuery<ENTITY> getNamedQuery(final String name) {
		final TypedQuery<ENTITY> query = this.em.createNamedQuery(name, this.entityClass);
		// this.addQueryCacheHints(query);
		return query;
	}

	/**
	 * gets untyped query (for delete, update).
	 *
	 * @param name
	 *            : name de la query
	 * @return <code>Query</code>
	 */
	protected final Query getNamedUntypedQuery(final String name) {
		return this.em.createNamedQuery(name);
	}

	/**
	 * retourne la named query demandée.
	 *
	 * @param <C>
	 *            : classe de retour
	 *
	 * @param name
	 *            : nom de la requete
	 * @param returnClazz
	 *            : la class de retour
	 * @return <code>Query</code>
	 */
	protected final <C> TypedQuery<C> getNamedQuery(final String name, final Class<C> returnClazz) {
		return this.em.createNamedQuery(name, returnClazz);
	}

	/**
	 * retourne la typed query demandée.
	 *
	 * @param <C>
	 *            : classe de retour
	 *
	 * @param name
	 *            : nom de la requete
	 * @param returnClazz
	 *            : la class de retour
	 * @return <code>Query</code>
	 */
	protected final <C> TypedQuery<C> getTypedQuery(final String name, final Class<C> returnClazz) {
		return this.em.createQuery(name, returnClazz);
	}

	@Override
	public List<ENTITY> readAll(final Order... orders) {
		final CriteriaBuilder cb = this.em.getCriteriaBuilder();
		final CriteriaQuery<ENTITY> cq = cb.createQuery(this.entityClass);
		final Root<ENTITY> c = cq.from(this.entityClass);
		cq.select(c);
		cq.orderBy(orders);
		return this.findAllByCriteriaQuery(cq, null, null);
	}

	/**
	 * méthode qui rajoute les paramtres stocké dans la map é la requete query.
	 *
	 * @param queryAsString
	 *            query As String
	 * @param <C>
	 *            : classe de retour
	 * @param params
	 *            params
	 * @param returnClazz
	 *            classe de retour
	 * @return <code>String</code>
	 */
	protected final <C> TypedQuery<C> getTypedQueryWithParams(final String queryAsString,
					final Map<String, Object> params, final Class<C> returnClazz) {
		final TypedQuery<C> query = this.em.createQuery(queryAsString, returnClazz);
		// this.addQueryCacheHints(query);
		for (final Map.Entry<String, Object> param : params.entrySet()) {
			query.setParameter(param.getKey(), param.getValue());
		}
		return query;
	}

	/**
	 * supprime selon critere. retourne le nombre entity affectées.
	 *
	 * @param cd
	 *            : les criteres
	 * @return <code>int</code>
	 */
	protected final int deleteByCriteria(final CriteriaDelete<ENTITY> cd) {
		final Query q = this.em.createQuery(cd);
		return q.executeUpdate();
	}

	/**
	 * update selon critere. retourne le nombre entity affectees.
	 *
	 * @param cd
	 *            : les criteres
	 * @return <code>int</code>
	 */
	protected final int updateByCriteria(final CriteriaUpdate<ENTITY> cd) {
		final Query q = this.em.createQuery(cd);
		return q.executeUpdate();
	}

	/**
	 * retourne l'entitymanager.
	 *
	 * @return <code>EntityManger</code>
	 */
	protected final EntityManager getEntityManager() {
		return this.em;
	}

	/**
	 * @param entityManager
	 *            the entityManager to set
	 */
	public final void setEntityManager(final EntityManager entityManager) {
		this.em = entityManager;
	}

	/**
	 *
	 * @return la classe de l'entity gere
	 */
	protected final Class<ENTITY> getEntityClass() {
		return this.entityClass;
	}

	@Override
	public final void detachEntity(final ENTITY entity) {
		this.em.detach(entity);
	}

	/**
	 * @param clazz
	 *            set entity reference class.
	 */
	public void setEntityClass(final Class<ENTITY> clazz) {
		this.entityClass = clazz;

	}

    /**
     * nullifies the empty strings of the object.
     *
     * @param o
     *            : the object
     * @throws BeanTechnicalException
     *             exception
     */
    private static void nullifyEmptyStrings(final Object o) throws BeanTechnicalException {
        for (final Field f : o.getClass().getDeclaredFields()) {
            f.setAccessible(true);
            try {
                if (f.getType().equals(String.class) && !Modifier.isStatic(f.getModifiers())
                        && !Modifier.isFinal(f.getModifiers())) {
                    final String value = (String) f.get(o);
                    if (value != null && value.trim().isEmpty()) {
                        f.set(o, null);
                    }
                }
            } catch (final IllegalArgumentException | IllegalAccessException e) {
                throw new BeanTechnicalException(e);
            }
        }
    }

}
