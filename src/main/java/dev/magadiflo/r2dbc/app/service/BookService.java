package dev.magadiflo.r2dbc.app.service;

import dev.magadiflo.r2dbc.app.dto.BookRequest;
import dev.magadiflo.r2dbc.app.dto.BookResponse;
import dev.magadiflo.r2dbc.app.proyection.BookProjection;
import org.springframework.data.domain.Page;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;

public interface BookService {
    Flux<BookResponse> findAllBooks();

    Mono<BookProjection> findBookById(Integer bookId);

    Mono<Page<BookProjection>> findBooksWithAuthorsByCriteria(String query, LocalDate publicationDate, int pageNumber, int pageSize);

    Mono<BookProjection> saveBook(BookRequest bookRequest);

    Mono<BookProjection> updateBook(Integer bookId, BookRequest bookRequest);

    Mono<Void> deleteBook(Integer bookId);
}
