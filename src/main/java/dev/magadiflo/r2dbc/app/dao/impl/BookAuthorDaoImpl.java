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
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

@Slf4j
@RequiredArgsConstructor
@Repository
public class BookAuthorDaoImpl implements BookAuthorDao {

    private final DatabaseClient databaseClient;
    private static final String BOOK_ID = "bookId";
    private static final String AUTHOR_ID = "authorId";

    @Override
    public Mono<Long> countBookAuthorByCriteria(BookCriteria bookCriteria) {
        return null;
    }

    @Override
    public Mono<Long> saveBookAuthor(BookAuthor bookAuthor) {
        return this.rowsUpdatedAfterInsert(bookAuthor);
    }

    @Override
    public Mono<Void> saveAllBookAuthor(List<BookAuthor> bookAuthorList) {
        return Flux.fromIterable(bookAuthorList)
                .flatMap(this::rowsUpdatedAfterInsert)
                .then();
    }

    @Override
    public Mono<Boolean> existBookAuthorByBookId(Integer bookId) {
        String sql = """
                SELECT EXISTS(
                    SELECT 1
                    FROM book_authors AS ba
                    WHERE ba.book_id = :bookId
                ) AS result
                """;
        return this.databaseClient
                .sql(sql)
                .bind(BOOK_ID, bookId)
                .map((row, rowMetadata) -> row.get("result", Boolean.class))
                .one();
    }

    @Override
    public Mono<Boolean> existBookAuthorByAuthorId(Integer authorId) {
        String sql = """
                SELECT EXISTS(
                    SELECT 1
                    FROM book_authors AS ba
                    WHERE ba.author_id = :authorId
                ) AS result
                """;
        return this.databaseClient
                .sql(sql)
                .bind(AUTHOR_ID, authorId)
                .map((row, rowMetadata) -> row.get("result", Boolean.class))
                .one();
    }

    @Override
    public Mono<Void> deleteBookAuthorByBookId(Integer bookId) {
        String sql = """
                DELETE FROM book_authors
                WHERE book_id = :bookId
                """;
        return this.databaseClient
                .sql(sql)
                .bind(BOOK_ID, bookId)
                .then();
    }

    @Override
    public Mono<Void> deleteBookAuthorByAuthorId(Integer authorId) {
        String sql = """
                DELETE FROM book_authors
                WHERE author_id = :authorId
                """;
        return this.databaseClient
                .sql(sql)
                .bind(AUTHOR_ID, authorId)
                .then();
    }

    @Override
    public Mono<BookProjection> findBookWithTheirAuthorsByBookId(Integer bookId) {
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
                .bind(BOOK_ID, bookId)
                .map(this.mappingBookProjection())
                .one();
    }

    @Override
    public Flux<BookProjection> findAllToPage(BookCriteria bookCriteria, Pageable pageable) {
        String sql = this.buildSql(bookCriteria);
        DatabaseClient.GenericExecuteSpec querySpec = this.databaseClient.sql(sql);

        if (bookCriteria.hasQuery()) {
            String likePattern = "%" + bookCriteria.query().trim() + "%";
            querySpec = querySpec.bind("query", likePattern);
        }

        if (bookCriteria.hasPublicationDate()) {
            querySpec = querySpec.bind("publicationDate", bookCriteria.publicationDate());
        }

        querySpec = querySpec
                .bind("limit", pageable.getPageSize())
                .bind("offset", pageable.getOffset());

        return querySpec
                .map(this.mappingBookProjection())
                .all();
    }

    private BiFunction<Row, RowMetadata, BookProjection> mappingBookProjection() {
        return (row, rowMetadata) -> new BookProjection(
                row.get("title", String.class),
                row.get("publication_date", LocalDate.class),
                row.get("online_availability", Boolean.class),
                row.get("concat_authors", String.class)
        );
    }

    private Mono<Long> rowsUpdatedAfterInsert(BookAuthor bookAuthor) {
        String sql = """
                INSERT INTO book_authors(book_id, author_id)
                VALUES(:bookId, :authorId)
                """;
        return this.databaseClient
                .sql(sql)
                .bind(BOOK_ID, bookAuthor.getBookId())
                .bind(AUTHOR_ID, bookAuthor.getAuthorId())
                .fetch()
                .rowsUpdated();
    }

    private String buildSql(BookCriteria bookCriteria) {
        List<String> conditions = new ArrayList<>();
        StringBuilder sql = new StringBuilder("""
                SELECT b.title,
                        b.publication_date,
                        b.online_availability,
                        STRING_AGG(a.first_name||' '||a.last_name, ', ') as concat_authors
                FROM books AS b
                    LEFT JOIN book_authors AS ba ON(b.id = ba.book_id)
                    LEFT JOIN authors AS a ON(ba.author_id = a.id)
                """);

        if (bookCriteria.hasQuery()) {
            conditions.add("""
                    (
                        LOWER(b.title) LIKE LOWER(:query)
                        OR LOWER(a.first_name) LIKE LOWER(:query)
                        OR LOWER(a.last_name) LIKE LOWER(:query)
                    )
                    """);
        }

        if (bookCriteria.hasPublicationDate()) {
            conditions.add("b.publication_date = :publicationDate");
        }

        if (!conditions.isEmpty()) {
            sql.append("WHERE ")
                    .append(String.join(" AND ", conditions))
                    .append("\n");
        }

        sql.append("""
                GROUP BY b.title,
                        b.publication_date,
                        b.online_availability
                ORDER BY b.title
                LIMIT :limit
                OFFSET :offset
                """);
        return sql.toString();
    }
}
