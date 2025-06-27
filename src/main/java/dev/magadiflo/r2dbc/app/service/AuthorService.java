package dev.magadiflo.r2dbc.app.service;

import dev.magadiflo.r2dbc.app.dto.AuthorRequest;
import dev.magadiflo.r2dbc.app.dto.AuthorResponse;
import dev.magadiflo.r2dbc.app.proyection.AuthorProjection;
import org.springframework.data.domain.Page;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface AuthorService {
    Flux<AuthorResponse> findAllAuthors();

    Mono<AuthorProjection> findAuthorById(Integer authorId);

    Mono<Page<AuthorProjection>> getAllAuthorsToPage(String query, int pageNumber, int pageSize);

    Mono<Integer> saveAuthor(AuthorRequest authorRequest);

    Mono<AuthorProjection> updateAuthor(Integer authorId, AuthorRequest authorRequest);

    Mono<Boolean> deleteAuthor(Integer authorId);
}
