package dev.magadiflo.r2dbc.app.persistence.dao;

import dev.magadiflo.r2dbc.app.model.dto.BookCriteria;
import dev.magadiflo.r2dbc.app.model.projection.IBookProjection;
import dev.magadiflo.r2dbc.app.persistence.entity.BookAuthor;
import org.springframework.data.domain.Pageable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface IBookAuthorDao {
    Mono<Long> findCountBookAuthorByCriteria(BookCriteria bookCriteria);

    Mono<Long> saveBookAuthor(BookAuthor bookAuthor);

    Mono<Void> saveAllBookAuthor(List<BookAuthor> bookAuthor);

    Mono<IBookProjection> findByBookId(Integer bookId);

    Mono<Boolean> existBookAuthorByBookId(Integer bookId);

    Mono<Boolean> existBookAuthorByAuthorId(Integer authorId);

    Mono<IBookProjection> findAllBookAuthorByBookId(Integer bookId);

    Mono<Void> deleteBookAuthorByBookId(Integer bookId);

    Mono<Void> deleteBookAuthorByAuthorId(Integer authorId);

    Flux<IBookProjection> findAllToPage(BookCriteria bookCriteria, Pageable pageable);
}
