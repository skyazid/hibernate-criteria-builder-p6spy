package com.demo.dao.impl;

import com.demo.conf.AbstractGenericDaoImpl;
import com.demo.dao.IPersonDao;
import com.demo.entities.Person;
import com.demo.entities.Person_;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.List;

public class PersonDaoImpl extends AbstractGenericDaoImpl<Person, Integer> implements IPersonDao {

    public PersonDaoImpl() {
        super(Person.class);
    }

    @Override
    public List<Person> findByName(String name) {
        final CriteriaBuilder criteriaBuilder = this.getCriteriaBuilder();
        final CriteriaQuery<Person> criteriaQuery = criteriaBuilder.createQuery(this.getEntityClass());
        final Root<Person> demoRoot = criteriaQuery.from(this.getEntityClass());
        criteriaQuery.where(criteriaBuilder.like(demoRoot.get(Person_.name), name));
        return this.findAllByCriteriaQuery(criteriaQuery);
    }

}
