package dev.magadiflo.r2dbc.app.dao.impl;

import dev.magadiflo.r2dbc.app.dao.BookAuthorDao;
import dev.magadiflo.r2dbc.app.dto.BookCriteria;
import dev.magadiflo.r2dbc.app.entity.BookAuthor;
import dev.magadiflo.r2dbc.app.proyection.BookProjection;
import io.r2dbc.spi.Row;
import io.r2dbc.spi.RowMetadata;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.List;
import java.util.function.BiFunction;

@Slf4j
@RequiredArgsConstructor
@Repository
public class BookAuthorDaoImpl implements BookAuthorDao {

    private final DatabaseClient databaseClient;

    @Override
    public Mono<Long> countBookAuthorByCriteria(BookCriteria bookCriteria) {
        return null;
    }

    @Override
    public Mono<Long> saveBookAuthor(BookAuthor bookAuthor) {
        return null;
    }

    @Override
    public Mono<Void> saveAllBookAuthor(List<BookAuthor> bookAuthorList) {
        return null;
    }

    @Override
    public Mono<Boolean> existBookAuthorByBookId(Integer bookId) {
        return null;
    }

    @Override
    public Mono<Boolean> existBookAuthorByAuthorId(Integer authorId) {
        return null;
    }

    @Override
    public Mono<Void> deleteBookAuthorByBookId(Integer bookId) {
        return null;
    }

    @Override
    public Mono<Void> deleteBookAuthorByAuthorId(Integer authorId) {
        return null;
    }

    @Override
    public Mono<BookProjection> findByBookId(Integer bookId) {
        String sql = """
                SELECT b.title,
                        b.publication_date,
                        b.online_availability,
                        STRING_AGG(a.first_name||' '||a.last_name, ', ') as concat_authors
                FROM book_authors AS ba
                    INNER JOIN books AS b ON ba.book_id = b.id
                    INNER JOIN authors AS a ON ba.author_id = a.id
                WHERE b.id = :bookId
                GROUP BY ba.book_id,
                        b.title,
                        b.publication_date,
                        b.online_availability
                """;
        return this.databaseClient
                .sql(sql)
                .bind("bookId", bookId)
                .map(this.mappingBookProjection())
                .one();
    }

    @Override
    public Mono<BookProjection> findAllBookAuthorByBookId(Integer bookId) {
        return null;
    }

    @Override
    public Flux<BookProjection> findAllToPage(BookCriteria bookCriteria, Pageable pageable) {
        return null;
    }

    private BiFunction<Row, RowMetadata, BookProjection> mappingBookProjection() {
        return (row, rowMetadata) -> new BookProjection(
                row.get("title", String.class),
                row.get("publication_date", LocalDate.class),
                row.get("online_availability", Boolean.class),
                row.get("concat_authors", String.class)
        );
    }
}
