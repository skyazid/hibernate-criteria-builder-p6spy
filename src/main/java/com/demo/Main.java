package com.demo;

import com.demo.dao.IPersonDao;
import com.demo.dao.impl.PersonDaoImpl;
import com.demo.entities.Person;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

public class Main {

    /**
     * The entity manager factory.
     */
    private static EntityManagerFactory entityManagerFactory = null;

    /**
     * Open entity manager.
     *
     * @return the entity manager
     */
    private static EntityManager openEntityManager() {
        if (entityManagerFactory == null) {
            entityManagerFactory = Persistence.createEntityManagerFactory("persistence-unit-p6spy");
        }
        return entityManagerFactory.createEntityManager();
    }

    public static void main(String[] args) {
        EntityManager entityManager = openEntityManager();
        IPersonDao personDao = new PersonDaoImpl();
        ((PersonDaoImpl) personDao).setEntityManager(entityManager);

        entityManager.getTransaction().begin();

        Person alice = new Person();
        alice.setName("alice");
        personDao.create(alice);

        Person bob = new Person();
        bob.setName("bob");
        personDao.create(bob);

        entityManager.getTransaction().commit();

        bob = personDao.findByName("bob").get(0);
        System.out.println(bob);

        entityManager.close();
        System.exit(1);
    }

}
