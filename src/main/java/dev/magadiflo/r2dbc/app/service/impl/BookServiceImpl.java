package dev.magadiflo.r2dbc.app.service.impl;

import dev.magadiflo.r2dbc.app.dao.BookAuthorDao;
import dev.magadiflo.r2dbc.app.dto.BookCriteria;
import dev.magadiflo.r2dbc.app.dto.BookRequest;
import dev.magadiflo.r2dbc.app.dto.BookResponse;
import dev.magadiflo.r2dbc.app.dto.BookUpdateRequest;
import dev.magadiflo.r2dbc.app.entity.BookAuthor;
import dev.magadiflo.r2dbc.app.exception.ApplicationExceptions;
import dev.magadiflo.r2dbc.app.mapper.BookMapper;
import dev.magadiflo.r2dbc.app.proyection.BookProjection;
import dev.magadiflo.r2dbc.app.repository.AuthorRepository;
import dev.magadiflo.r2dbc.app.repository.BookRepository;
import dev.magadiflo.r2dbc.app.service.BookService;
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

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

@Slf4j
@RequiredArgsConstructor
@Service
public class BookServiceImpl implements BookService {

    private final BookRepository bookRepository;
    private final AuthorRepository authorRepository;
    private final BookAuthorDao bookAuthorDao;
    private final BookMapper bookMapper;

    @Override
    @Transactional(readOnly = true)
    public Flux<BookResponse> findAllBooks() {
        return this.bookRepository.findAll()
                .map(this.bookMapper::toBookResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Mono<BookProjection> findBookById(Integer bookId) {
        return this.bookAuthorDao.findBookWithTheirAuthorsByBookId(bookId)
                .switchIfEmpty(ApplicationExceptions.bookNotFound(bookId));
    }

    @Override
    @Transactional(readOnly = true)
    public Mono<Page<BookProjection>> findBooksWithAuthorsByCriteria(String query, LocalDate publicationDate, int pageNumber, int pageSize) {
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        BookCriteria bookCriteria = new BookCriteria(query, publicationDate);
        return Mono.zip(
                this.bookAuthorDao.findAllToPage(bookCriteria, pageable).collectList(),
                this.bookAuthorDao.countBookAuthorByCriteria(bookCriteria),
                (data, total) -> new PageImpl<>(data, pageable, total)
        );
    }

    @Override
    @Transactional
    public Mono<BookProjection> saveBook(BookRequest bookRequest) {
        return this.validateAuthors(bookRequest)
                .then(Mono.fromSupplier(() -> this.bookMapper.toBook(bookRequest)))
                .flatMap(this.bookRepository::save)
                .flatMap(savedBook -> {
                    if (bookRequest.hasNoAuthorIds()) {
                        return Mono.just(savedBook);
                    }
                    List<BookAuthor> relations = this.bookAuthorList(bookRequest.authorIds(), savedBook.getId());
                    return this.bookAuthorDao.saveAllBookAuthor(relations)
                            .thenReturn(savedBook);
                })
                .flatMap(savedBook -> this.bookAuthorDao.findBookWithTheirAuthorsByBookId(savedBook.getId()));
    }

    @Override
    @Transactional
    public Mono<BookProjection> updateBook(Integer bookId, BookUpdateRequest bookUpdateRequest) {
        return this.bookRepository.findById(bookId)
                .map(book -> this.bookMapper.toBookUpdate(book, bookUpdateRequest))
                .flatMap(this.bookRepository::save)
                .flatMap(savedBook -> this.bookAuthorDao.findBookWithTheirAuthorsByBookId(bookId))
                .switchIfEmpty(ApplicationExceptions.bookNotFound(bookId));
    }

    @Override
    @Transactional
    public Mono<BookProjection> updateBookAuthors(Integer bookId, List<Integer> authorIds) {
        return this.bookRepository.findById(bookId)
                .switchIfEmpty(ApplicationExceptions.bookNotFound(bookId))
                .flatMap(book -> {
                    if (Objects.isNull(authorIds) || authorIds.isEmpty()) {
                        return this.bookAuthorDao.deleteBookAuthorByBookId(bookId);
                    }
                    return this.validateAuthors(authorIds)
                            .then(this.bookAuthorDao.deleteBookAuthorByBookId(bookId))
                            .then(Mono.fromSupplier(() -> this.bookAuthorList(authorIds, bookId)))
                            .flatMap(this.bookAuthorDao::saveAllBookAuthor);
                })
                .then(this.bookAuthorDao.findBookWithTheirAuthorsByBookId(bookId));
    }

    @Override
    @Transactional
    public Mono<Void> deleteBook(Integer bookId) {
        return this.bookRepository.findById(bookId)
                .switchIfEmpty(ApplicationExceptions.bookNotFound(bookId))
                .flatMap(book -> this.bookAuthorDao.existBookAuthorByBookId(bookId))
                .flatMap(hasAuthors -> Boolean.TRUE.equals(hasAuthors) ? this.bookAuthorDao.deleteBookAuthorByBookId(bookId) : Mono.empty())
                .then(this.bookRepository.deleteById(bookId));
    }

    // Mono.fromSupplier(...): internamente debes retornar un valor simple (un objeto). El resultado es un Mono que emite ese valor al suscribirse.
    // Mono.defer(...): internamente debes retornar un Mono. El resultado es exactamente ese Mono retornado (no lo crea hasta que se suscriba).
    private Mono<Void> validateAuthors(BookRequest bookRequest) {
        return Mono.defer(() -> {
            if (bookRequest.hasNoAuthorIds()) {
                return Mono.empty();
            }
            return this.allAuthorIdsExist(bookRequest.authorIds());
        });
    }

    private Mono<Void> validateAuthors(List<Integer> authorIds) {
        return Mono.defer(() -> this.allAuthorIdsExist(authorIds));
    }

    private Mono<Void> allAuthorIdsExist(List<Integer> authorIds) {
        return this.authorRepository.findAllAuthorsByIdIn(authorIds)
                .collectList()
                .flatMap(authors -> {
                    if (authorIds.size() != authors.size()) {
                        return ApplicationExceptions.authorIdsNotFound();
                    }
                    return Mono.empty();
                });
    }

    private List<BookAuthor> bookAuthorList(List<Integer> authorIds, Integer bookId) {
        return authorIds.stream()
                .map(authorId -> BookAuthor.builder()
                        .bookId(bookId)
                        .authorId(authorId)
                        .build()
                )
                .toList();
    }
}
