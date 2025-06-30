package dev.magadiflo.r2dbc.app.dao;

import dev.magadiflo.r2dbc.app.dto.AuthorCriteria;
import dev.magadiflo.r2dbc.app.entity.Author;
import reactor.core.publisher.Flux;

public interface AuthorDao {
    Flux<Author> findAuthorByCriteria(AuthorCriteria authorCriteria);
}
