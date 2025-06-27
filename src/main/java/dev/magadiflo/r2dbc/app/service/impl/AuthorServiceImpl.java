package dev.magadiflo.r2dbc.app.service.impl;

import dev.magadiflo.r2dbc.app.dto.AuthorRequest;
import dev.magadiflo.r2dbc.app.dto.AuthorResponse;
import dev.magadiflo.r2dbc.app.proyection.AuthorProjection;
import dev.magadiflo.r2dbc.app.repository.AuthorRepository;
import dev.magadiflo.r2dbc.app.service.AuthorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@RequiredArgsConstructor
@Service
public class AuthorServiceImpl implements AuthorService {

    private final AuthorRepository authorRepository;

    @Override
    @Transactional(readOnly = true)
    public Flux<AuthorResponse> findAllAuthors() {
        return null;
    }

    @Override
    @Transactional(readOnly = true)
    public Mono<AuthorProjection> findAuthorById(Integer authorId) {
        return null;
    }

    @Override
    @Transactional(readOnly = true)
    public Mono<Page<AuthorProjection>> getAllAuthorsToPage(String query, int pageNumber, int pageSize) {
        return null;
    }

    @Override
    @Transactional
    public Mono<Integer> saveAuthor(AuthorRequest authorRequest) {
        return null;
    }

    @Override
    @Transactional
    public Mono<AuthorProjection> updateAuthor(Integer authorId, AuthorRequest authorRequest) {
        return null;
    }

    @Override
    @Transactional
    public Mono<Boolean> deleteAuthor(Integer authorId) {
        return null;
    }
}
