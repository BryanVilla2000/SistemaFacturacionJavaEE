package com.fitsystem.util;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

@ApplicationScoped
public class JPAUtil {

    private final EntityManagerFactory emf = Persistence.createEntityManagerFactory("fitsystemPU");

    @Produces
    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }
}
