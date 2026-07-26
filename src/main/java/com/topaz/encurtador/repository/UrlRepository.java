package com.topaz.encurtador.repository;

import com.topaz.encurtador.model.ShortUrl;

import javax.enterprise.context.ApplicationScoped;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import java.util.Optional;

@ApplicationScoped
public class UrlRepository {

    @PersistenceContext(unitName = "encurtadorPU")
    private EntityManager entityManager;

    public ShortUrl save(ShortUrl shortUrl) {
        entityManager.persist(shortUrl);
        return shortUrl;
    }

    public Optional<ShortUrl> findByShortCode(String shortCode) {

        TypedQuery<ShortUrl> query = entityManager.createQuery(
                "SELECT s FROM ShortUrl s WHERE s.shortCode = :shortCode",
                ShortUrl.class);

        query.setParameter("shortCode", shortCode);

        return query.getResultList().stream().findFirst();
    }

    public Optional<ShortUrl> findByOriginalUrl(String originalUrl) {

        TypedQuery<ShortUrl> query = entityManager.createQuery(
                "SELECT s FROM ShortUrl s WHERE s.originalUrl = :originalUrl",
                ShortUrl.class);

        query.setParameter("originalUrl", originalUrl);

        return query.getResultList().stream().findFirst();
    }

    public boolean existsByShortCode(String shortCode) {
        return findByShortCode(shortCode).isPresent();
    }
}