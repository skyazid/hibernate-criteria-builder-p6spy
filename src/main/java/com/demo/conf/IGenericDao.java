package com.demo.conf;

import javax.persistence.criteria.Order;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * Interface utilisant les types paramétrés afin d'abstraire les méthodes CRUD
 * (Create,Read,Update,Delete). Les DAO étendront donc cette interface en la
 * paramétrant
 *
 * @param <PK>
 *            : la clé primaire
 * @param <ENTITY>
 *            : l'entité
 *
 */
public interface IGenericDao<ENTITY, PK extends Serializable> {

	/**
	 * Méthode de création d'un enregistrement en base de données.
	 *
	 * @param newInstance
	 *            de type T paramétré
	 */
	void create(ENTITY newInstance);

	/**
	 * Méthode de mise à jour d'un objet (paramétré).
	 *
	 * @param transientObject
	 *            représente le bean à mettre à jour
	 * @return the managed entity
	 */
	ENTITY update(ENTITY transientObject);

	/**
	 * Méthode de suppression d'un enregistrement en base de données.
	 *
	 * @param persistentObject
	 *            qui est l'objet à supprimer (un bean)
	 */
	void delete(ENTITY persistentObject);

	/**
	 * Suppression d'un objet par son identifiant.
	 *
	 * @param identifiant
	 *            Identifiant de l'objet à supprimer
	 */
	void deleteById(PK identifiant);

	/**
	 * Méthode de lecture d'un enregistrement en base de données.
	 *
	 * @param id
	 *            qui est la clé primaire pour chercher l'enregistrement
	 * @return T qui représente un enregistrement (un bean)
	 */
	ENTITY read(PK id);

	/**
	 * Compte le nombre d'objets.
	 *
	 * @return le nombre d'objets
	 */
	long count();

	/**
	 * méthode qui retourne tous les résultats.
	 *
	 * @return List des résultats.
	 */
	List<ENTITY> readAll();

	/**
	 * méthode qui retourne tous les résultats ordonnées.
	 * 
	 * @param orders
	 *            les order by
	 *
	 * @return List des résultats.
	 */
	List<ENTITY> readAll(Order... orders);

	/**
	 * trouver toutes les entitys qui correspondent avec les attributes.
	 * 
	 * @param attributes
	 *            : les attributes key = nom attribut, value = valeur
	 * @return <code>List</code>
	 */
	List<ENTITY> findAllByAttributes(Map<String, Object> attributes);

	/**
	 * retourne les entity qui correspondent à l'example.
	 *
	 * @param example
	 *            : l'example.
	 * @return <code>List</code>
	 */
	List<ENTITY> findAllByBeanCritere(IBeanAsSearchCriteres<ENTITY> example);

	/**
	 * detache l'entity de la session hibernate.
	 *
	 * @param entity
	 *            : entity to be detached
	 */
	void detachEntity(ENTITY entity);

	/**
	 * delete all entities.
	 *
	 * @return nb elements supprimés
	 */
	int deleteAll();

}
