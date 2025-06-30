package dev.magadiflo.r2dbc.app.service.impl;

import dev.magadiflo.r2dbc.app.dto.AuthorRequest;
import dev.magadiflo.r2dbc.app.dto.AuthorResponse;
import dev.magadiflo.r2dbc.app.exception.ApplicationExceptions;
import dev.magadiflo.r2dbc.app.mapper.AuthorMapper;
import dev.magadiflo.r2dbc.app.proyection.AuthorProjection;
import dev.magadiflo.r2dbc.app.repository.AuthorRepository;
import dev.magadiflo.r2dbc.app.service.AuthorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@RequiredArgsConstructor
@Service
public class AuthorServiceImpl implements AuthorService {

    private final AuthorRepository authorRepository;
    private final AuthorMapper authorMapper;

    @Override
    @Transactional(readOnly = true)
    public Flux<AuthorResponse> findAllAuthors() {
        return this.authorRepository.findAll()
                .map(this.authorMapper::toAuthorResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Mono<AuthorProjection> findAuthorById(Integer authorId) {
        return this.authorRepository.findAuthorById(authorId)
                .switchIfEmpty(ApplicationExceptions.authorNotFound(authorId));
    }

    @Override
    @Transactional(readOnly = true)
    public Mono<Page<AuthorProjection>> getAllAuthorsToPage(String query, int pageNumber, int pageSize) {
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        return Mono.zip(
                this.authorRepository.findByQuery(query, pageable).collectList(),
                this.authorRepository.findCountByQuery(query),
                (data, total) -> new PageImpl<>(data, pageable, total)
        );
    }

    @Override
    @Transactional
    public Mono<Integer> saveAuthor(AuthorRequest authorRequest) {
        return Mono.fromSupplier(() -> this.authorMapper.toAuthor(authorRequest))
                .flatMap(this.authorRepository::saveAuthor);
    }

    @Override
    @Transactional
    public Mono<AuthorProjection> updateAuthor(Integer authorId, AuthorRequest authorRequest) {
        return this.authorRepository.findById(authorId)
                .map(author -> this.authorMapper.toAuthor(authorRequest))
                .doOnNext(author -> author.setId(authorId))
                .flatMap(this.authorRepository::updateAuthor)
                .flatMap(affectedRows -> this.authorRepository.findAuthorById(authorId))
                .switchIfEmpty(ApplicationExceptions.authorNotFound(authorId));
    }

    @Override
    @Transactional
    public Mono<Boolean> deleteAuthor(Integer authorId) {
        return this.authorRepository.findById(authorId)
                .flatMap(author -> this.authorRepository.deleteAuthorById(authorId))
                .switchIfEmpty(ApplicationExceptions.authorNotFound(authorId));
    }
}
