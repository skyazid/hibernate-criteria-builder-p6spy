package com.demo.dao;

import com.demo.conf.IGenericDao;
import com.demo.entities.Person;

import java.util.List;

public interface IPersonDao extends IGenericDao<Person, Integer> {

    List<Person> findByName(String name);

}
