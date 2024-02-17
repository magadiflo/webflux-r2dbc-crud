package dev.magadiflo.r2dbc.app.persistence.dao;

import dev.magadiflo.r2dbc.app.model.dto.AuthorCriteria;
import dev.magadiflo.r2dbc.app.persistence.entity.Author;
import reactor.core.publisher.Flux;

public interface IAuthorDao {
    Flux<Author> findAuthorByCriteria(AuthorCriteria authorCriteria);
}
