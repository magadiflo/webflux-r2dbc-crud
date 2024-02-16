package dev.magadiflo.r2dbc.app.service.impl;

import dev.magadiflo.r2dbc.app.exception.ApiException;
import dev.magadiflo.r2dbc.app.model.dto.AuthorCriteria;
import dev.magadiflo.r2dbc.app.model.dto.AuthorFilter;
import dev.magadiflo.r2dbc.app.model.dto.RegisterAuthorDTO;
import dev.magadiflo.r2dbc.app.model.dto.UpdateAuthorDTO;
import dev.magadiflo.r2dbc.app.model.projection.IAuthorProjection;
import dev.magadiflo.r2dbc.app.persistence.entity.Author;
import dev.magadiflo.r2dbc.app.persistence.repository.IAuthorRepository;
import dev.magadiflo.r2dbc.app.persistence.repository.IBookRepository;
import dev.magadiflo.r2dbc.app.service.IAuthorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.MappingException;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
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
    private final IBookRepository bookRepository;
    private final ModelMapper modelMapper;

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
    public Mono<Page<IAuthorProjection>> findAllToPage(AuthorFilter authorFilter, Pageable pageable) {
        return this.authorRepository.findByQ(authorFilter.q(), pageable)
                .collectList()
                .switchIfEmpty(Mono.error(new ApiException("No hay resultados", HttpStatus.NO_CONTENT)))
                .zipWith(this.authorRepository.findCountByQ(authorFilter.q()),
                        (iAuthorProjections, count) -> new PageImpl<>(iAuthorProjections, pageable, count));

    }

    @Override
    @Transactional
    public Mono<Integer> saveAuthor(RegisterAuthorDTO registerAuthorDTO) {
        return Mono.just(registerAuthorDTO)
                .flatMap(dto -> {
                    try {
                        return Mono.just(this.modelMapper.map(dto, Author.class));
                    } catch (MappingException e) {
                        log.error(e.getMessage());
                        return Mono.error(new ApiException("Error al insertar datos", HttpStatus.BAD_REQUEST));
                    }
                })
                .flatMap(this.authorRepository::saveAuthor);
    }

    @Override
    @Transactional
    public Mono<IAuthorProjection> updateAuthor(Integer authorId, UpdateAuthorDTO updateAuthorDTO) {
        return Mono.just(updateAuthorDTO)
                .flatMap(dto -> {
                    try {
                        Author authorEntity = this.modelMapper.map(dto, Author.class);
                        authorEntity.setId(authorId);
                        return Mono.just(authorEntity);
                    } catch (MappingException e) {
                        return Mono.error(new ApiException("Error al actualizar", HttpStatus.BAD_REQUEST));
                    }
                })
                .flatMap(this.authorRepository::updateAuthor)
                .flatMap(this.authorRepository::findByAuthorId);
    }

    @Override
    @Transactional
    public Mono<Void> deleteAuthor(Integer authorId) {
        /*
        return bookRepository.existBookAuthorByAuthorId(authorId)
            .flatMap(existBookAuthor -> {

                if (existBookAuthor) {
                    return bookRepository.deleteBookAuthorByAuthorId(authorId);
                }

                return bookRepository.deleteBookAuthorByAuthorId(authorId)
                        .then(authorRepository.deleteById(authorId));

            });
         */
        return null;
    }
}
