package dev.magadiflo.r2dbc.app.service.impl;

import dev.magadiflo.r2dbc.app.exception.ApiException;
import dev.magadiflo.r2dbc.app.model.dto.AuthorCriteria;
import dev.magadiflo.r2dbc.app.model.dto.RegisterAuthorDTO;
import dev.magadiflo.r2dbc.app.model.dto.UpdateAuthorDTO;
import dev.magadiflo.r2dbc.app.model.projection.IAuthorProjection;
import dev.magadiflo.r2dbc.app.persistence.dao.IBookAuthorDao;
import dev.magadiflo.r2dbc.app.persistence.entity.Author;
import dev.magadiflo.r2dbc.app.persistence.repository.IAuthorRepository;
import dev.magadiflo.r2dbc.app.service.IAuthorService;
import dev.magadiflo.r2dbc.app.utils.AuthorMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@RequiredArgsConstructor
@Service
public class AuthorServiceImpl implements IAuthorService {

    private final IAuthorRepository authorRepository;
    private final IBookAuthorDao bookAuthorDao;
    private final AuthorMapper authorMapper;

    @Override
    @Transactional(readOnly = true)
    public Flux<Author> findAll(AuthorCriteria authorCriteria) {
        return this.authorRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Mono<IAuthorProjection> findAuthorById(Integer authorId) {
        return this.authorRepository.findByAuthorId(authorId)
                .switchIfEmpty(Mono.error(new ApiException("No hay resultados con authorId: %d".formatted(authorId), HttpStatus.NOT_FOUND)));
    }

    @Override
    @Transactional(readOnly = true)
    public Mono<Page<IAuthorProjection>> findAllToPage(String query, int pageNumber, int pageSize) {
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        Mono<Integer> totalRecords = this.authorRepository.findCountByQuery(query);

        return this.authorRepository.findByQuery(query, pageable)
                .collectList()
                .zipWith(totalRecords, (authorProjections, total) -> new PageImpl<>(authorProjections, pageable, total));
    }

    @Override
    @Transactional
    public Mono<Integer> saveAuthor(RegisterAuthorDTO registerAuthorDTO) {
        return Mono.just(registerAuthorDTO)
                .flatMap(dto -> this.authorMapper.toAuthor(registerAuthorDTO))
                .flatMap(this.authorRepository::saveAuthor)
                .doOnNext(affectedRows -> log.info("Filas afectadas en el insert: {}", affectedRows));
    }

    @Override
    @Transactional
    public Mono<IAuthorProjection> updateAuthor(Integer authorId, UpdateAuthorDTO updateAuthorDTO) {
        return this.authorRepository.findById(authorId)
                .flatMap(authorDB -> this.authorMapper.toAuthor(updateAuthorDTO, authorId))
                .flatMap(this.authorRepository::updateAuthor)
                .doOnNext(affectedRows -> log.info("Filas afectadas en el update: {}", affectedRows))
                .flatMap(affectedRows -> this.authorRepository.findByAuthorId(authorId))
                .switchIfEmpty(Mono.error(new ApiException("No se encontró el author con id %s para actualizar".formatted(authorId), HttpStatus.NOT_FOUND)));
    }

    @Override
    @Transactional
    public Mono<Boolean> deleteAuthor(Integer authorId) {
        return this.authorRepository.findById(authorId)
                .flatMap(authorDB -> this.bookAuthorDao.existBookAuthorByAuthorId(authorId))
                .flatMap(existsBookAuthor -> {
                    log.info("Existe el author en la tabla book_authors?: {}", existsBookAuthor);
                    if (existsBookAuthor) {
                        return this.bookAuthorDao.deleteBookAuthorByAuthorId(authorId).then(Mono.just(true));
                    }
                    return Mono.just(true);
                })
                .flatMap(canContinue -> this.authorRepository.deleteById(authorId).then(Mono.just(true)))
                .switchIfEmpty(Mono.error(new ApiException("No se encontró el author con id %s para eliminar".formatted(authorId), HttpStatus.NOT_FOUND)));
    }
}
