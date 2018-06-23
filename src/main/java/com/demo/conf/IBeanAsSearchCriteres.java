package com.demo.conf;



import java.util.Map;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

/**
 * <code>IBeanAsSearchCritere</code> permet d'utiliser un Bean comme critère de recherche.
 * @param <ENTITY> : l'entity correspondante
 * 
 */
public interface IBeanAsSearchCriteres<ENTITY> {

    /**
     * Transforme le Bean critère en <code>java.util.Map</code>.
     * 
     * @return <code>java.util.Map</code> contenant tous les critères
     */
    Map<String, Object> transformCritereAsMap();
   
    /**
     * transform l'exemple en predicate.
     * @param cb : le criteria builder
     * @param root : le root
     * @return <code>Predicate</code>
     */
    Predicate toPredicate(CriteriaBuilder cb, Root<ENTITY> root);
}
