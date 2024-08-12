package dev.magadiflo.r2dbc.app.persistence.dao.impl;

import dev.magadiflo.r2dbc.app.exception.ApiException;
import dev.magadiflo.r2dbc.app.model.dto.BookCriteria;
import dev.magadiflo.r2dbc.app.model.projection.IBookProjection;
import dev.magadiflo.r2dbc.app.model.vo.BookVO;
import dev.magadiflo.r2dbc.app.persistence.dao.IBookAuthorDao;
import dev.magadiflo.r2dbc.app.persistence.entity.BookAuthor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Repository
public class BookAuthorDaoImpl implements IBookAuthorDao {

    /**
     * DatabaseClient, su símil sería jdbcTemplate, aquí usamos SQL nativo para hacer las consultas.
     */
    private final DatabaseClient databaseClient;

    @Override
    public Mono<Long> findCountBookAuthorByCriteria(BookCriteria bookCriteria) {
        String select = "SELECT COUNT(b.id) AS result ";
        String from = "FROM books b ";
        String where = "";

        StringBuilder sqlWhere = new StringBuilder();
        boolean flag = false;

        if (StringUtils.hasText(bookCriteria.q())) {

            if (flag) {
                sqlWhere.append("OR ");
            }

            sqlWhere.append("b.title LIKE :q ");
            flag = true;
        }

        if (bookCriteria.publicationDate() != null) {

            if (flag) {
                sqlWhere.append("OR ");
            }

            sqlWhere.append("b.publicationDate = :publicationDate ");
            flag = true;
        }

        if (flag) {
            where = sqlWhere.insert(0, "WHERE ").toString();
        }

        String sql = select + from + where;
        log.info(sql);

        DatabaseClient.GenericExecuteSpec ges = databaseClient.sql(sql);

        if (StringUtils.hasText(bookCriteria.q())) {
            ges = ges.bind("q", "%" + bookCriteria.q() + "%");
        }

        if (bookCriteria.publicationDate() != null) {
            ges = ges.bind("publicationDate", bookCriteria.publicationDate());
        }

        return ges.map((row, metadata) -> {
                    log.info("count result {}", metadata.getColumnMetadata("result").toString());
                    return row.get("result", Long.class);
                }).first()
                .switchIfEmpty(Mono.error(new ApiException("No record found for book", HttpStatus.NOT_FOUND)));
    }

    @Override
    public Mono<Long> saveBookAuthor(BookAuthor bookAuthor) {
        return this.databaseClient.sql("""
                        INSERT INTO book_authors(book_id, author_id)
                        VALUES(:bookId, :authorId)
                        """)
                .bind("bookId", bookAuthor.getBookId())
                .bind("authorId", bookAuthor.getAuthorId())
                .fetch()
                .rowsUpdated()
                .onErrorMap(error -> new ApiException("Error al insertar en la tabla book_authors" + error.getMessage(), HttpStatus.BAD_REQUEST));
    }

    @Override
    public Mono<Void> saveAllBookAuthor(List<BookAuthor> bookAuthorList) {
        List<Mono<Long>> inserts = bookAuthorList.stream()
                .map(bookAuthorToSave -> this.databaseClient.sql("""
                                INSERT INTO book_authors(book_id, author_id)
                                VALUES(:bookId, :authorId)
                                """)
                        .bind("bookId", bookAuthorToSave.getBookId())
                        .bind("authorId", bookAuthorToSave.getAuthorId())
                        .fetch()
                        .rowsUpdated()
                        .onErrorMap(error -> new ApiException(error.getMessage(), HttpStatus.BAD_REQUEST))
                )
                .toList();

        Flux<Long> concat = Flux.concat(inserts);
        return concat.then();
    }

    @Override
    public Mono<IBookProjection> findByBookId(Integer bookId) {
        String sql = """				
                SELECT ba.book_id as bookId, b.title as title, b.publication_date as publicationDate, b.online_availability as onlineAvailability,
                        STRING_AGG(a.first_name||' '||a.last_name, ', ') as concatAuthors
                FROM book_authors ba
                    INNER JOIN books b ON ba.book_id = b.id
                    INNER JOIN authors a ON ba.author_id = a.id
                WHERE b.id = :bookId
                GROUP BY ba.book_id, b.title, b.publication_date, b.online_availability
                """;

        return databaseClient.sql(sql)
                .bind("bookId", bookId)
                .map((row, metadata) -> {

                    log.info("publicationDate {} ", metadata.getColumnMetadata("publicationDate"));

                    return (IBookProjection) BookVO.builder()
                            .id(row.get("bookId", Integer.class))
                            .title(row.get("title", String.class))
                            //.publicationDate(row.get("publicationDate",LocalDateTime.class) != null ? row.get("publicationDate",LocalDateTime.class).toLocalDate() : null)
                            .publicationDate(row.get("publicationDate", LocalDate.class))
                            .onlineAvailability(row.get("onlineAvailability", Boolean.class))
                            .concatAuthors(row.get("concatAuthors", String.class))
                            .build();
                }).first()
                .switchIfEmpty(Mono.error(new ApiException("No se encontraron registros para el libro con id: " + bookId, HttpStatus.NOT_FOUND)));
    }

    @Override
    public Mono<Boolean> existBookAuthorByBookId(Integer bookId) {
        String sql = """				
                SELECT CASE
                          WHEN COUNT(ba.book_id) > 0 THEN true
                          ELSE false
                       END as result
                FROM book_authors AS ba
                WHERE ba.book_id = :bookId
                """;

        return databaseClient.sql(sql)
                .bind("bookId", bookId)
                .map((row, metadata) -> row.get("result", Boolean.class))
                .first();
    }

