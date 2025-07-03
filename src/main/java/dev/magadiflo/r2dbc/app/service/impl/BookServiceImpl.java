package dev.magadiflo.r2dbc.app.service.impl;

import dev.magadiflo.r2dbc.app.dao.BookAuthorDao;
import dev.magadiflo.r2dbc.app.dto.BookCriteria;
import dev.magadiflo.r2dbc.app.dto.BookRequest;
import dev.magadiflo.r2dbc.app.dto.BookResponse;
import dev.magadiflo.r2dbc.app.exception.ApplicationExceptions;
import dev.magadiflo.r2dbc.app.mapper.BookMapper;
import dev.magadiflo.r2dbc.app.proyection.BookProjection;
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

@Slf4j
@RequiredArgsConstructor
@Service
public class BookServiceImpl implements BookService {

    private final BookRepository bookRepository;
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
        return null;
    }

    @Override
    @Transactional
    public Mono<BookProjection> updateBook(Integer bookId, BookRequest bookRequest) {
        return null;
    }

    @Override
    @Transactional
    public Mono<Void> deleteBook(Integer bookId) {
        return null;
    }
}
