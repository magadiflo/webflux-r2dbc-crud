package dev.magadiflo.r2dbc.app.dao.impl;

import dev.magadiflo.r2dbc.app.dao.AuthorDao;
import dev.magadiflo.r2dbc.app.dto.AuthorCriteria;
import dev.magadiflo.r2dbc.app.entity.Author;
import lombok.RequiredArgsConstructor;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.data.relational.core.query.Query;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

import java.util.Objects;

@RequiredArgsConstructor
@Repository
public class AuthorDaoImpl implements AuthorDao {

    private final R2dbcEntityTemplate template;

    @Override
    public Flux<Author> findAuthorByCriteria(AuthorCriteria authorCriteria) {
        Criteria criteria = Criteria.empty();

        if (Objects.nonNull(authorCriteria.firstName()) && !authorCriteria.firstName().trim().isEmpty()) {
            Criteria likeFirstName = Criteria.where("first_name").like(this.likePattern(authorCriteria.firstName()));
            criteria = criteria.and(likeFirstName);
        }

        if (Objects.nonNull(authorCriteria.lastName()) && !authorCriteria.lastName().trim().isEmpty()) {
            Criteria likeLastName = Criteria.where("last_name").like(this.likePattern(authorCriteria.lastName()));
            criteria = criteria.and(likeLastName);
        }

        Query query = Query.query(criteria);

        return this.template
                .select(Author.class)
                .matching(query)
                .all();
    }

    private String likePattern(String value) {
        return "%" + value.trim() + "%";
    }
}