    @Override
    public Mono<Boolean> existBookAuthorByAuthorId(Integer authorId) {
        String sql = """				
                SELECT CASE
                            WHEN COUNT(ba.book_id) > 0 THEN true
                            ELSE false
                       END as result
                FROM book_authors AS ba
                WHERE ba.author_id = :authorId
                """;

        return this.databaseClient.sql(sql)
                .bind("authorId", authorId)
                .map((row, metadata) -> row.get("result", Boolean.class))
                .first();
    }

    @Override
    public Flux<IBookProjection> findAllBookAuthorByBookId(Integer bookId) {
        String sql = """				
                SELECT ba.book_id as bookId, b.title as title, b.publication_date as publicationDate, b.online_availability as onlineAvailability,
                        STRING_AGG(a.first_name||' '||a.last_name, ', ') as concatAuthors
                FROM book_authors ba
                    INNER JOIN books b ON ba.book_id = b.id
                    INNER JOIN authors a ON ba.author_id = a.id
                WHERE b.id = :bookId
                GROUP BY ba.book_id, b.title, b.publication_date, b.online_availability
                """;

        return databaseClient.sql(sql)
                .bind("bookId", bookId)
                .map((row, metadata) -> {
                    log.info("publicationDate {} ", metadata.getColumnMetadata("publicationDate"));

                    return (IBookProjection) BookVO.builder()
                            .id(row.get("bookId", Integer.class))
                            .title(row.get("title", String.class))
                            .publicationDate(row.get("publicationDate", LocalDate.class))
                            .onlineAvailability(row.get("onlineAvailability", Boolean.class))
                            .concatAuthors(row.get("concatAuthors", String.class))
                            .build();
                }).all()
                .switchIfEmpty(Mono.error(new ApiException("No record found.", HttpStatus.NOT_FOUND)));
    }

    @Override
    public Mono<Void> deleteBookAuthorByBookId(Integer bookId) {
        String sql = """			
                DELETE FROM book_authors AS ba
                WHERE ba.book_id = :bookId
                """;

        return databaseClient.sql(sql)
                .bind("bookId", bookId)
                .fetch()
                .rowsUpdated()
                .then()
                .onErrorMap(t -> {
                    log.error(t.getMessage());
                    return new ApiException("Error in delete book_authors, bookId " + bookId, HttpStatus.NOT_FOUND);

                });
    }

    @Override
    public Mono<Void> deleteBookAuthorByAuthorId(Integer authorId) {
        String sql = """			
                DELETE FROM book_authors AS ba
                WHERE ba.author_id = :authorId
                """;

        return this.databaseClient.sql(sql)
                .bind("authorId", authorId)
                .fetch()
                .rowsUpdated()
                .then()
                .onErrorMap(t -> {
                    log.error("Ocurrió un error: " + t.getMessage());
                    return new ApiException("Error in delete book_authors, authorId " + authorId, HttpStatus.NOT_FOUND);
                });
    }

    @Override
    public Flux<IBookProjection> findAllToPage(BookCriteria bookCriteria, Pageable pageable) {
        String select = """				
                SELECT b.id as bookId, b.title as title, b.publication_date as publicationDate, b.online_availability as onlineAvailability,
                      STRING_AGG(a.first_name||' '||a.last_name, ', ') as concatAuthors
                """;

        String from = """		
                FROM book_authors ba
                    INNER JOIN books b ON ba.book_id = b.id
                    INNER JOIN authors a ON ba.author_id = a.id
                """;

        String where = "";

        StringBuilder sqlWhere = new StringBuilder();
        boolean flag = false;

        if (StringUtils.hasText(bookCriteria.q())) {

            if (flag) {
                sqlWhere.append("OR ");
            }

            sqlWhere.append("b.title LIKE :q ");
            flag = true;
        }

        if (bookCriteria.publicationDate() != null) {

            if (flag) {
                sqlWhere.append("OR ");
            }

            sqlWhere.append("b.publicationDate = :publicationDate ");
            flag = true;
        }

        if (flag) {
            where = sqlWhere.insert(0, "WHERE ").toString();
        }

        String limit = """
                GROUP BY b.id
                ORDER BY b.id ASC
                LIMIT :pageSize OFFSET :offset
                """;

        String sql = select + from + where + limit;
        log.info(sql);

        DatabaseClient.GenericExecuteSpec ges = databaseClient.sql(sql);

        if (StringUtils.hasText(bookCriteria.q())) {
            ges = ges.bind("q", "%" + bookCriteria.q() + "%");
        }

        if (bookCriteria.publicationDate() != null) {
            ges = ges.bind("publicationDate", bookCriteria.publicationDate());
        }

        return ges.bind("pageSize", pageable.getPageSize())
                .bind("offset", pageable.getOffset())
                .map((row, metadata) -> {
                    log.info("publicationDate {} ", metadata.getColumnMetadata("publicationDate"));
                    log.info("bookId {} ", metadata.getColumnMetadata("bookId").toString());

                    return (IBookProjection) BookVO.builder()
                            .id(row.get("bookId", Integer.class))
                            .title(row.get("title", String.class))
                            .publicationDate(row.get("publicationDate", LocalDate.class))
                            .onlineAvailability(row.get("onlineAvailability", Boolean.class))
                            .concatAuthors(row.get("concatAuthors", String.class))
                            .build();
                })
                .all();
    }
}
