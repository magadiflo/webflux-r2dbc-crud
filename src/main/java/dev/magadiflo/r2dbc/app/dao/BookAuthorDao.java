package dev.magadiflo.r2dbc.app.dao;

import dev.magadiflo.r2dbc.app.dto.BookCriteria;
import dev.magadiflo.r2dbc.app.entity.BookAuthor;
import dev.magadiflo.r2dbc.app.proyection.BookProjection;
import org.springframework.data.domain.Pageable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface BookAuthorDao {
    Mono<Long> countBookAuthorByCriteria(BookCriteria bookCriteria);

    Mono<Long> saveBookAuthor(BookAuthor bookAuthor);

    Mono<Void> saveAllBookAuthor(List<BookAuthor> bookAuthorList);

    Mono<Boolean> existBookAuthorByBookId(Integer bookId);

    Mono<Boolean> existBookAuthorByAuthorId(Integer authorId);

    Mono<Void> deleteBookAuthorByBookId(Integer bookId);

    Mono<Void> deleteBookAuthorByAuthorId(Integer authorId);

    Mono<BookProjection> findBookWithTheirAuthorsByBookId(Integer bookId);

    Flux<BookProjection> findAllToPage(BookCriteria bookCriteria, Pageable pageable);
}
