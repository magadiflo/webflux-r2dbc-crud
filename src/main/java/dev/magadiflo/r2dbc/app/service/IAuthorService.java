package dev.magadiflo.r2dbc.app.service;

import dev.magadiflo.r2dbc.app.model.dto.AuthorCriteria;
import dev.magadiflo.r2dbc.app.model.dto.AuthorFilter;
import dev.magadiflo.r2dbc.app.model.dto.RegisterAuthorDTO;
import dev.magadiflo.r2dbc.app.model.dto.UpdateAuthorDTO;
import dev.magadiflo.r2dbc.app.model.projection.IAuthorProjection;
import dev.magadiflo.r2dbc.app.persistence.entity.Author;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface IAuthorService {
    Flux<Author> findAll(AuthorCriteria authorCriteria);

    Mono<IAuthorProjection> findAuthorById(Integer authorId);

    Mono<Page<IAuthorProjection>> findAllToPage(AuthorFilter authorFilter, Pageable pageable);

    Mono<Integer> saveAuthor(RegisterAuthorDTO registerAuthorDTO);

    Mono<IAuthorProjection> updateAuthor(Integer authorId, UpdateAuthorDTO updateAuthorDTO);

    Mono<Boolean> deleteAuthor(Integer authorId);
}
