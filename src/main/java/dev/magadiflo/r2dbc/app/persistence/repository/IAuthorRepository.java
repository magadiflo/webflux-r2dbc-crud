package dev.magadiflo.r2dbc.app.persistence.repository;

import dev.magadiflo.r2dbc.app.model.projection.IAuthorProjection;
import dev.magadiflo.r2dbc.app.persistence.entity.Author;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface IAuthorRepository extends ReactiveCrudRepository<Author, Integer> {

    /**
     * @param author
     * @return affectedRows
     */
    @Modifying
    @Query(value = """
            INSERT INTO authors(first_name, last_name, birthdate)
            VALUES(:#{#author.firstName}, :#{#author.lastName}, :#{#author.birthdate})
            """)
    Mono<Integer> saveAuthor(@Param(value = "author") Author author);

    /**
     * @param author
     * @return affectedRows
     */
    @Modifying
    @Query(value = """
            UPDATE authors
            SET first_name = :#{#author.firstName},
                last_name = :#{#author.lastName},
                birthdate = :#{#author.birthdate}
            WHERE id = :#{#author.id}
            """)
    Mono<Integer> updateAuthor(@Param(value = "author") Author author);

    @Query("""
            SELECT COUNT(a.id)
            FROM authors AS a
            WHERE a.first_name LIKE '%' || :q || '%' OR a.last_name LIKE '%' || :q || '%'
            """)
    Mono<Integer> findCountByQ(@Param(value = "q") String q);

    @Query("""
            SELECT a.id, a.first_name, a.last_name, CONCAT(a.first_name, ' ', a.last_name) AS full_name, a.birthdate
            FROM authors AS a
            WHERE a.id = :authorId
            """)
    Mono<IAuthorProjection> findByAuthorId(@Param(value = "authorId") Integer authorId);

    @Query("""
            SELECT a.id, a.first_name, a.last_name, a.first_name || ' ' || a.last_name AS full_name, a.birthdate
            FROM authors AS a
            WHERE a.first_name LIKE CONCAT('%',:q,'%') OR a.last_name LIKE CONCAT('%',:q,'%')
            ORDER BY a.id ASC
            LIMIT :#{#pageable.pageSize} OFFSET :#{#pageable.offset}
            """)
    Flux<IAuthorProjection> findByQ(@Param(value = "q") String q, @Param(value = "pageable") Pageable pageable);
}
