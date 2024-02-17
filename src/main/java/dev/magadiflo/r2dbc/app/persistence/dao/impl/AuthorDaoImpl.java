package dev.magadiflo.r2dbc.app.persistence.dao.impl;

import dev.magadiflo.r2dbc.app.model.dto.AuthorCriteria;
import dev.magadiflo.r2dbc.app.persistence.dao.IAuthorDao;
import dev.magadiflo.r2dbc.app.persistence.entity.Author;
import lombok.AllArgsConstructor;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.data.relational.core.query.Query;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@AllArgsConstructor
@Repository
public class AuthorDaoImpl implements IAuthorDao {
    /**
     * R2dbcEntityTemplate: Su símil sería en JPA como un EntityManager, es como para usar criteria,
     * es decir usamos la clase para hacer las consultas, como vemos en el método inferior,
     * estamos usando la clase Author dentro de ...select(Author.class), no usamos
     * sql nativo.
     */
    private final R2dbcEntityTemplate r2dbcEntityTemplate;

    @Override
    public Flux<Author> findAuthorByCriteria(AuthorCriteria authorCriteria) {
        Criteria criteria = Criteria.empty();

        if (authorCriteria.firstName() != null) {
            criteria = criteria.and(Criteria.where("firstName").like("%" + authorCriteria.firstName() + "%"));
        }

        if (authorCriteria.lastName() != null) {
            criteria = criteria.and(Criteria.where("lastName").like("%" + authorCriteria.lastName() + "%"));
        }

        Query query = Query.query(criteria);

        return this.r2dbcEntityTemplate
                .select(Author.class)
                .matching(query)
                .all();
    }
}
